package com.example.kino.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.kino.data.Seat
import com.example.kino.screens.SeatColorOccupied
import com.example.kino.screens.SeatColorSelected
import com.example.kino.data.SeatState

@Composable
fun SeatItem(seat: Seat, onClick: () -> Unit) {
    val seatColorFree = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    val color = when (seat.state) {
        SeatState.FREE -> seatColorFree
        SeatState.OCCUPIED -> SeatColorOccupied.copy(alpha = 0.5f)
        SeatState.SELECTED -> SeatColorSelected
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .padding(1.dp)
            .clickable(enabled = seat.state != SeatState.OCCUPIED) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerRadius = CornerRadius(4.dp.toPx())
            drawRoundRect(color, topLeft = Offset(size.width * 0.1f, 0f), size = Size(size.width * 0.8f, size.height * 0.6f), cornerRadius = cornerRadius)
            drawRoundRect(color, topLeft = Offset(size.width * 0.1f, size.height * 0.55f), size = Size(size.width * 0.8f, size.height * 0.35f), cornerRadius = cornerRadius)
            drawRoundRect(color, topLeft = Offset(0f, size.height * 0.3f), size = Size(size.width * 0.15f, size.height * 0.5f), cornerRadius = CornerRadius(2.dp.toPx()))
            drawRoundRect(color, topLeft = Offset(size.width * 0.85f, size.height * 0.3f), size = Size(size.width * 0.15f, size.height * 0.5f), cornerRadius = CornerRadius(2.dp.toPx()))
            if (seat.state == SeatState.OCCUPIED) {
                drawLine(Color.Black, start = Offset(10f, 10f), end = Offset(size.width - 10f, size.height - 10f), strokeWidth = 3f)
                drawLine(Color.Black, start = Offset(size.width - 10f, 10f), end = Offset(10f, size.height - 10f), strokeWidth = 3f)
            }
        }
    }
}
