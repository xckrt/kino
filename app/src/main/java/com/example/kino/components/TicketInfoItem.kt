package com.example.kino.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun TicketInfoItem(label: String, value: String, textColor: Color) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}