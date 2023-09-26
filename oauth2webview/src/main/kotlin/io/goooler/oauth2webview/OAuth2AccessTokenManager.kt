package io.goooler.oauth2webview

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import io.goooler.oauth2webview.OAuth2StateListener.Companion.cancel
import io.goooler.oauth2webview.OAuth2StateListener.Companion.failure
import java.net.URL
import java.util.concurrent.CountDownLatch
import okhttp3.OkHttpClient

/**
 * Token manger used to handle all the access token needs, only Authorization Code Grant flow is supported [https://oauth.net/2/grant-types/authorization-code/](https://oauth.net/2/grant-types/authorization-code/).
 *
 *
 * For a simple usage use the [setUpWebView] method to sign in the user through the Authorization Server webpage
 * and use [retrieveValidAccessToken] when in need of the access token
 *
 * @param storage an [OAuth2AccessTokenStorage] implementation that will store the access token securely
 * @param authorizationEndpoint The [authorization endpoint URI](https://tools.ietf.org/html/rfc6749#section-3.1) for the service.
 * @param tokenEndpoint The [token endpoint URI](https://tools.ietf.org/html/rfc6749#section-3.2) for the service.
 * @param clientId the client ID as in the specifications [https://tools.ietf.org/html/rfc6749#section-2.3.1](https://tools.ietf.org/html/rfc6749#section-2.3.1)
 * @param clientSecret the client Secret as in the specifications [https://tools.ietf.org/html/rfc6749#section-2.3.1](https://tools.ietf.org/html/rfc6749#section-2.3.1)
 * @param redirectUri the redirectURI as in the specifications [https://tools.ietf.org/html/rfc6749#section-3.1.2](https://tools.ietf.org/html/rfc6749#section-3.1.2)
 * @param scope the scope as in the specifications [https://tools.ietf.org/html/rfc6749#section-3.3](https://tools.ietf.org/html/rfc6749#section-3.3)
 */
class OAuth2AccessTokenManager(
    private val storage: OAuth2AccessTokenStorage,
    private val authorizationEndpoint: String,
    private val tokenEndpoint: String,
    private val clientId: String,
    private val clientSecret: String?,
    private val redirectUri: String,
    private val scope: String?,
    client: OkHttpClient = OkHttpClient(),
) {
    private val api = OAuth2Api(client)

    var prompt: String? = null

    /**
     * The [URL] to show in a [WebView]
     */
    private val authorizationUrl get() = URL(
        Uri.parse(authorizationEndpoint)
            .buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("scope", scope)
            .appendQueryParameter("response_type", "code")
            .apply {
                if (prompt != null) appendQueryParameter("prompt", prompt)
            }
            .build()
            .toString(),
    )

    /**
     * Returns a valid access token asynchronously
     * This call is asynchronous and can make a network call if the token needs to be refreshed
     * @param listener the state of the operation
     */
    fun retrieveValidAccessToken(listener: OAuth2StateListener) {
        val accessToken = storage.getStoredAccessToken()
        when {
            accessToken == null -> {
                listener.failure("No stored AccessToken found")
            }

            accessToken.isExpired -> {
                if (accessToken.refreshToken != null) {
                    requestRefreshedAccessToken(accessToken.refreshToken, listener)
                } else {
                    listener.failure("No RefreshToken")
                }
            }

            else -> {
                listener.onSuccess(accessToken)
            }
        }
    }

    /**
     * Returns a valid access token synchronously
     * This call is synchronous and can make a network call if the token needs to be refreshed
     * @return a valid access token or null
     */
    fun retrieveValidAccessTokenBlocking(): OAuth2AccessToken? {
        val countDownLatch = CountDownLatch(1)
        var newToken: OAuth2AccessToken? = null
        retrieveValidAccessToken(
            object : OAuth2StateListener {
                override fun onSuccess(token: OAuth2AccessToken) {
                    newToken = token
                    countDownLatch.countDown()
                }

                override fun onFailure(e: OAuth2Exception) {
                    countDownLatch.countDown()
                }

                override fun onLoading() = Unit
            },
        )
        countDownLatch.await()
        return newToken
    }

    /**
     * Force a refresh of the access token and save the new one
     * This network call is synchronous
     * @return a valid access token or null
     */
    fun forceRefreshAccessTokenBlocking(): OAuth2AccessToken? {
        val refreshToken = storage.getStoredAccessToken()?.refreshToken
        var newAccessToken: OAuth2AccessToken? = null
        if (refreshToken != null) {
            val countDownLatch = CountDownLatch(1)
            requestRefreshedAccessToken(
                refreshToken,
                object : OAuth2StateListener {
                    override fun onSuccess(token: OAuth2AccessToken) {
                        newAccessToken = token
                        countDownLatch.countDown()
                    }

                    override fun onFailure(e: OAuth2Exception) {
                        countDownLatch.countDown()
                    }

                    override fun onLoading() = Unit
                },
            )
            countDownLatch.await()
        }

        return newAccessToken
    }

    /**
     * Exchange the code received from the Authorization Server for the access token
     * @param code as described in [https://tools.ietf.org/html/rfc6749#section-4.1]
     * @param listener the state of the operation
     */
    fun exchangeAndSaveTokenUsingCode(code: String, listener: OAuth2StateListener) {
        listener.onLoading()
        api.requestAccessToken(
            url = tokenEndpoint,
            clientId = clientId,
            clientSecret = clientSecret,
            code = code,
            redirectUri = redirectUri,
            grantType = "authorization_code",
            listener = listener,
        )
    }

    /**
     * @return true if there is a valid access token stored
     */
    fun isLogged(): Boolean {
        val storedAccessToken = storage.getStoredAccessToken()?.accessToken
        return !storedAccessToken.isNullOrEmpty()
    }

    /**
     * Remove the stored access token
     */
    fun removeAccessToken() {
        storage.removeAccessToken()
    }

    /**
     * Set up a [WebView] to display the OAuth2 authorization page and be called when the login has been successful or not
     *
     * @param webView the [WebView] that will contain the login page of the app
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun setUpWebView(webView: WebView, listener: OAuth2StateListener) {
        webView.clearCache(true)
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.webViewClient = object : WebViewClient() {
            // For API level >= 26
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    return super.shouldOverrideUrlLoading(view, request)
                }
                val should = shouldOverrideUrlLoading(request.url, listener)
                return if (should) true else super.shouldOverrideUrlLoading(view, request)
            }

            // For API level < 26
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // For API level >= 26
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return super.shouldOverrideUrlLoading(view, url)
                }
                val should = shouldOverrideUrlLoading(Uri.parse(url), listener)
                @Suppress("DEPRECATION")
                return if (should) true else super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                listener.onPageFinished(url)
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCloseWindow(window: WebView) {
                listener.cancel("User closed the window")
            }
        }

        // Load the authorization URL
        val url = authorizationUrl.toString()
        webView.loadUrl(url)
    }

    private fun shouldOverrideUrlLoading(url: Uri, listener: OAuth2StateListener): Boolean {
        if (url.toString().startsWith(redirectUri)) {
            url.getQueryParameter("code")?.let { code ->
                exchangeAndSaveTokenUsingCode(code, listener)
                return true
            }

            val error = url.getQueryParameter("error")
            val errorDesc = url.getQueryParameter("error_description")
            if (error != null || errorDesc != null) {
                listener.cancel("$error: $errorDesc")
                return true
            }
        }
        return false
    }

    /**
     * Make a request to the Authorization Server to refresh the access token
     * @param listener the state of the operation
     */
    private fun requestRefreshedAccessToken(
        refreshToken: String,
        listener: OAuth2StateListener,
    ) {
        listener.onLoading()
        api.requestNewAccessToken(
            url = tokenEndpoint,
            clientId = clientId,
            clientSecret = clientSecret,
            redirectUri = redirectUri,
            grantType = "refresh_token",
            refreshToken = refreshToken,
            listener = listener,
        )
    }
}
