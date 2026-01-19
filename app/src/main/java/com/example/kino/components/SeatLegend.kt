package com.example.kino.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kino.screens.SeatColorOccupied
import com.example.kino.screens.SeatColorSelected

@Composable
fun SeatLegend() {
    val seatColorFree = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        LegendItem(seatColorFree, "Свободно")
        Spacer(Modifier.width(16.dp))
        LegendItem(SeatColorSelected, "Выбрано")
        Spacer(Modifier.width(16.dp))
        LegendItem(SeatColorOccupied, "Занято")
    }
}