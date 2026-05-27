package com.recapped.app.data.remote

import com.recapped.app.data.remote.dto.ArtistInfoResponse
import com.recapped.app.data.remote.dto.TopArtistsResponse
import com.recapped.app.data.remote.dto.TopTracksResponse
import com.recapped.app.data.remote.dto.UserTopArtistsResponse
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

    /**
     * Charts globales de artistas.
     * Actualmente usado para rankings mundiales.
     */
    @GET("2.0/")
    suspend fun getTopArtists(
        @Query("method") method: String = "chart.gettopartists",
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): TopArtistsResponse

    /**
     * Charts globales de canciones.
     * Lo dejamos disponible por si querés seguir usando una sección global.
     */
    @GET("2.0/")
    suspend fun getTopTracks(
        @Query("method") method: String = "chart.gettoptracks",
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): TopTracksResponse

    /**
     * Top artistas personales de un usuario de Last.fm.
     *
     * period puede ser:
     * overall, 7day, 1month, 3month, 6month, 12month
     */
    @GET("2.0/")
    suspend fun getUserTopArtists(
        @Query("method") method: String = "user.gettopartists",
        @Query("user") user: String,
        @Query("period") period: String = "1month",
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): UserTopArtistsResponse

    /**
     * Top canciones personales de un usuario de Last.fm.
     *
     * period puede ser:
     * overall, 7day, 1month, 3month, 6month, 12month
     */
    @GET("2.0/")
    suspend fun getUserTopTracks(
        @Query("method") method: String = "user.gettoptracks",
        @Query("user") user: String,
        @Query("period") period: String = "1month",
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): TopTracksResponse

    /**
     * Canciones recientes del usuario.
     * Útil para Home o para mostrar actividad reciente.
     */
    @GET("2.0/")
    suspend fun getUserRecentTracks(
        @Query("method") method: String = "user.getrecenttracks",
        @Query("user") user: String,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): TopTracksResponse

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