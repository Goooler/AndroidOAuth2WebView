package com.alessandrodefrancesco.oauth2webviewsample

import android.app.Application
import android.content.Context
import com.alessandrodefrancesco.oauth2webview.OAuth2AccessTokenStorageSharedPreferences
import com.alessandrodefrancesco.oauth2webview.OAuth2AccessTokenManager

class MyApplication: Application() {

    companion object {
        lateinit var accessTokenManager: OAuth2AccessTokenManager

        const val CLIENT_ID = "CLIENT_ID"
        const val CLIENT_SECRET = "CLIENT_SECRET"
        const val REDIRECT_URI = "http://samplecallback.com/"
    }

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
        val storageSharedPreferences = OAuth2AccessTokenStorageSharedPreferences(sharedPreferences)
        accessTokenManager = OAuth2AccessTokenManager(
            storage = storageSharedPreferences,
            authorizationServerBaseURL = "https://api.instagram.com/oauth/",
            clientID = CLIENT_ID,
            clientSecret = CLIENT_SECRET,
            redirectURI = REDIRECT_URI,
            scope = "basic"
        )

        accessTokenManager.DEBUG = true
    }
}