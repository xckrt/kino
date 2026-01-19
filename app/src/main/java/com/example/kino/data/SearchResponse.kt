package com.example.kino.data

data class SearchResponse(
    val keyword: String? = null,
    val pagesCount: Int = 0,
    val films: List<MovieDto> = emptyList()
)