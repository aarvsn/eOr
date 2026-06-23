package com.gamelaunch.frontend.data.network.dto

import com.google.gson.annotations.SerializedName

data class ScraperResponseDto(
    @SerializedName("header") val header: ScraperHeaderDto? = null,
    @SerializedName("response") val response: ScraperDataDto? = null
)

data class ScraperHeaderDto(
    @SerializedName("APIversion") val apiVersion: String? = null,
    @SerializedName("dateTime") val dateTime: String? = null,
    @SerializedName("commandRequested") val commandRequested: String? = null,
    @SerializedName("success") val success: String? = null,
    @SerializedName("error") val error: String? = null
)

data class ScraperDataDto(
    @SerializedName("jeu") val game: GameInfoDto? = null
)

data class UserInfoDto(
    @SerializedName("header") val header: ScraperHeaderDto? = null,
    @SerializedName("response") val response: UserResponseDto? = null
)

data class UserResponseDto(
    @SerializedName("ssid") val ssid: String? = null,
    @SerializedName("sspassword") val sspassword: String? = null,
    @SerializedName("niveau") val level: String? = null,
    @SerializedName("maxthreads") val maxThreads: String? = null,
    @SerializedName("requeststoday") val requestsToday: String? = null,
    @SerializedName("requestsKoToday") val requestsKoToday: String? = null
)
