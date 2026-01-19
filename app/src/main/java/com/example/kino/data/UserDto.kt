package com.example.kino.data

data class UserDto(
    val uid: String = "",
    val email: String = "",
    val rating: Double = 0.0,
    val username: String = "",
    val ticketCount: Int = 0,
    val points: Int = 0,
    val reviewCount: Int = 0,
    val unlockedAchievements: List<String> = emptyList()
)