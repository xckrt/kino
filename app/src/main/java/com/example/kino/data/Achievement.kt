package com.example.kino.data

import androidx.compose.ui.graphics.vector.ImageVector

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)