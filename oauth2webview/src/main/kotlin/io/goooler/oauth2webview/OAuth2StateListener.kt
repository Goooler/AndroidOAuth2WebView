package io.goooler.oauth2webview

interface OAuth2StateListener {

    fun onSuccess(token: OAuth2AccessToken)

    fun onFailure(e: OAuth2Exception)

    fun onLoading()

    companion object {
        fun OAuth2StateListener.cancel(message: String) {
            onFailure(OAuth2Exception.UserCancelException(message))
        }

        fun OAuth2StateListener.failure(
            message: String = "OAuth2 auth failed",
            cause: Throwable? = null,
        ) {
            onFailure(OAuth2Exception.OAuth2AuthException(message, cause))
        }
    }
}
