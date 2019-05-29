package com.alessandrodefrancesco.oauth2webview

import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * A simple implementation of [OAuth2AccessTokenStorage] that saves the access token as plain text in the passed shared preferences.
 * It is recommend to set the access mode to MODE_PRIVATE.
 *
 * @property mSharedPreferences The shared preferences used for saving the access token.
 */
class OAuth2AccessTokenStorageSharedPreferences(
    private val mSharedPreferences: SharedPreferences
): OAuth2AccessTokenStorage {

    override fun getStoredAccessToken(): OAuth2AccessToken? {
        val storedJson = mSharedPreferences.getString(ACCESS_TOKEN_PREFERENCES_KEY, null)
        return if (storedJson != null)
            Gson().fromJson(storedJson, OAuth2AccessToken::class.java)
        else
            null
    }

    override fun storeAccessToken(accessToken: OAuth2AccessToken) {
        mSharedPreferences
            .edit()
            .putString(ACCESS_TOKEN_PREFERENCES_KEY, Gson().toJson(accessToken))
            .apply()
    }

    override fun hasAccessToken(): Boolean {
        return mSharedPreferences.contains(ACCESS_TOKEN_PREFERENCES_KEY)
    }

    override fun removeAccessToken() {
        mSharedPreferences
            .edit()
            .remove(ACCESS_TOKEN_PREFERENCES_KEY)
            .apply()
    }

    companion object {
        private const val ACCESS_TOKEN_PREFERENCES_KEY = "OAuth2AccessToken"
    }
}