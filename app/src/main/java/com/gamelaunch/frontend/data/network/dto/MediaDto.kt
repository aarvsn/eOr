package com.gamelaunch.frontend.data.network.dto

import com.google.gson.annotations.SerializedName

data class MediaDto(
    @SerializedName("type") val type: String,
    @SerializedName("url") val url: String,
    @SerializedName("region") val region: String? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("parent") val parent: String? = null,
    @SerializedName("crc") val crc: String? = null,
    @SerializedName("md5") val md5: String? = null,
    @SerializedName("sha1") val sha1: String? = null,
    @SerializedName("size") val size: String? = null
)
