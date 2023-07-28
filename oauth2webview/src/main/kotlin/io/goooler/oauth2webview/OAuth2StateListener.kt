package io.goooler.oauth2webview

interface OAuth2StateListener {

    fun onSuccess(token: OAuth2AccessToken)

    fun onFailure(e: OAuth2Exception)

    fun onLoading()
}
