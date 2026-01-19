package com.example.kino.data

import androidx.room.Embedded
import androidx.room.Relation

data class TicketWithMovie(
    @Embedded val ticket: TicketEntity,
    @Relation(
        parentColumn = "filmId",
        entityColumn = "filmId"
    )
    val movie: MovieEntity
)