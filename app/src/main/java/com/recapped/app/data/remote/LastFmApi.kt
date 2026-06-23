package com.recapped.app.data.remote

import com.recapped.app.data.remote.dto.ArtistInfoResponse
import com.recapped.app.data.remote.dto.RecentTracksResponse
import com.recapped.app.data.remote.dto.TopArtistsResponse
import com.recapped.app.data.remote.dto.TopTracksResponse
import com.recapped.app.data.remote.dto.UserTopArtistsResponse
import retrofit2.http.GET
import retrofit2.http.Query

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
    suspend fun getTopTracks(
        @Query("method") method: String = "chart.gettoptracks",
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): TopTracksResponse

    @GET("2.0/")
    suspend fun getUserTopArtists(
        @Query("method") method: String = "user.gettopartists",
        @Query("user") user: String,
        @Query("period") period: String,
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): UserTopArtistsResponse

    @GET("2.0/")
    suspend fun getUserTopTracks(
        @Query("method") method: String = "user.gettoptracks",
        @Query("user") user: String,
        @Query("period") period: String,
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): TopTracksResponse

    @GET("2.0/")
    suspend fun getUserRecentTracks(
        @Query("method") method: String = "user.getrecenttracks",
        @Query("user") user: String,
        @Query("from") from: Long,
        @Query("to") to: Long,
        @Query("limit") limit: Int = 200,
        @Query("page") page: Int = 1,
        @Query("extended") extended: Int = 0,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): RecentTracksResponse

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
        const val BASE_URL =
            "https://ws.audioscrobbler.com/"
    }
}