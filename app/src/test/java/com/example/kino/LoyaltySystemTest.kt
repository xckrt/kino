package com.example.kino

import com.example.kino.utils.LoyaltySystem
import org.junit.Assert.assertEquals
import org.junit.Test

class LoyaltySystemTest {
    @Test
    fun `calculateNewRating adds bonus correctly for standard ticket`() {
        val currentRating = 4.0
        val currentTickets = 1
        val result = LoyaltySystem.calculateNewRating(currentRating, currentTickets)
        assertEquals(4.1, result, 0.01)
    }

    @Test
    fun `calculateNewRating adds double bonus for 5th ticket`() {
        val currentRating = 4.0
        val currentTickets = 4
        val result = LoyaltySystem.calculateNewRating(currentRating, currentTickets)
        assertEquals(4.3, result, 0.01)
    }

    @Test
    fun `getDiscountPercent returns correct discount for Expert`() {
        val rating = 4.9
        val discount = LoyaltySystem.getDiscountPercent(rating)
        assertEquals(50, discount)
    }
}