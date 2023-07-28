package io.goooler.oauth2webview

sealed class OAuth2Exception private constructor(message: String, cause: Throwable?) : Exception(message, cause) {
    /**
     * User canceled the authorization process
     */
    class UserCancelException internal constructor(message: String) : OAuth2Exception(message, null)

    /**
     * The authorization server returned an error
     */
    class OAuth2AuthException internal constructor(message: String, cause: Throwable?) : OAuth2Exception(message, cause)
}
