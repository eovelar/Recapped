package com.recapped.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeezerArtistSearchResponse(
    @Json(name = "data") val data: List<DeezerArtistDto>
)

@JsonClass(generateAdapter = true)
data class DeezerArtistDto(
    @Json(name = "id") val id: Long?,
    @Json(name = "name") val name: String?,
    @Json(name = "picture") val picture: String?,
    @Json(name = "picture_small") val pictureSmall: String?,
    @Json(name = "picture_medium") val pictureMedium: String?,
    @Json(name = "picture_big") val pictureBig: String?,
    @Json(name = "picture_xl") val pictureXl: String?
)