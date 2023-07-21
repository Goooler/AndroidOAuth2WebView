package com.alessandrodefrancesco.oauth2webview

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Calendar

/**
 * Represents an OAuth2 AccessToken response received from the Authorization Server as described in [https://tools.ietf.org/html/rfc6749#section-5.1](https://tools.ietf.org/html/rfc6749#section-5.1).
 */
data class OAuth2AccessToken(
    /**
     * REQUIRED
     * The type of the token issued as described in [https://tools.ietf.org/html/rfc6749#section-7.1](https://tools.ietf.org/html/rfc6749#section-7.1).
     * Value is case insensitive.
     */
    @SerializedName("token_type")
    val tokenType: String? = null,

    /**
     * REQUIRED
     * The access token issued by the authorization server.
     */
    @SerializedName("access_token")
    val accessToken: String? = null,

    /**
     * OPTIONAL
     * The refresh token, which can be used to obtain new
     * access tokens using the same authorization grant as described
     * in [https://tools.ietf.org/html/rfc6749#section-6](https://tools.ietf.org/html/rfc6749#section-6).
     */
    @SerializedName("refresh_token")
    val refreshToken: String? = null,

    /**
     * RECOMMENDED
     * The lifetime in seconds of the access token.  For
     * example, the value "3600" denotes that the access token will
     * expire in one hour from the time the response was generated.
     * If omitted, the authorization server SHOULD provide the
     * expiration time via other means or document the default value.
     */
    @SerializedName("expires_in")
    val expiresIn: Int? = null,

    /**
     * OPTIONAL
     */
    @SerializedName("ext_expires_in")
    val extExpiresIn: Int? = null,

    /**
     * OPTIONAL
     */
    val scope: String? = null,

) : Serializable {

    /**
     * The expiration date of the token, calculated from [expiresIn].
     */
    val expirationDate: Calendar? = if (expiresIn == null) {
        null
    } else {
        Calendar.getInstance().apply { add(Calendar.SECOND, expiresIn) }
    }

    /**
     * Returns whether the access token is expired or not.
     *
     * @return true if expired. Otherwise false.
     */
    val isExpired: Boolean
        get() = expirationDate != null && Calendar.getInstance().after(expirationDate)
}
