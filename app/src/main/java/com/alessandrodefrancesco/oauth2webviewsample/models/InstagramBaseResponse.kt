package com.alessandrodefrancesco.oauth2webviewsample.models

import com.google.gson.annotations.SerializedName

data class InstagramBaseResponse<DataType>(
    @SerializedName("meta")
    val meta: Meta,
    @SerializedName("data")
    val data: DataType?,
    @SerializedName("pagination")
    val pagination: Pagination?,
) {
    data class Meta(
        @SerializedName("code")
        val code: Int,
        @SerializedName("error_type")
        val errorType: String?,
        @SerializedName("error_message")
        val errorMessage: String?,
    )
    data class Pagination(
        @SerializedName("next_url")
        val nextUrl: Int,
        @SerializedName("next_max_id")
        val nextMaxId: String?,
    )
}
