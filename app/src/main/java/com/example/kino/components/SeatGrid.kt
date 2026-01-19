package com.example.kino.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.kino.data.Seat
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun SeatGrid(seats: List<Seat>, onSeatClick: (Seat) -> Unit) {
    val rows = seats.groupBy { it.row }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { (_, rowSeats) ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowSeats.forEach { seat ->
                    SeatItem(seat = seat, onClick = { onSeatClick(seat) })
                }
            }
        }
    }
}