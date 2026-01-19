package com.example.kino.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarHalf
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Weekend
import androidx.compose.material.icons.rounded.WorkspacePremium
import com.example.kino.data.Achievement

object AchievementSystem {
    val list = listOf(
        Achievement("debut", "Дебют", "Купить первый билет", Icons.Rounded.ConfirmationNumber),
        Achievement("kino_maniac", "Киноманьяк", "Купить 5 билетов", Icons.Rounded.Movie),
        Achievement("VIP_person", "VIP Клиент", "Купить 10 билетов", Icons.Rounded.Diamond),
        Achievement("The_Legend", "Легенда", "Купить 20 билетов", Icons.Rounded.WorkspacePremium),
        Achievement("lark", "Жаворонок", "Сеанс до 12:00", Icons.Rounded.WbSunny),
        Achievement("owl", "Сова", "Сеанс после 22:00", Icons.Rounded.NightsStay),
        Achievement("prime_time", "Прайм-тайм", "Сеанс с 18:00 до 21:00", Icons.Rounded.Schedule),
        Achievement("first_word", "Первое слово", "Оставить 1 рецензию", Icons.Rounded.RateReview),
        Achievement("critic", "Критик", "Оставить 5 рецензий", Icons.Rounded.HistoryEdu),
        Achievement("opinion_leader", "Лидер мнений", "Оставить 10 рецензий", Icons.Rounded.Campaign),
        Achievement("hater", "Хейтер", "Оставить негативную рецензию", Icons.Rounded.ThumbDown),
        Achievement("enthusiastic", "Восторженный", "Оставить позитивную рецензию", Icons.Rounded.ThumbUp),
        Achievement("solo", "Соло", "Купить 1 билет (на сеанс)", Icons.Rounded.Person),
        Achievement("date", "Свидание", "Купить 2 билета (на сеанс)", Icons.Rounded.Favorite),
        Achievement("party", "Тусовка", "Купить 3+ билета (на сеанс)", Icons.Rounded.Groups),
        Achievement("newbie", "Новичок", "Добро пожаловать в семью", Icons.Rounded.StarOutline),
        Achievement("experienced", "Опытный", "Рейтинг 3.0", Icons.Rounded.StarHalf),
        Achievement("expert", "Эксперт", "Рейтинг 4.0", Icons.Rounded.Star),
        Achievement("movie_god", "Кино-Бог", "Рейтинг 4.8", Icons.Rounded.AutoAwesome),
        Achievement("weekend", "Выходной", "Пойти в кино в Сб или Вс", Icons.Rounded.Weekend)
    )


    fun checkNewAchievements(
        currentAchievements: List<String>,
        ticketCount: Int,
        rating: Double,
        sessionTime: String,
        reviewCount: Int,
        seatsInBooking: Int,
        reviewType: String?,
        isWeekend: Boolean
    ): MutableList<String> {
        val newUnlocked = mutableListOf<String>()
        val hour = try { sessionTime.split(":")[0].toInt() } catch (e: Exception) { -1 }
        fun check(id: String, condition: Boolean) {
            if (!currentAchievements.contains(id) && !newUnlocked.contains(id) && condition) {
                newUnlocked.add(id)
            }
        }
        check("debut", ticketCount >= 1)
        check("kino_maniac", ticketCount >= 5)
        check("VIP_person", ticketCount >= 10)
        check("The_Legend", ticketCount >= 20)
        check("lark", hour in 0..11)
        check("owl", hour >= 22)
        check("prime_time", hour in 18..20)
        check("first_word", reviewCount >= 1)
        check("critic", reviewCount >= 5)
        check("opinion_leader", reviewCount >= 10)
        check("hater", reviewType == "NEGATIVE")
        check("enthusiastic", reviewType == "POSITIVE")
        check("solo", seatsInBooking == 1)
        check("date", seatsInBooking == 2)
        check("party", seatsInBooking >= 3)
        check("newbie", rating >= 0.0)
        check("experienced", rating >= 3.0)
        check("expert", rating >= 4.0)
        check("movie_god", rating >= 4.8)
        check("weekend", isWeekend)

        return newUnlocked
    }
}