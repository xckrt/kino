package com.example.kino.data

import com.google.gson.annotations.SerializedName

data class VideoDto(
    @SerializedName("url") val url: String,
    @SerializedName("name") val name: String?,
    @SerializedName("site") val site: String?
)
