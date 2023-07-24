package io.goooler.oauth2webview

import com.google.gson.Gson
import java.io.IOException
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

/**
 * Retrofit API to communicate with the Authorization Server
 */
object OAuth2Api {
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
    ): Result<OAuth2AccessToken> {
        val json = createRequestBodyString(clientId, clientSecret, code, redirectUri, grantType, null)
        return runCatching {
            post(url, json).let {
                gson.fromJson(it, OAuth2AccessToken::class.java)
            }
        }
    }

    fun requestNewAccessToken(
        url: String,
        clientId: String,
        clientSecret: String?,
        redirectUri: String,
        grantType: String,
        refreshToken: String,
    ): Result<OAuth2AccessToken> {
        val json = createRequestBodyString(
            clientId,
            clientSecret,
            null,
            redirectUri,
            grantType,
            refreshToken,
        )
        return runCatching {
            post(url, json).let {
                gson.fromJson(it, OAuth2AccessToken::class.java)
            }
        }
    }


    private fun post(url: String, json: String): String? {
        val body: RequestBody = RequestBody.create(mediaType, json)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            return response.body()!!.string()
        }
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
