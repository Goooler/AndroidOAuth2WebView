# Android OAuth2 Authorization Code Grant

This is a fork of [AlessandroDeFrancesco/AndroidOAuth2WebView](https://github.com/AlessandroDeFrancesco/AndroidOAuth2WebView), wants to be the easiest and fastest setup of the OAuth2 Authorization Code Grant flow for an Android application.

The only external library used is OkHttp to make requests to the Authorization Server.

Why not building on Chrome Custom Tabs like [Auth0.Android](https://github.com/auth0/Auth0.Android) or [openid/AppAuth-Android](https://github.com/openid/AppAuth-Android)? Because its customization options are not as extensive the WebView.

## Usage

You can start the OAuth2 Authorization Code Grant by following these steps:

1. Create an Activity/Fragment with a WebView
2. Initialize the OAuth2AccessTokenManager (Preferably as a Singleton or through Injection)
    ```kotlin
    /*
    * Example with Instagram API (https://www.instagram.com/developer/)
    */
    val sharedPreferences = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
    val storageSharedPreferences = OAuth2AccessTokenStorageSharedPreferences(sharedPreferences)
    accessTokenManager = OAuth2AccessTokenManager(
        storage = storageSharedPreferences,
        authorizationEndpoint = AUTHORIZATION_ENDPOINT,
        tokenEndpoint = TOKEN_ENDPOINT,
        clientID = CLIENT_ID,
        clientSecret = CLIENT_SECRET,
        redirectUri = "http://samplecallback.com/",
        scope = "basic"
    )
    ```
3. Let the accessTokenManager set up the WebView
    ```kotlin
    accessTokenManager.setUpWebView(
        binding.webView,
        object : OAuth2StateListener {
            override fun onFailure(e: OAuth2Exception) {
                Log.e("Login", "Failure")
            }
            override fun onSuccess(token: OAuth2AccessToken) {
                Log.d("Login", "Success")
            }

            override fun onLoading() {
                Log.e("Login", "Loading")
            }
        },
    )
    ```
4. On a successful login you can access and use the Access Token anywhere:
    ```kotlin
    // Asynchronously
    accessTokenManager.retrieveValidAccessToken { result ->
        override fun onSuccess(token: OAuth2AccessToken) {
            Log.d("Access Token", token.accessToken)
        }

        override fun onFailure(e: OAuth2Exception) = Unit

        override fun onLoading() = Unit
    }
    ```

    ```kotlin
    // Synchronously (Beware that it can make a network request if the token is expired and can crash the app if it is made in the UI Thread)
    val storedToken = accessTokenManager.retrieveValidAccessTokenBlocking()
    Log.d("Access Token", storedToken.accessToken)
    ```
    The OAuth2AccessTokenManager will take care of refreshing the token when it expires.

# Customizations

## Token Storage

The OAuth2AccessTokenManager uses OAuth2AccessTokenStorage to store and retrieve the access token securely. A naive implementation is provided as example, OAuth2AccessTokenStorageSharedPreferences uses the shared preferences in MODE_PRIVATE to save it. You can implement OAuth2AccessTokenStorage as you wish with the level of security that you need.
