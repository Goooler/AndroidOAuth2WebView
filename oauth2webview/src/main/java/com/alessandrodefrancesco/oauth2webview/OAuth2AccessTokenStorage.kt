package com.alessandrodefrancesco.oauth2webview

/**
 * An interface used by [OAuth2AccessTokenManager] to retrieve, store and remove the access token
 */
interface OAuth2AccessTokenStorage {
    /**
     * Return the stored OAuth2 Access Token if there is one stored
     */
    fun getStoredAccessToken(): OAuth2AccessToken?

    /**
     * Store the OAuth2 Access Token
     */
    fun storeAccessToken(accessToken: OAuth2AccessToken)

    /**
     * @return true if there is an access token stored
     */
    fun hasAccessToken(): Boolean

    /**
     * Deletes the OAuth2 Access Token if there is one stored
     */
    fun removeAccessToken()
}