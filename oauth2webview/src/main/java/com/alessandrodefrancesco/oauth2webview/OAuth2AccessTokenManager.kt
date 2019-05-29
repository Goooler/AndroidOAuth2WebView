package com.alessandrodefrancesco.oauth2webview

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import com.alessandrodefrancesco.utils.LoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.util.concurrent.CountDownLatch

/**
 * Token manger used to handle all the access token needs, only Authorization Code Grant flow is supported [https://oauth.net/2/grant-types/authorization-code/](https://oauth.net/2/grant-types/authorization-code/).
 *
 *
 * For a simple usage use the [setUpWebView] method to sign in the user through the Authorization Server webpage
 * and use [retrieveValidAccessToken] when in need of the access token
 *
 * @param storage an [OAuth2AccessTokenStorage] implementation that will store the access token securely
 * @param authorizationServerBaseURL the base URL for the Authorization Server that is compliant with the specifications in [https://tools.ietf.org/html/rfc6749](https://tools.ietf.org/html/rfc6749) (Ex. https://api.instagram.com/oauth/)
 * @param clientID the client ID as in the specifications [https://tools.ietf.org/html/rfc6749#section-2.3.1](https://tools.ietf.org/html/rfc6749#section-2.3.1)
 * @param clientSecret the client Secret as in the specifications [https://tools.ietf.org/html/rfc6749#section-2.3.1](https://tools.ietf.org/html/rfc6749#section-2.3.1)
 * @param redirectURI the redirectURI as in the specifications [https://tools.ietf.org/html/rfc6749#section-3.1.2](https://tools.ietf.org/html/rfc6749#section-3.1.2)
 * @param scope the scope as in the specifications [https://tools.ietf.org/html/rfc6749#section-3.3](https://tools.ietf.org/html/rfc6749#section-3.3)
 */
class OAuth2AccessTokenManager(
    private val storage: OAuth2AccessTokenStorage,
    private val authorizationServerBaseURL: String,
    private val clientID: String,
    private val clientSecret: String,
    private val redirectURI: String,
    private val scope: String?
) {

    var DEBUG = false

    /**
     * The URL path of the OAuth2 service where there is the webpage to show to user
     */
    var authorizationPath = "authorize"
    /**
     * The URL path of the OAuth2 service used to retrieve and refresh the access token
     */
    var tokenPath = "access_token"
    /**
     * The URL path of the OAuth2 service used to logout/invalidate the access token
     */
    var logoutPath = "logout"

    /**
     * The API used to communicate with the Authorization Server
     */
    private val networkAPI: OAuth2Api
        get() {
            var builder = Retrofit.Builder()
            builder = builder.addConverterFactory(GsonConverterFactory.create(Gson()))
            builder = builder.baseUrl(authorizationServerBaseURL)
            if(DEBUG){
                val client = OkHttpClient.Builder().addInterceptor(LoggingInterceptor()).build()
                builder = builder.client(client)
            }

            val retrofit = builder.build()
            return retrofit.create(OAuth2Api::class.java)
        }


    /**
     * The [URL] to show in a [WebView]
     */
    val authorizationUrl = URL(
        Uri.parse(authorizationServerBaseURL + authorizationPath)
            .buildUpon()
            .appendQueryParameter("client_id", clientID)
            .appendQueryParameter("redirect_uri", redirectURI)
            .appendQueryParameter("scope", scope)
            .appendQueryParameter("response_type", "code")
            .build().toString()
    )

    /**
     * Returns a valid access token asynchronously
     * This call is asynchronous and can make a network call if the token needs to be refreshed
     * @param callback the result of the operation
     */
    fun retrieveValidAccessToken(callback: (Result<OAuth2AccessToken>) -> Unit) {
        val accessToken = storage.getStoredAccessToken()
        when {
            accessToken == null -> {
                callback(Result.failure(Exception("No stored Token found")))
            }
            accessToken.isExpired -> {
                if (accessToken.refreshToken != null) {
                    requestRefreshedAccessToken(accessToken.refreshToken, callback)
                } else {
                    callback(Result.failure(Exception("No Refresh Access Token")))
                }
            }
            else -> {
                callback(Result.success(accessToken))
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
        var token: OAuth2AccessToken? = null
        retrieveValidAccessToken { result ->
            token = result.getOrNull()
            countDownLatch.countDown()
        }
        countDownLatch.await()
        return token
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
            requestRefreshedAccessToken(refreshToken) { result ->
                newAccessToken = result.getOrNull()
                countDownLatch.countDown()
            }
            countDownLatch.await()
        }

        return newAccessToken
    }

    /**
     * Make a request to the Authorization Server to refresh the access token
     * @param callback the result of the operation
     */
    private fun requestRefreshedAccessToken(refreshToken: String, callback: (Result<OAuth2AccessToken>) -> Unit) {
        return networkAPI.requestNewAccessToken(
            path = tokenPath,
            refreshToken = refreshToken,
            clientID = clientID,
            clientSecret = clientSecret,
            redirectUri = redirectURI,
            grantType = "refresh_token"
        ).enqueue(object : Callback<OAuth2AccessToken> {
            override fun onFailure(call: Call<OAuth2AccessToken>, t: Throwable) {
                callback(Result.failure(t))
            }

            override fun onResponse(call: Call<OAuth2AccessToken>, response: Response<OAuth2AccessToken>) {
                val accessToken = response.body()
                if (!response.isSuccessful || accessToken == null) {
                    callback(Result.failure(Exception()))
                } else {
                    storage.storeAccessToken(accessToken)
                    callback(Result.success(accessToken))
                }
            }
        })
    }

    /**
     * Exchange the code received from the Authorization Server for the access token
     * @param code as described in [https://tools.ietf.org/html/rfc6749#section-4.1]
     * @param callback the result of the operation
     */
    fun exchangeAndSaveTokenUsingCode(code: String, callback: (Result<OAuth2AccessToken>) -> Unit) {
        networkAPI.requestAccessToken(
            path = tokenPath,
            clientID = clientID,
            clientSecret = clientSecret,
            redirectUri = redirectURI,
            code = code,
            grantType = "authorization_code"
        ).enqueue(object : Callback<OAuth2AccessToken> {
            override fun onFailure(call: Call<OAuth2AccessToken>, t: Throwable) {
                callback(Result.failure(t))
            }

            override fun onResponse(call: Call<OAuth2AccessToken>, response: Response<OAuth2AccessToken>) {
                val accessToken = response.body()
                if (!response.isSuccessful || accessToken == null) {
                    callback(Result.failure(Exception()))
                } else {
                    // success, save the token
                    storage.storeAccessToken(accessToken)
                    callback(Result.success(accessToken))
                }
            }
        })
    }

    /**
     * @return true if there is a valid access token stored
     */
    fun isLogged(): Boolean {
        val storedAccessToken = storage.getStoredAccessToken()?.accessToken
        return storedAccessToken != null && storedAccessToken.isNotEmpty()
    }

    /**
     * Logs out the user from the authorization server
     * @param callback the result of the operation
     */
    fun logout(callback: (Result<Any?>) -> Unit) {
        val refreshedToken = storage.getStoredAccessToken()?.refreshToken
        if (refreshedToken != null) {
            networkAPI.requestLogout(
                path = logoutPath,
                clientID = clientID,
                clientSecret = clientSecret,
                refreshToken = refreshedToken
            )
                .enqueue(object : Callback<Any?> {
                    override fun onFailure(call: Call<Any?>, t: Throwable) {
                        callback(Result.failure(t))
                    }

                    override fun onResponse(call: Call<Any?>, response: Response<Any?>) {
                        if (!response.isSuccessful) {
                            callback(Result.failure(Exception()))
                        } else {
                            storage.removeAccessToken()
                            callback(Result.success(Any()))
                        }
                    }
                })
        } else {
            storage.removeAccessToken()
            callback(Result.success(Any()))
        }
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
     * @param loginFail when the login failed
     * @param loginSuccess when the login has been successful and the access token has been successfully stored
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun setUpWebView(webView: WebView, loginSuccess: () -> Unit, loginFail: () -> Unit) {
        webView.clearCache(true)
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val code = request?.url?.getQueryParameter("code")
                Log.d("OAuth2", "Redirecting to: ${request?.url}")
                if (code != null) {
                    exchangeAndSaveTokenUsingCode(code) { result ->
                        result.onSuccess {
                            loginSuccess()
                        }
                        result.onFailure {
                            loginFail()
                        }
                    }
                    return true
                }

                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        // Load the authorization URL
        val url = authorizationUrl.toString()
        webView.loadUrl(url)
    }

}