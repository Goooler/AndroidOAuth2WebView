package com.alessandrodefrancesco.oauth2webviewsample.models

import com.google.gson.annotations.SerializedName

data class InstagramUser(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("profile_picture") val profilePicture: String,
    @SerializedName("bio") val bio: String,
    @SerializedName("website") val website: String,
    @SerializedName("is_business") val isBusiness: Boolean,
    @SerializedName("counts") val counts: Counts
) {
    data class Counts(
        @SerializedName("media") val media: Long,
        @SerializedName("follows") val follows: Long,
        @SerializedName("followed_by") val followedby: Long
    )
}