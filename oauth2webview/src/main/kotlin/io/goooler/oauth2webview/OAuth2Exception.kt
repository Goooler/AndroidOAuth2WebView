package io.goooler.oauth2webview

sealed class OAuth2Exception(message: String, cause: Throwable?) : Exception(message, cause) {
    /**
     * User canceled the authorization process
     */
    class UserCancelException(message: String) : OAuth2Exception(message, null)

    /**
     * The authorization server returned an error
     */
    class OAuth2AuthException(cause: Throwable?) : OAuth2Exception("OAuth2 auth failed", cause)
}
