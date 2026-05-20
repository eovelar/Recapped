package com.recapped.app.data.remote

import com.recapped.app.data.remote.dto.ArtistInfoResponse
import com.recapped.app.data.remote.dto.TopArtistsResponse
import com.recapped.app.data.remote.dto.TopTracksResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Cliente Retrofit para la API REST pública de Last.fm.
 * Doc: https://www.last.fm/api
 *
 * Todos los endpoints usan el mismo path "/2.0/" y se diferencian
 * por el query param "method".
 */
interface LastFmApi {

    @GET("2.0/")
    suspend fun getTopArtists(
        @Query("method") method: String = "chart.gettopartists",
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): TopArtistsResponse

    @GET("2.0/")
    suspend fun getArtistInfo(
        @Query("method") method: String = "artist.getinfo",
        @Query("artist") artist: String,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("lang") lang: String = "es",
        @Query("autocorrect") autocorrect: Int = 1
    ): ArtistInfoResponse

    @GET("2.0/")
    suspend fun getArtistTopTracks(
        @Query("method") method: String = "artist.gettoptracks",
        @Query("artist") artist: String,
        @Query("limit") limit: Int = 8,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("autocorrect") autocorrect: Int = 1
    ): TopTracksResponse

    companion object {
        const val BASE_URL = "https://ws.audioscrobbler.com/"
    }
}
