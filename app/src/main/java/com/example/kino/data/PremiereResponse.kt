package com.example.kino.data

import com.example.kino.data.MovieDto

data class PremiereResponse(
    val total: Int,
    val items: List<MovieDto>
)