package com.example.kino.data

import com.example.kino.data.VideoDto
import com.google.gson.annotations.SerializedName

data class VideoResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("items") val items: List<VideoDto>
)
