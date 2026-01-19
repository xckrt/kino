package com.example.kino.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kino.components.MovieMetaTag
import com.example.kino.components.ReviewItem
import com.example.kino.data.MovieDto
import com.example.kino.dialogs.TrailerDialog
import com.example.kino.viewmodel.CinemaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MovieDetailsScreen(
    viewModel: CinemaViewModel,
    movieId: Int,
    onBuyTicketClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {

    var movie by remember { mutableStateOf<MovieDto?>(null) }
    var trailerUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showPlayer by remember { mutableStateOf(false) }
    LaunchedEffect(movieId) {
        try {
            isLoading = true
            withContext(Dispatchers.IO) {
                val movieDeferred = async { viewModel.getMovieById(movieId) }
                val videosDeferred = async { viewModel.getFilmVideos(movieId) }
                viewModel.loadReviews(movieId)
                val movieResult = movieDeferred.await()
                val videosResult = videosDeferred.await()

                movie = movieResult

                val kpWidget = videosResult.items.find {
                    it.site == "KINOPOISK_WIDGET"
                }?.url

                if (kpWidget != null) {
                    trailerUrl = kpWidget
                } else {
                    val query = "${movieResult.getTitle()} трейлер смотреть онлайн"
                    trailerUrl = "https://yandex.ru/video/search?text=$query"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }
    val reviews by viewModel.currentMovieReviews.collectAsState()
    if (showPlayer && trailerUrl != null) {
        TrailerDialog(url = trailerUrl!!) {
            showPlayer = false
        }
    }
    if (isLoading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }
    if (movie == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Не удалось загрузить данные",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Назад", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        return
    }

    val currentMovie = movie!!
    val scrollState = rememberScrollState()
    val isAvailableInCinema = remember(movie) {
        movie?.let { viewModel.isMovieActive(it.id) } ?: false
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box(Modifier.fillMaxWidth().height(450.dp)) {
                AsyncImage(
                    model = currentMovie.posterUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                val brushColor = MaterialTheme.colorScheme.background
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, brushColor),
                                startY = 300f
                            )
                        )
                )

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.onPrimary.copy(0.6f), Color.Transparent)
                            )
                        )
                )
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(top = 40.dp, start = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onPrimary.copy(0.3f))
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Color.White)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(0.6f))
                        .clickable { showPlayer = true }
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(40.dp))
                }
                Text(
                    "Трейлер",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 100.dp)
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-60).dp)
            ) {
                Text(
                    currentMovie.getTitle(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MovieMetaTag(Icons.Rounded.Star, String.format("%.1f", currentMovie.getRatingValue()), MaterialTheme.colorScheme.primary)
                    currentMovie.year?.let {
                        MovieMetaTag(
                            Icons.Rounded.DateRange,
                            it,
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    if (!currentMovie.genres.isNullOrEmpty()) {
                        MovieMetaTag(
                            Icons.Rounded.Star,
                            currentMovie.genres.firstOrNull()?.genre ?: "Кино",
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "О фильме",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = currentMovie.description ?: "Описание отсутствует...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Отзывы (${reviews.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                if (reviews.isEmpty()) {
                    Text(
                        "Пока нет отзывов",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                } else {
                    reviews.forEach { review ->
                        ReviewItem(review)
                        Spacer(Modifier.height(12.dp))
                    }
                }
                Spacer(Modifier.height(100.dp))
            }
        }
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            if (isAvailableInCinema) {
                Box(
                    Modifier
                        .padding(16.dp)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = { onBuyTicketClick(currentMovie.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Выбрать места",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Фильм находится в архиве.\nПрокат завершен.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}