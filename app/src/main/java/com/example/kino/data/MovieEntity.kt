package com.example.kino.data

import androidx.room.Entity
import androidx.room.PrimaryKey


data class MovieEntity(
    val filmId: Int,
    val title: String,
    val posterUrl: String,
    val genre: String? = null,
    val year: String? = null
)