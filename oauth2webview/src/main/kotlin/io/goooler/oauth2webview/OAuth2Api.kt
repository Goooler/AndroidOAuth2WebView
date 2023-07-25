package io.goooler.oauth2webview

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import java.io.IOException
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * OkHttp API to communicate with the Authorization Server
 */
class OAuth2Api(private val client: OkHttpClient) {
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val gson = Gson()

    fun requestAccessToken(
        url: String,
        clientId: String,
        clientSecret: String?,
        code: String,
        redirectUri: String,
        grantType: String,
        callback: (Result<OAuth2AccessToken>) -> Unit,
    ) {
        val formBody = createRequestBody(clientId, clientSecret, code, redirectUri, grantType, null)
        post(url, formBody, callback)
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
        val formBody = createRequestBody(
            clientId,
            clientSecret,
            null,
            redirectUri,
            grantType,
            refreshToken,
        )
        post(url, formBody, callback)
    }

    private fun post(
        url: String,
        formBody: FormBody,
        callback: (Result<OAuth2AccessToken>) -> Unit,
    ) {
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()
        client.newCall(request).enqueue(
            object : okhttp3.Callback {
                private val handler = Handler(Looper.getMainLooper())

                override fun onFailure(call: Call, e: IOException) {
                    handler.post { callback(Result.failure(e)) }
                }

                override fun onResponse(call: Call, response: Response) {
                    handler.post {
                        if (response.isSuccessful && response.body != null) {
                            val bean = gson.fromJson(
                                response.body!!.string(),
                                OAuth2AccessToken::class.java,
                            )
                            callback(Result.success(bean))
                        } else {
                            val message = response.body?.string() ?: response.toString()
                            callback(Result.failure(Exception(message)))
                        }
                    }
                }
            },
        )
    }

    private fun createRequestBody(
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
}
