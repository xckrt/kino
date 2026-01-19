package com.example.kino.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kino.components.AchievementPopup
import com.example.kino.data.TicketStatus
import com.example.kino.dialogs.ReviewDialog
import com.example.kino.viewmodel.CinemaViewModel
import kotlinx.coroutines.delay

@SuppressLint("ComposableDestinationInComposeScope")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CinemaApp(viewModel: CinemaViewModel) {
    val navController = rememberNavController()
    val currentUserId by viewModel.currentUserId.collectAsState()
    var achievementTitle by remember { mutableStateOf("") }
    var achievementIcon by remember { mutableStateOf<ImageVector?>(null) }
    var isPopupVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.notificationChannel.collect { message ->
            val rawTitle = message.replace("🏆 Достижение: ", "").trim()
            val foundAchievement = com.example.kino.utils.AchievementSystem.list.find {
                it.title.equals(rawTitle, ignoreCase = true)
            }
            achievementTitle = rawTitle
            achievementIcon = foundAchievement?.icon
            isPopupVisible = true
            delay(4000)
            isPopupVisible = false
        }
    }
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            val route = navController.currentDestination?.route
            if (route == "login" || route == "register") {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }
    val startDestination = if (currentUserId == null) "login" else "home"
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        NavHost(navController, startDestination = startDestination) {

            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegisterNavigate = {
                        navController.navigate("register")
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onMovieClick = { movieId -> navController.navigate("movieDetails/$movieId") },
                    onTicketsClick = { navController.navigate("tickets") },
                    onProfileClick = { navController.navigate("profile") }
                )
            }
            composable(
                "movieDetails/{movieId}",
                arguments = listOf(navArgument("movieId") { type = NavType.IntType })
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: -1
                MovieDetailsScreen(
                    viewModel = viewModel,
                    movieId = movieId,
                    onBuyTicketClick = { id -> navController.navigate("booking/$id") },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                "booking/{movieId}",
                arguments = listOf(navArgument("movieId") { type = NavType.IntType })
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
                BookingScreen(
                    viewModel = viewModel,
                    filmId = movieId,
                    onBookingComplete = {
                        navController.navigate("tickets") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                )
            }
            composable("tickets") {
                val tickets by viewModel.myTickets.collectAsState()
                var showReviewDialog by remember { mutableStateOf(false) }
                var reviewMovieId by remember { mutableStateOf(0) }
                var reviewMovieTitle by remember { mutableStateOf("") }
                if (showReviewDialog) {
                    ReviewDialog(
                        movieTitle = reviewMovieTitle,
                        onDismiss = { showReviewDialog = false },
                        onSubmit = { text, type ->
                            viewModel.addReview(reviewMovieId, reviewMovieTitle, text, type) {
                                showReviewDialog = false
                            }
                        }
                    )
                }
                LaunchedEffect(Unit) {
                    viewModel.loadMyTickets()
                }
                MyTicketsScreen(
                    tickets = tickets,
                    onBackClick = { navController.popBackStack() },
                    onPayClick = { ticketId -> viewModel.updateTicketStatus(ticketId, TicketStatus.PAID) },
                    onCancelClick = { ticketId -> viewModel.deleteTicket(ticketId) },
                    onLeaveReviewClick = { mid, title ->
                        reviewMovieId = mid
                        reviewMovieTitle = title
                        showReviewDialog = true
                    }
                )
            }
            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onLogout = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }
        AchievementPopup(
            visible = isPopupVisible,
            text = achievementTitle,
            icon=achievementIcon
        )
    }
}