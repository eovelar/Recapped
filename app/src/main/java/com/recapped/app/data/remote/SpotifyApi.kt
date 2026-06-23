package com.recapped.app.data.remote

import com.recapped.app.data.remote.dto.SpotifySearchResponse
import com.recapped.app.data.remote.dto.SpotifyTokenResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SpotifyApi {

    @GET("search")
    suspend fun searchTrack(
        @Header("Authorization") authorization: String,
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("market") market: String = "AR",
        @Query("limit") limit: Int = 1
    ): SpotifySearchResponse

    @GET("search")
    suspend fun searchArtist(
        @Header("Authorization") authorization: String,
        @Query("q") query: String,
        @Query("type") type: String = "artist",
        @Query("market") market: String = "AR",
        @Query("limit") limit: Int = 5
    ): SpotifySearchResponse
}

interface SpotifyAccountsApi {

    @FormUrlEncoded
    @POST("api/token")
    suspend fun requestAccessToken(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("client_id") clientId: String,
        @Field("code_verifier") codeVerifier: String
    ): SpotifyTokenResponse

    @FormUrlEncoded
    @POST("api/token")
    suspend fun refreshAccessToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String
    ): SpotifyTokenResponse
}