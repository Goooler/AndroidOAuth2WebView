package com.alessandrodefrancesco.oauth2webviewsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alessandrodefrancesco.oauth2webview.OAuth2AccessToken
import com.alessandrodefrancesco.oauth2webviewsample.databinding.ActivityMainBinding
import com.alessandrodefrancesco.oauth2webviewsample.models.InstagramBaseResponse
import com.alessandrodefrancesco.oauth2webviewsample.models.InstagramUser
import com.alessandrodefrancesco.utils.LoggingInterceptor
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }

        private const val INSTAGRAM_BASE_URL = "https://api.instagram.com/v1/"

        val instagramAPI: InstagramAPI by lazy {
            val client = OkHttpClient.Builder()
                .addInterceptor(LoggingInterceptor())
                .build()

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .baseUrl(INSTAGRAM_BASE_URL).client(client)
                .build()

            retrofit.create(InstagramAPI::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyApplication.accessTokenManager.retrieveValidAccessToken { result ->
            result.onSuccess { storedToken ->
                requestUserInfo(storedToken)
            }
        }
    }

    private fun requestUserInfo(storedToken: OAuth2AccessToken) {
        val accessToken = storedToken.accessToken
        if (accessToken != null) {
            instagramAPI.getUserInfo(accessToken).enqueue(object : Callback<InstagramBaseResponse<InstagramUser>> {
                override fun onFailure(call: Call<InstagramBaseResponse<InstagramUser>>, t: Throwable) {
                    Log.e("MainActivity", "Error: " + t.localizedMessage)
                }

                override fun onResponse(call: Call<InstagramBaseResponse<InstagramUser>>, response: Response<InstagramBaseResponse<InstagramUser>>) {
                    val userInfo = response.body()?.data
                    if (response.isSuccessful && userInfo != null) {
                        showUserInfo(userInfo)
                    } else {
                        Log.e("MainActivity", "Error: " + response.errorBody())
                    }
                }
            })
        }
    }

    private fun showUserInfo(userInfo: InstagramUser) {
        binding.username.text = userInfo.username
        binding.fullName.text = userInfo.fullName
    }
}
