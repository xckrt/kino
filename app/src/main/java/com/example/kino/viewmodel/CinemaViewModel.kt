package com.example.kino.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.kino.data.FirestoreTicket
import com.example.kino.data.MovieDto
import com.example.kino.data.MovieEntity
import com.example.kino.data.ThemeStorage
import com.example.kino.data.TicketEntity
import com.example.kino.data.TicketStatus
import com.example.kino.data.TicketWithMovie
import com.example.kino.data.UserDto
import com.example.kino.data.UserReview
import com.example.kino.data.interfaces.KinopoiskApi
import com.example.kino.utils.AchievementSystem
import com.example.kino.utils.LoyaltySystem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.min
@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class CinemaViewModel @Inject constructor(
    private val api: KinopoiskApi,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val themeStorage: ThemeStorage
) : ViewModel() {
    data class Seat(val row: Int, val num: Int)
    private val _notificationChannel = Channel<String>()
    val notificationChannel = _notificationChannel.receiveAsFlow()

    private val _currentUserProfile = MutableStateFlow<UserDto?>(null)
    val currentUserProfile = _currentUserProfile.asStateFlow()
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId = _currentUserId.asStateFlow()
    private val _currentMovieReviews = MutableStateFlow<List<UserReview>>(emptyList())
    val currentMovieReviews = _currentMovieReviews.asStateFlow()
    val isDarkTheme: StateFlow<Boolean> = themeStorage.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    private val _currentUserReviews = MutableStateFlow<List<UserReview>>(emptyList())
    val currentUserReviews = _currentUserReviews.asStateFlow()
    private val _isAuthChecking = MutableStateFlow(true)
    val isAuthChecking = _isAuthChecking.asStateFlow()
    private val _movies = MutableStateFlow<List<MovieDto>>(emptyList())
    val movies = _movies.asStateFlow()
    private val activeMovieIds = mutableSetOf<Int>()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _userPoints = MutableStateFlow<Int>(0)
    val userPoints = _userPoints.asStateFlow()
    private val _myTickets = MutableStateFlow<List<com.example.kino.data.TicketWithMovie>>(emptyList())
    val myTickets = _myTickets.asStateFlow()
    private val _topUsers = MutableStateFlow<List<UserDto>>(emptyList())
    val topUsers = _topUsers.asStateFlow()
    private var searchJob: Job? = null
    init {
        checkAuth()
        loadLeaderboard()
        loadNowPlaying()
        loadSchedule()
    }
    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeStorage.saveTheme(isDark)
        }
    }
    fun loadReviews(movieId: Int) {
        viewModelScope.launch {
            val allReviews = mutableListOf<UserReview>()
            try {
                val snapshot = db.collection("reviews")
                    .whereEqualTo("movieId", movieId)
                    .get()
                    .await()
                val fbReviews = snapshot.toObjects(UserReview::class.java)
                allReviews.addAll(fbReviews)
            } catch (e: Exception) { e.printStackTrace() }
            try {
                val response = api.getReviews(movieId)
                val apiReviews = response.items.map { dto ->
                    UserReview(
                        id = "api_${dto.author.hashCode()}",
                        movieId = movieId,
                        authorName = dto.author ?: "Зритель",
                        text = (dto.title ?: "") + "\n\n" + (dto.description ?: ""),
                        type = dto.type ?: "NEUTRAL",
                        date = dto.date?.take(10) ?: "",
                        timestamp = 0
                    )
                }
                allReviews.addAll(apiReviews)
            } catch (e: Exception) { e.printStackTrace() }
            _currentMovieReviews.value = allReviews
        }
    }
    fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""
        loadLeaderboard()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(UserDto::class.java)
                    _currentUserProfile.value = user
                    _userPoints.value = user?.points ?: 0
                } else {
                    val newUser = UserDto(
                        uid = uid,
                        email = email,
                        username = email.substringBefore("@"),
                        rating = 0.0,
                        points = 0
                    )
                    db.collection("users").document(uid).set(newUser)
                    _currentUserProfile.value = newUser
                    _userPoints.value = 0
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }
    fun addReview(movieId: Int, movieTitle: String, text: String, type: String, onComplete: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)
        val newReviewRef = db.collection("reviews").document()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef)
            val userDto = userSnapshot.toObject(UserDto::class.java) ?: UserDto()
            val finalAuthorName = userDto.username.ifEmpty { userDto.email.substringBefore("@") }
            val review = UserReview(
                id = newReviewRef.id,
                movieId = movieId,
                movieTitle = movieTitle,
                userId = uid,
                authorName = finalAuthorName,
                text = text,
                type = type,
                date = currentDate,
                timestamp = System.currentTimeMillis()
            )
            val newReviewCount = (userDto.reviewCount ?: 0) + 1
            val newAchievements = AchievementSystem.checkNewAchievements(
                currentAchievements = userDto.unlockedAchievements,
                ticketCount = userDto.ticketCount,
                rating = userDto.rating,
                sessionTime = "12:00",
                reviewCount = newReviewCount,
                seatsInBooking = 0,
                reviewType = type,
                isWeekend = false
            )
            transaction.set(newReviewRef, review)
            transaction.update(userRef, "unlockedAchievements", userDto.unlockedAchievements + newAchievements)
            newAchievements
        }.addOnSuccessListener { newIds ->
            if (newIds.isNotEmpty()) {
                viewModelScope.launch {
                    newIds.forEach { id ->
                        val ach = AchievementSystem.list.find { it.id == id }
                        if (ach != null) _notificationChannel.send("🏆 Достижение: ${ach.title}")
                    }
                }
            }
            loadReviews(movieId)
            loadUserData()
            onComplete()
        }
    }
    fun loadUserReviews() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("reviews")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                _currentUserReviews.value = result.toObjects(UserReview::class.java)
            }
    }
    private fun checkAuth() {
        val user = auth.currentUser
        _currentUserId.value = user?.uid
        if (user != null) {
            loadUserData()
        }
        _isAuthChecking.value = false
    }
    fun registerUser(email: String, pass: String, username: String, onResult: (String?) -> Unit) {
        _isLoading.value = true
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    _isLoading.value = false
                    onResult("Пользователь с таким именем уже существует")
                } else {
                    performAuthRegistration(email, pass, username, onResult)
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                onResult("Ошибка проверки имени: ${e.localizedMessage}")
            }
    }
    fun updateUsername(newName: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("username", newName)
            .addOnSuccessListener {
                loadUserData()
            }
    }
    fun loginUser(email: String, pass: String, onResult: (String?) -> Unit) {
        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null && user.isEmailVerified) {
                    _currentUserId.value = user.uid
                    loadUserData()
                    _isLoading.value = false
                    onResult(null)
                } else {
                    auth.signOut()
                    _isLoading.value = false
                    onResult("Пожалуйста, подтвердите email. Проверьте почту.")
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
                onResult("Ошибка входа: ${it.localizedMessage}")
            }
    }
    private fun performAuthRegistration(email: String, pass: String, username: String, onResult: (String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: return@addOnCompleteListener
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                    user.updateProfile(profileUpdates).addOnCompleteListener {
                        user.sendEmailVerification()
                            .addOnSuccessListener {
                                val newUser = UserDto(
                                    uid = uid,
                                    email = email,
                                    username = username,
                                    rating = 0.0,
                                    points = 0
                                )
                                db.collection("users").document(uid).set(newUser)
                                    .addOnSuccessListener {
                                        auth.signOut()
                                        _isLoading.value = false
                                        onResult(null)
                                    }
                            }
                            .addOnFailureListener {
                                _isLoading.value = false
                                onResult("Ошибка отправки письма. Попробуйте позже.")
                            }
                    }
                } else {
                    _isLoading.value = false
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException) {
                        onResult("Этот Email уже зарегистрирован")
                    } else {
                        onResult(exception?.localizedMessage ?: "Ошибка регистрации")
                    }
                }
            }
    }
    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Введите email для сброса")
            return
        }
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Ошибка сброса")
            }
    }
    fun resendVerificationEmail(email: String, password: String, onResult: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                it.user?.sendEmailVerification()
                    ?.addOnSuccessListener {
                        auth.signOut()
                        onResult("Письмо отправлено повторно!")
                    }
                    ?.addOnFailureListener { e ->
                        auth.signOut()
                        onResult("Ошибка: ${e.localizedMessage}")
                    }
            }
    }
    fun logout() {
        auth.signOut()
        _currentUserId.value = null
        _currentUserProfile.value = null
    }
    fun loadLeaderboard() {
        db.collection("users")
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val users = result.toObjects(UserDto::class.java)
                _topUsers.value = users
            }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadNowPlaying() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentResize = LocalDate.now()
                val year = currentResize.year
                val month = currentResize.month.name
                val response = api.getPremieres(year, month)
                val items = response.items.filter { it.nameRu != null }
                activeMovieIds.clear()
                items.forEach { movie ->
                    activeMovieIds.add(movie.id)
                }
                _movies.value = items
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun isMovieActive(movieId: Int): Boolean {
        return activeMovieIds.contains(movieId)
    }
    fun loadSchedule() {
        db.collection("active_movies").whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { result ->
                val movieIds = result.documents.mapNotNull { it.getLong("filmId")?.toInt() }
                if (movieIds.isNotEmpty()) {
                    viewModelScope.launch {
                        val loadedMovies = mutableListOf<MovieDto>()
                        movieIds.forEach { id ->
                            try {
                                val movie = api.getMovieDetails(id)
                                loadedMovies.add(movie)
                            } catch (e: Exception) { }
                        }
                        _movies.value = loadedMovies
                    }
                }
            }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun searchMovies(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(800)
            if (query.isBlank()) {
                loadNowPlaying()
            } else {
                _isLoading.value = true
                _movies.value = emptyList()
                try {
                    val response = api.searchByKeyword(query)
                    _movies.value = response.films.filter {
                        it.nameRu != null || it.nameEn != null || it.nameOriginal != null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
    fun buyTicket(
        filmId: Int,
        price: Double,
        sessionTime: String,
        movieTitle: String,
        posterUrl: String,
        date: String,
        seats: List<Seat>,
        status: TicketStatus,
        usePoints: Boolean = false,
        onComplete: () -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)
        val globalTicketsCollection = db.collection("tickets")
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentData = snapshot.toObject(UserDto::class.java) ?: UserDto()
            var finalPrice = price
            var pointsToDeduct = 0
            if (usePoints && currentData.points > 0 && status == TicketStatus.PAID) {
                val allowedPercent = LoyaltySystem.getDiscountPercent(currentData.rating)
                val maxDiscountRubles = price * (allowedPercent / 100.0)
                pointsToDeduct = min(currentData.points.toDouble(), maxDiscountRubles).toInt()

                finalPrice -= pointsToDeduct
            }
            val newCount = currentData.ticketCount + 1
            val newRating = LoyaltySystem.calculateNewRating(currentData.rating, currentData.ticketCount)
            val earnedPoints = (finalPrice * 0.1).toInt()
            val newPoints = (currentData.points - pointsToDeduct) + earnedPoints
            val isWeekend = try {
                val localDate = LocalDate.parse(date)
                val day = localDate.dayOfWeek
                day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
            } catch (e: Exception) { false }
            val newAchievements = AchievementSystem.checkNewAchievements(
                currentAchievements = currentData.unlockedAchievements,
                ticketCount = newCount,
                rating = newRating,
                sessionTime = sessionTime,
                reviewCount = currentData.reviewCount,
                seatsInBooking = seats.size,
                reviewType = null,
                isWeekend = isWeekend
            )
            val finalAchievements = currentData.unlockedAchievements + newAchievements
            transaction.update(userRef, "ticketCount", newCount)
            transaction.update(userRef, "rating", newRating)
            transaction.update(userRef, "points", newPoints)
            transaction.update(userRef, "unlockedAchievements", finalAchievements)
            newAchievements
        }.addOnSuccessListener { newAchievements ->
            val batch = db.batch()
            seats.forEach { seat ->
                val newTicketRef = globalTicketsCollection.document()
                val qrData = "TICKET:${newTicketRef.id}|$movieTitle|$date|$sessionTime|${seat.row}-${seat.num}"
                val ticketData = FirestoreTicket(
                    id = newTicketRef.id,
                    userId = uid,
                    filmId = filmId,
                    movieTitle = movieTitle,
                    posterUrl = posterUrl,
                    seatRow = seat.row,
                    seatNumber = seat.num,
                    date = date,
                    time = sessionTime,
                    status = status.name,
                    qrCodeData = qrData
                )
                batch.set(newTicketRef, ticketData)
            }
            batch.commit().addOnSuccessListener {
                if (newAchievements.isNotEmpty()) {
                    viewModelScope.launch {
                        newAchievements.forEach { id ->
                            val ach = AchievementSystem.list.find { it.id == id }
                            if (ach != null) _notificationChannel.send("🏆 Достижение: ${ach.title}")
                        }
                    }
                }
                loadMyTickets()
                loadUserData()
                onComplete()
            }
        }
    }
    suspend fun getOccupiedSeats(filmId: Int, date: String, time: String): List<FirestoreTicket> {
        return try {
            val snapshot = db.collection("tickets")
                .whereEqualTo("filmId", filmId)
                .whereEqualTo("date", date)
                .whereEqualTo("time", time)
                .whereIn("status", listOf("PAID", "BOOKED"))
                .get()
                .await()
            snapshot.toObjects(FirestoreTicket::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    suspend fun getMovieById(filmId: Int): MovieDto {
        return api.getMovieDetails(filmId)
    }
    fun loadMyTickets() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("tickets")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                val uiTickets = result.documents.mapNotNull { doc ->
                    val fsTicket = doc.toObject(FirestoreTicket::class.java)
                    if (fsTicket != null) {
                        TicketEntity(
                            id = doc.id,
                            userId = uid,
                            filmId = fsTicket.filmId,
                            seatRow = fsTicket.seatRow,
                            seatNumber = fsTicket.seatNumber,
                            date = fsTicket.date,
                            time = fsTicket.time,
                            price = 350.0,
                            status = try { TicketStatus.valueOf(fsTicket.status) } catch (e: Exception) { TicketStatus.BOOKED },
                            qrCodeData = fsTicket.qrCodeData
                        ).let { entity ->
                            val movieEntity = MovieEntity(
                                filmId = fsTicket.filmId,
                                title = fsTicket.movieTitle,
                                posterUrl = fsTicket.posterUrl,
                                year = "",
                                genre = ""
                            )
                            TicketWithMovie(entity, movieEntity)
                        }
                    } else null
                }
                _myTickets.value = uiTickets
            }
    }
    fun updateTicketStatus(ticketId: String, newStatus: TicketStatus) {
        val uid = auth.currentUser?.uid ?: return
        val ticketRef = db.collection("tickets").document(ticketId)
        ticketRef.update("status", newStatus.name)
            .addOnSuccessListener {
                loadMyTickets()
            }
            .addOnFailureListener { e -> e.printStackTrace() }
    }
    fun deleteTicket(ticketId: String) {
        println("DEBUG: Попытка удалить билет с ID: $ticketId")
        val ticketRef = db.collection("tickets").document(ticketId)
        ticketRef.delete()
            .addOnSuccessListener {
                println("DEBUG: Билет успешно удален из базы")
                loadMyTickets()
            }
            .addOnFailureListener { e ->
                println("DEBUG: Ошибка удаления: ${e.localizedMessage}")
                e.printStackTrace()
            }
    }
    suspend fun getFilmVideos(filmId: Int): com.example.kino.data.VideoResponse {
        return api.getFilmVideos(filmId)
    }
}