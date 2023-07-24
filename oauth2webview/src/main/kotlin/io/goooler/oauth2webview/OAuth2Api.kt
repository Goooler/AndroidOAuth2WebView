package io.goooler.oauth2webview

import com.google.gson.Gson
import java.io.IOException
import java.lang.Exception
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

/**
 * Retrofit API to communicate with the Authorization Server
 */
class OAuth2Api {
    private val client = OkHttpClient()
    private val mediaType = MediaType.get("application/json; charset=utf-8")
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
        val json = createRequestBodyString(clientId, clientSecret, code, redirectUri, grantType, null)
        post(url, json, callback)
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
        val json = createRequestBodyString(
            clientId,
            clientSecret,
            null,
            redirectUri,
            grantType,
            refreshToken,
        )
        post(url, json, callback)
    }

    private fun post(url: String, json: String, callback: (Result<OAuth2AccessToken>) -> Unit) {
        val body: RequestBody = RequestBody.create(mediaType, json)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        client.newCall(request).enqueue(
            object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    callback(Result.failure(e))
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful && response.body() != null) {
                        val bean = gson.fromJson(response.body()!!.string(), OAuth2AccessToken::class.java)
                        callback(Result.success(bean))
                    } else {
                        callback(Result.failure(Exception()))
                    }
                }
            },
        )
    }

    private fun createRequestBodyString(
        clientId: String,
        clientSecret: String?,
        code: String?,
        redirectUri: String,
        grantType: String,
        refreshToken: String?,
    ): String {
        val map = mutableMapOf(
            "client_id" to clientId,
            "redirect_uri" to redirectUri,
            "grant_type" to grantType,
        )
        code?.let {
            map["code"] = it
        }
        clientSecret?.let {
            map["client_secret"] = it
        }
        refreshToken?.let {
            map["refresh_token"] = it
        }
        return gson.toJson(map)
    }
}
