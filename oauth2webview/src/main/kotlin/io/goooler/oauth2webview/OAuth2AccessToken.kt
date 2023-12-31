package io.goooler.oauth2webview

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
    val tokenType: String?,

    /**
     * REQUIRED
     * The access token issued by the authorization server.
     */
    @SerializedName("access_token")
    val accessToken: String?,

    /**
     * OPTIONAL
     * The refresh token, which can be used to obtain new
     * access tokens using the same authorization grant as described
     * in [https://tools.ietf.org/html/rfc6749#section-6](https://tools.ietf.org/html/rfc6749#section-6).
     */
    @SerializedName("refresh_token")
    val refreshToken: String?,

    /**
     * The ID token describing the authenticated user, if provided.
     * @see [OpenID Connect Core 1.0, Section 2](https://openid.net/specs/openid-connect-core-1_0.html#rfc.section.2)
     */
    @SerializedName("id_token")
    val idToken: String?,

    /**
     * OPTIONAL
     */
    val scope: String?,

    /**
     * RECOMMENDED
     * The lifetime in seconds of the access token.  For
     * example, the value "3600" denotes that the access token will
     * expire in one hour from the time the response was generated.
     * If omitted, the authorization server SHOULD provide the
     * expiration time via other means or document the default value.
     */
    @SerializedName("expires_in")
    val expiresIn: Int,

    /**
     * OPTIONAL
     */
    @SerializedName("ext_expires_in")
    val extExpiresIn: Int,

) : Serializable {

    /**
     * The expiration date of the token, calculated from [expiresIn].
     */
    val expirationDate: Calendar? get() = if (expiresIn == 0) {
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
