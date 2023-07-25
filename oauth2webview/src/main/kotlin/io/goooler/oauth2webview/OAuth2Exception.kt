package io.goooler.oauth2webview

sealed class OAuth2Exception(message: String, cause: Throwable?) : Exception(message, cause) {
    /**
     * User canceled the authorization process
     */
    class UserCancelException(message: String) : OAuth2Exception(message, null)

    /**
     * The authorization server returned an error
     */
    class OAuth2AuthException : OAuth2Exception {
        constructor(cause: Throwable) : super("OAuth2 auth failed", cause)
        constructor(message: String) : super(message, null)
    }
}
