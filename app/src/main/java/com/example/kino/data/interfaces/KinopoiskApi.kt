package com.example.kino.data.interfaces

import com.example.kino.data.PremiereResponse
import com.example.kino.data.ApiReviewResponse
import com.example.kino.data.MovieDto
import com.example.kino.data.SearchResponse
import com.example.kino.data.VideoResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface KinopoiskApi {
    @Headers("X-API-KEY: a22aca21-f4b6-42f1-be34-de638e5c1df6")
    @GET("api/v2.2/films/{id}")
    suspend fun getMovieDetails(@Path("id") id: Int): MovieDto
    @Headers("X-API-KEY: a22aca21-f4b6-42f1-be34-de638e5c1df6")
    @GET("api/v2.2/films/{id}/videos")
    suspend fun getFilmVideos(@Path("id") id: Int): VideoResponse
    @Headers("X-API-KEY: a22aca21-f4b6-42f1-be34-de638e5c1df6")
    @GET("/api/v2.1/films/search-by-keyword")
    suspend fun searchByKeyword(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1
    ):SearchResponse
    @Headers("X-API-KEY: a22aca21-f4b6-42f1-be34-de638e5c1df6")
    @GET("/api/v2.2/films/premieres")
    suspend fun getPremieres(
        @Query("year") year: Int,
        @Query("month") month: String
    ): PremiereResponse
    @Headers("X-API-KEY: a22aca21-f4b6-42f1-be34-de638e5c1df6")
    @GET("/api/v2.2/films/{id}/reviews")
    suspend fun getReviews(
        @Path("id") id: Int,
        @Query("page") page: Int = 1,
        @Query("order") order: String = "DATE_DESC"
    ): ApiReviewResponse


}