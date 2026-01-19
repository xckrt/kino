package com.example.kino

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthValidationTest {
    private fun validateRegistration(
        email: String,
        pass: String,
        confirm: String,
        username: String
    ): String? {
        if (username.isBlank()) return "Укажите имя пользователя"
        if (email.isBlank() || pass.isBlank() || confirm.isBlank()) return "Заполните все поля"
        if (!email.contains("@")) return "Некорректный Email"
        if (pass != confirm) return "Пароли не совпадают"
        if (pass.length < 6) return "Пароль слишком короткий (мин. 6)"
        if (!pass.matches(Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$"))) {
            return "Пароль слишком простой"
        }
        return null
    }
    @Test
    fun `validation fails on invalid email`() {
        val result = validateRegistration(
            email = "invalid-email",
            pass = "Pass123!",
            confirm = "Pass123!",
            username = "User"
        )
        assertEquals("Некорректный Email", result)
    }

    @Test
    fun `validation fails on password mismatch`() {
        val result = validateRegistration(
            email = "test@test.com",
            pass = "Pass123!",
            confirm = "Pass999!",
            username = "User"
        )
        assertEquals("Пароли не совпадают", result)
    }

    @Test
    fun `validation fails on weak password`() {
        val result = validateRegistration(
            email = "test@test.com",
            pass = "123456",
            confirm = "123456",
            username = "User"
        )
        assertEquals("Пароль слишком простой", result)
    }

    @Test
    fun `validation passes on correct data`() {
        val result = validateRegistration(
            email = "test@test.com",
            pass = "CorrectPass1!",
            confirm = "CorrectPass1!",
            username = "User"
        )
        assertEquals(null, result)
    }
}