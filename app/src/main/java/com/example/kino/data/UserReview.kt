package com.example.kino.data

data class UserReview(
    val id: String = "",
    val movieId: Int = 0,
    val movieTitle: String = "",
    val userId: String = "",
    val authorName: String = "",
    val text: String = "",
    val type: String = "NEUTRAL",
    val date: String = "",
    val timestamp: Long = 0
)
