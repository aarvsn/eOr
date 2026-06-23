package com.gamelaunch.frontend.data.network.dto

import com.google.gson.annotations.SerializedName

data class GameInfoDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("noms") val names: List<NameDto>? = null,
    @SerializedName("synopsis") val synopsis: List<SynopsisDto>? = null,
    @SerializedName("medias") val medias: List<MediaDto>? = null,
    @SerializedName("genres") val genres: List<GenreDto>? = null,
    @SerializedName("dates") val dates: List<DateDto>? = null,
    @SerializedName("note") val rating: RatingDto? = null,
    @SerializedName("systeme") val system: SystemDto? = null
) {
    fun getBestName(preferredRegion: String = "us"): String? {
        if (names.isNullOrEmpty()) return null
        return names.firstOrNull { it.region == preferredRegion }?.text
            ?: names.firstOrNull { it.region == "wor" }?.text
            ?: names.firstOrNull { it.region == "eu" }?.text
            ?: names.firstOrNull()?.text
    }

    fun getBestSynopsis(preferredRegion: String = "us"): String? {
        if (synopsis.isNullOrEmpty()) return null
        return synopsis.firstOrNull { it.region == preferredRegion }?.text
            ?: synopsis.firstOrNull { it.region == "wor" }?.text
            ?: synopsis.firstOrNull()?.text
    }

    fun getMediaUrl(type: String, preferredRegion: String = "us"): String? {
        if (medias.isNullOrEmpty()) return null
        return medias.firstOrNull { it.type == type && it.region == preferredRegion }?.url
            ?: medias.firstOrNull { it.type == type && it.region == "wor" }?.url
            ?: medias.firstOrNull { it.type == type }?.url
    }

    fun getReleaseYear(preferredRegion: String = "us"): Int? {
        if (dates.isNullOrEmpty()) return null
        val dateStr = dates.firstOrNull { it.region == preferredRegion }?.text
            ?: dates.firstOrNull { it.region == "wor" }?.text
            ?: dates.firstOrNull()?.text
        return dateStr?.take(4)?.toIntOrNull()
    }

    fun getRating(): Float? {
        val text = rating?.text ?: return null
        return text.toFloatOrNull()?.div(20f) // ScreenScraper scores are 0–100; normalize to 0–5
    }

    fun getPrimaryGenre(language: String = "en"): String? {
        return genres
            ?.firstOrNull()
            ?.names
            ?.firstOrNull { it.language == language }
            ?.text
    }
}

data class NameDto(
    @SerializedName("region") val region: String,
    @SerializedName("text") val text: String
)

data class SynopsisDto(
    @SerializedName("region") val region: String? = null,
    @SerializedName("text") val text: String
)

data class GenreDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("noms") val names: List<GenreNameDto>? = null
)

data class GenreNameDto(
    @SerializedName("langue") val language: String,
    @SerializedName("text") val text: String
)

data class DateDto(
    @SerializedName("region") val region: String? = null,
    @SerializedName("text") val text: String
)

data class RatingDto(
    @SerializedName("text") val text: String
)

data class SystemDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("text") val text: String? = null
)
