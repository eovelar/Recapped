package com.recapped.app.data.remote

import com.recapped.app.data.remote.dto.DeezerAlbumDetailDto
import com.recapped.app.data.remote.dto.DeezerArtistSearchResponse
import com.recapped.app.data.remote.dto.DeezerTrackSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerApi {

    @GET("search/artist")
    suspend fun searchArtist(
        @Query("q") query: String
    ): DeezerArtistSearchResponse

    @GET("search")
    suspend fun searchTrack(
        @Query("q") query: String
    ): DeezerTrackSearchResponse

    @GET("album/{albumId}")
    suspend fun getAlbum(
        @Path("albumId") albumId: Long
    ): DeezerAlbumDetailDto
}