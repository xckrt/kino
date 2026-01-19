package com.example.kino.data

import com.example.kino.data.GenreDto
import com.google.gson.annotations.SerializedName

data class MovieDto(
    @SerializedName("kinopoiskId") val kinopoiskId: Int?,
    @SerializedName("filmId") val filmId: Int?,
    @SerializedName("nameRu") val nameRu: String?,
    @SerializedName("nameEn") val nameEn: String?,
    @SerializedName("nameOriginal") val nameOriginal: String?,
    @SerializedName("posterUrl") val posterUrl: String?,
    @SerializedName("posterUrlPreview") val posterUrlPreview: String,
    @SerializedName("year") val year: String?,
    @SerializedName("ratingKinopoisk") val ratingKinopoisk: Double?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("genres") val genres: List<GenreDto>?,
    @SerializedName("description") val description: String?
) {
    val id: Int
        get() = kinopoiskId ?: filmId ?: 0

    fun getTitle(): String {
        return nameRu ?: nameEn ?: nameOriginal ?: "Без названия"
    }
    fun getRatingValue(): Double {
        return ratingKinopoisk
            ?: rating?.toDoubleOrNull()
            ?: 0.0
    }
}