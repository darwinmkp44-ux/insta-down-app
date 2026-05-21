package com.instadown.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data model for FastSaver API "/fetch" response.
 */
data class InstagramResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("id") val id: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("type") val type: String?, // e.g., "video", "image"
    @SerializedName("download_url") val downloadUrl: String?,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?,
    @SerializedName("duration") val duration: Int?, // in seconds
    @SerializedName("caption") val caption: String?
)
