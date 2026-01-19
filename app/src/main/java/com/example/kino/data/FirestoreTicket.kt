package com.example.kino.data

data class FirestoreTicket(
    val id: String = "",
    val userId:String = "",
    val filmId: Int = 0,
    val movieTitle: String = "",
    val posterUrl: String = "",
    val seatRow: Int = 0,
    val seatNumber: Int = 0,
    val date: String = "",
    val time: String = "",
    val status: String = "BOOKED",
    val qrCodeData: String = ""
)
