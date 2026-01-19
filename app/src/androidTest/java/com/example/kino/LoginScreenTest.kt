package com.example.kino

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.kino.screens.LoginScreen
import com.example.kino.viewmodel.CinemaViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val viewModel = mockk<CinemaViewModel>(relaxed = true)
    @Before
    fun setup() {
        every { viewModel.isLoading } returns MutableStateFlow(false)
        every { viewModel.isAuthChecking } returns MutableStateFlow(false)
    }
    @Test
    fun loginScreen_elements_are_displayed() {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {},
                onRegisterNavigate = {}
            )
        }
        composeTestRule.onNodeWithText("Вход в систему").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Пароль").assertIsDisplayed()
        composeTestRule.onNodeWithText("ВОЙТИ").assertIsDisplayed()
    }
    @Test
    fun loginScreen_input_works() {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {},
                onRegisterNavigate = {}
            )
        }
        composeTestRule.onNodeWithText("Email")
            .performTextInput("test@test.ru")
        composeTestRule.onNodeWithText("Пароль")
            .performTextInput("123456")
        composeTestRule.onNodeWithText("test@test.ru").assertIsDisplayed()
        composeTestRule.onNodeWithText("123456").assertIsDisplayed()
    }
    @Test
    fun loginButton_calls_viewModel() {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {},
                onRegisterNavigate = {}
            )
        }
        composeTestRule.onNodeWithText("Email").performTextInput("test@test.ru")
        composeTestRule.onNodeWithText("Пароль").performTextInput("123456")
        composeTestRule.onNodeWithText("ВОЙТИ").performClick()
        verify {
            viewModel.loginUser(
                email = "test@test.ru",
                pass = "123456",
                onResult = any()
            )
        }
    }
}