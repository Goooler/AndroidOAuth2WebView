package io.goooler.oauth2webview

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit API to communicate with the Authorization Server
 */
interface OAuth2Api {

    @FormUrlEncoded
    @POST("{path}")
    fun requestAccessToken(
        @Path("path") path: String,
        @Field("client_id") clientID: String,
        @Field("code") code: String,
        @Field("redirect_uri")redirectUri: String,
        @Field("grant_type") grantType: String,
    ): Call<OAuth2AccessToken>

    @FormUrlEncoded
    @POST("{path}")
    fun requestNewAccessToken(
        @Path("path") path: String,
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientID: String,
        @Field("client_secret") clientSecret: String,
        @Field("redirect_uri")redirectUri: String,
        @Field("grant_type") grantType: String,
    ): Call<OAuth2AccessToken>

    @FormUrlEncoded
    @POST("{path}")
    fun requestLogout(
        @Path("path") path: String,
        @Field("client_id") clientID: String,
        @Field("client_secret") clientSecret: String,
        @Field("refresh_token") refreshToken: String,
    ): Call<Any?>
}
