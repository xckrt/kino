package com.example.kino.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kino.components.CinemaScreenVisual
import com.example.kino.components.DateSelector
import com.example.kino.components.SeatGrid
import com.example.kino.components.SeatLegend
import com.example.kino.data.TicketStatus
import com.example.kino.viewmodel.CinemaViewModel
import java.time.LocalDate
import java.time.LocalTime
import com.example.kino.data.Seat
import com.example.kino.data.SeatState
import com.example.kino.utils.LoyaltySystem
import kotlin.math.min
val SeatColorOccupied = Color(0xFFEF5350)
val SeatColorSelected = Color(0xFFFFCA28)
val DiscountGreen = Color(0xFF4CAF50)
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingScreen(
    viewModel: CinemaViewModel,
    filmId: Int,
    onBookingComplete: () -> Unit
) {
    val context = LocalContext.current
    val topUsers by viewModel.topUsers.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUser = remember(topUsers, currentUserId) {
        topUsers.find { it.uid == currentUserId }
    }
    val userPoints = currentUser?.points ?: 0
    val userRating = currentUser?.rating ?: 0.0
    var usePoints by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var processingType by remember { mutableStateOf<TicketStatus?>(null) }
    val allSessions = remember { listOf("10:00", "13:30", "16:45", "19:00", "21:30", "23:55") }
    val availableSessions by remember(selectedDate) {
        derivedStateOf {
            val nowTime = LocalTime.now()
            val nowDate = LocalDate.now()
            if (selectedDate == nowDate) {
                allSessions.filter { timeStr ->
                    try {
                        val sessionTime = LocalTime.parse(timeStr)
                        sessionTime.isAfter(nowTime.plusMinutes(10))
                    } catch (e: Exception) { false }
                }
            } else if (selectedDate.isAfter(nowDate)) {
                allSessions
            } else {
                emptyList()
            }
        }
    }
    var selectedTime by remember { mutableStateOf("") }
    LaunchedEffect(availableSessions) {
        if (availableSessions.isNotEmpty() && !availableSessions.contains(selectedTime)) {
            selectedTime = availableSessions.first()
        } else if (availableSessions.isEmpty()) {
            selectedTime = ""
        }
    }
    val seats = remember { mutableStateListOf<Seat>() }
    var movieTitle by remember { mutableStateOf("") }
    var posterUrl by remember { mutableStateOf("") }
    var isDataLoading by remember { mutableStateOf(true) }
    LaunchedEffect(filmId, selectedDate, selectedTime) {
        if (selectedTime.isNotEmpty()) {
            isDataLoading = true
            try {
                val movie = viewModel.getMovieById(filmId)
                movieTitle = movie.nameRu ?: movie.nameOriginal ?: "Без названия"
                posterUrl = movie.posterUrlPreview

                val occupiedTickets = viewModel.getOccupiedSeats(filmId, selectedDate.toString(), selectedTime)

                seats.clear()
                for (row in 1..6) {
                    for (num in 1..8) {
                        val isOccupied = occupiedTickets.any { it.seatRow == row && it.seatNumber == num }
                        seats.add(Seat("$row-$num", row, num, if (isOccupied) SeatState.OCCUPIED else SeatState.FREE))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isDataLoading = false
            }
        } else {
            seats.clear()
            isDataLoading = false
        }
    }
    val selectedSeats by remember { derivedStateOf { seats.filter { it.state == SeatState.SELECTED } } }
    val rawTotalPrice by remember { derivedStateOf { selectedSeats.size * 350.0 } }
    val discountInfo by remember(usePoints, rawTotalPrice, userPoints, userRating) {
        derivedStateOf {
            if (usePoints && rawTotalPrice > 0) {
                val allowedPercent = LoyaltySystem.getDiscountPercent(userRating)
                val maxDiscountRubles = rawTotalPrice * (allowedPercent / 100.0)
                val actualDiscount = min(userPoints.toDouble(), maxDiscountRubles)
                Pair(actualDiscount, allowedPercent)
            } else {
                Pair(0.0, 0)
            }
        }
    }
    val discount = discountInfo.first
    val allowedPercent = discountInfo.second
    val finalPrice by remember { derivedStateOf { rawTotalPrice - discount } }
    val isButtonEnabled = !isDataLoading && movieTitle.isNotEmpty() && selectedSeats.isNotEmpty() && selectedTime.isNotEmpty() && processingType == null
    fun processBooking(status: TicketStatus) {
        processingType = status
        val seatsForVm = selectedSeats.map { CinemaViewModel.Seat(it.row, it.num) }
        viewModel.buyTicket(
            filmId = filmId,
            price = finalPrice,
            sessionTime = selectedTime,
            movieTitle = movieTitle,
            posterUrl = posterUrl,
            date = selectedDate.toString(),
            seats = seatsForVm,
            status = status,
            onComplete = {
                processingType = null
                onBookingComplete()
            }
        )
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (movieTitle.isEmpty()) "Загрузка..." else movieTitle,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1
                        )
                        Text(
                            "Выбор мест",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = selectedSeats.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    shadowElevation = 8.dp
                ) {
                    Column(Modifier.padding(16.dp)) {
                        if (userPoints > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clickable { usePoints = !usePoints },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Списать баллы",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    val currentTierName = LoyaltySystem.getStatusLabel(userRating)
                                    val maxPercent = LoyaltySystem.getDiscountPercent(userRating)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$userPoints Б ",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "($currentTierName: до $maxPercent%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Switch(
                                    checked = usePoints,
                                    onCheckedChange = { usePoints = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(bottom = 12.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "К оплате: ",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "${finalPrice.toInt()} ₽",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                            if (usePoints && discount > 0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "-${discount.toInt()} Б",
                                        color = DiscountGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    val actualPercent = ((discount / rawTotalPrice) * 100).toInt()
                                    Text(
                                        "Скидка $actualPercent%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DiscountGreen.copy(alpha = 0.8f)
                                    )
                                }
                            } else {
                                Text(
                                    "${selectedSeats.size} билетов",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                        ) {
                            if (!usePoints) {
                                OutlinedButton(
                                    onClick = { processBooking(TicketStatus.BOOKED) },
                                    enabled = isButtonEnabled,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                                ) {
                                    if (processingType == TicketStatus.BOOKED) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Бронь", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                            Button(
                                onClick = { processBooking(TicketStatus.PAID) },
                                enabled = isButtonEnabled,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                if (processingType == TicketStatus.PAID) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    AnimatedContent(
                                        targetState = usePoints,
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(150, 150)) togetherWith
                                                    fadeOut(animationSpec = tween(150)) using SizeTransform(false)
                                        },
                                        label = "BuyButtonText"
                                    ) { pointsActive ->
                                        if (pointsActive) {
                                            Text("Купить со скидкой", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        } else {
                                            Text("Купить", maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DateSelector(selectedDate) { selectedDate = it }
            Spacer(Modifier.height(16.dp))
            Text(
                "Время сеанса",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (availableSessions.isEmpty()) {
                Text(
                    text = "На этот день сеансов больше нет",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableSessions) { time ->
                        val isSelected = time == selectedTime
                        val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        val txtColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable { selectedTime = time }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(time, color = txtColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            if (selectedTime.isNotEmpty() && !isDataLoading) {
                CinemaScreenVisual()
                Spacer(Modifier.height(32.dp))
                SeatGrid(seats) { seat ->
                    if (processingType != null) return@SeatGrid

                    val index = seats.indexOf(seat)
                    if (index != -1 && seat.state != SeatState.OCCUPIED) {
                        val newState = if (seat.state == SeatState.FREE) SeatState.SELECTED else SeatState.FREE
                        seats[index] = seat.copy(state = newState)
                    }
                }
                Spacer(Modifier.height(32.dp))
                SeatLegend()
            } else if (isDataLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}