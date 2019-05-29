package com.alessandrodefrancesco.oauth2webviewsample

import com.alessandrodefrancesco.oauth2webviewsample.models.InstagramBaseResponse
import com.alessandrodefrancesco.oauth2webviewsample.models.InstagramUser
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface InstagramAPI {

    @GET("users/self")
    fun getUserInfo(@Query("access_token") accessToken: String): Call<InstagramBaseResponse<InstagramUser>>
}
