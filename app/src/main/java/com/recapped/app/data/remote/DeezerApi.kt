package com.recapped.app.data.remote

import com.recapped.app.data.remote.dto.DeezerArtistSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DeezerApi {

    @GET("search/artist")
    suspend fun searchArtist(
        @Query("q") query: String
    ): DeezerArtistSearchResponse
}