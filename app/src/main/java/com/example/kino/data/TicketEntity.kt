package com.example.kino.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


data class TicketEntity(
    val id: String,
    val userId: String,
    val filmId: Int,
    val date: String,
    val time: String,
    val seatRow: Int,
    val seatNumber: Int,
    val price: Double,
    val status: TicketStatus,
    val qrCodeData: String
)