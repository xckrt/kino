package com.example.kino.utils

import java.math.BigDecimal
import java.math.RoundingMode

object LoyaltySystem {

    data class LoyaltyLevel(
        val name: String,
        val minRating: Double,
        val maxDiscountPercent: Int
    )
    val levels = listOf(
        LoyaltyLevel("Новичок", 0.0, 0),
        LoyaltyLevel("Любитель", 3.0, 15),
        LoyaltyLevel("Киноман", 4.0, 30),
        LoyaltyLevel("Эксперт", 4.8, 50)
    )
    fun calculateNewRating(currentRating: Double, ticketCount: Int): Double {
        var newRating = currentRating + 0.1
        if ((ticketCount + 1) % 5 == 0) {
            newRating += 0.2
        }
        return newRating.coerceIn(0.0, 5.0).round(1)
    }

    fun getCurrentLevel(rating: Double): LoyaltyLevel {
        return levels.findLast { rating >= it.minRating } ?: levels.first()
    }
    fun getDiscountPercent(rating: Double): Int {
        return getCurrentLevel(rating).maxDiscountPercent
    }
    fun getStatusLabel(rating: Double): String {
        return getCurrentLevel(rating).name
    }
    private fun Double.round(decimals: Int): Double {
        return BigDecimal(this).setScale(decimals, RoundingMode.HALF_UP).toDouble()
    }
}