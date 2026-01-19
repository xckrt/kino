package com.example.kino.data

import com.example.kino.data.ApiReviewDto

data class ApiReviewResponse(
    val total: Int,
    val items: List<ApiReviewDto>
)
