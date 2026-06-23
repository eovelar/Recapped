package com.recapped.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpotifyTokenResponse(
    @Json(name = "access_token")
    val accessToken: String,

    @Json(name = "token_type")
    val tokenType: String,

    @Json(name = "expires_in")
    val expiresIn: Long,

    @Json(name = "refresh_token")
    val refreshToken: String?,

    @Json(name = "scope")
    val scope: String?
)

@JsonClass(generateAdapter = true)
data class SpotifySearchResponse(
    @Json(name = "tracks")
    val tracks: SpotifyTracksDto?
)

@JsonClass(generateAdapter = true)
data class SpotifyTracksDto(
    @Json(name = "items")
    val items: List<SpotifyTrackDto>
)

@JsonClass(generateAdapter = true)
data class SpotifyTrackDto(
    @Json(name = "id")
    val id: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "uri")
    val uri: String,

    @Json(name = "external_urls")
    val externalUrls: SpotifyExternalUrlsDto
)

@JsonClass(generateAdapter = true)
data class SpotifyExternalUrlsDto(
    @Json(name = "spotify")
    val spotify: String
)