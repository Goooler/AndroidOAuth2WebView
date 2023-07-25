package io.goooler.oauth2webview

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import io.goooler.oauth2webview.OAuth2AccessTokenManager.Companion.failure
import io.goooler.oauth2webview.OAuth2AccessTokenManager.Companion.success
import java.io.IOException
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * OkHttp API to communicate with the Authorization Server
 */
class OAuth2Api(private val client: OkHttpClient) {
    private val gson = Gson()
    private val handler = Handler(Looper.getMainLooper())

    fun requestAccessToken(
        url: String,
        clientId: String,
        clientSecret: String?,
        code: String,
        redirectUri: String,
        grantType: String,
        callback: (Result<OAuth2AccessToken>) -> Unit,
    ) {
        val body = buildRequestBody(clientId, clientSecret, code, redirectUri, grantType, null)
        post(url, body, callback)
    }

    fun requestNewAccessToken(
        url: String,
        clientId: String,
        clientSecret: String?,
        redirectUri: String,
        grantType: String,
        refreshToken: String,
        callback: (Result<OAuth2AccessToken>) -> Unit,
    ) {
        val body = buildRequestBody(
            clientId,
            clientSecret,
            null,
            redirectUri,
            grantType,
            refreshToken,
        )
        post(url, body, callback)
    }

    private fun post(
        url: String,
        body: FormBody,
        callback: (Result<OAuth2AccessToken>) -> Unit,
    ) {
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        client.newCall(request).enqueue(HttpCallback(handler, gson, callback))
    }

    private fun buildRequestBody(
        clientId: String,
        clientSecret: String?,
        code: String?,
        redirectUri: String,
        grantType: String,
        refreshToken: String?,
    ): FormBody {
        val builder = FormBody.Builder()
            .add("client_id", clientId)
            .add("redirect_uri", redirectUri)
            .add("grant_type", grantType)
        code?.let {
            builder.add("code", it)
        }
        clientSecret?.let {
            builder.add("client_secret", it)
        }
        refreshToken?.let {
            builder.add("refresh_token", it)
        }
        return builder.build()
    }

    class HttpCallback(
        private val handler: Handler,
        private val gson: Gson,
        private val callback: (Result<OAuth2AccessToken>) -> Unit,
    ) : okhttp3.Callback {
        override fun onFailure(call: Call, e: IOException) {
            handler.post { callback.failure(e) }
        }

        override fun onResponse(call: Call, response: Response) {
            handler.post {
                if (response.isSuccessful && response.body != null) {
                    try {
                        val bean = gson.fromJson(
                            response.body!!.string(),
                            OAuth2AccessToken::class.java,
                        )
                        callback.success(bean)
                    } catch (e: Exception) {
                        callback.failure(e)
                    }
                } else {
                    val message = response.body?.string() ?: response.toString()
                    callback.failure(message)
                }
            }
        }
    }
}
