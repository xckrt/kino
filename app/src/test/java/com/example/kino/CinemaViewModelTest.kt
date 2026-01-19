package com.example.kino
import com.example.kino.data.MovieDto
import com.example.kino.data.PremiereResponse
import com.example.kino.data.interfaces.KinopoiskApi
import com.example.kino.viewmodel.CinemaViewModel
import com.google.common.base.CharMatcher.any
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
@OptIn(ExperimentalCoroutinesApi::class)
class CinemaViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CinemaViewModel
    private val api = mockk<KinopoiskApi>(relaxed = true)
    private val auth = mockk<FirebaseAuth>(relaxed = true)
    private val db = mockk<FirebaseFirestore>(relaxed = true)
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CinemaViewModel(api,auth,db)
    }
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNowPlaying updates state correctly`() = runTest {

        val mockMovies = listOf(
            MovieDto(
                kinopoiskId = 1,
                filmId = 1,
                nameRu = "Фильм 1",
                nameEn = "",
                nameOriginal = "",
                posterUrl = "",
                posterUrlPreview = "",
                year = "2025",
                ratingKinopoisk = 8.5,
                rating = "8.5",
                genres = emptyList(),
                description = ""
            ),
            MovieDto(
                kinopoiskId = 2,
                filmId = 2,
                nameRu = "Фильм 2",
                nameEn = "",
                nameOriginal = "",
                posterUrl = "",
                posterUrlPreview = "",
                year = "2025",
                ratingKinopoisk = 7.0,
                rating = "7.0",
                genres = emptyList(),
                description = ""
            )
        )

        val response = PremiereResponse(
            total = mockMovies.size,
            items = mockMovies
        )
        coEvery { api.getPremieres(any(), any()) } returns response
        assertTrue(viewModel.movies.value.isEmpty())
        viewModel.loadNowPlaying()
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
        assertEquals(2, viewModel.movies.value.size)
        assertEquals("Фильм 1", viewModel.movies.value[0].nameRu)
    }

    @Test
    fun `searchMovies handles empty query without crashing`() = runTest {
        viewModel.searchMovies("")
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
    }
}