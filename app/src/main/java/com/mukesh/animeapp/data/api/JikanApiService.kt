package com.mukesh.animeapp.data.api

import com.mukesh.animeapp.data.model.Anime
import com.mukesh.animeapp.data.model.AnimeDetail
import com.mukesh.animeapp.data.model.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApiService {

    @GET("v4/top/anime")
    suspend fun getTopAnime(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<Anime>>


    @GET("v4/anime/{id}")
    suspend fun getAnimeDetails(@Path("id") id: Int): ApiResponse<AnimeDetail>
}
