package com.recapped.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TopArtistsResponse(
    @Json(name = "artists") val artists: ArtistsWrapper
)

@JsonClass(generateAdapter = true)
data class ArtistsWrapper(
    @Json(name = "artist") val artist: List<ArtistDto>,
    @Json(name = "@attr") val attr: AttrDto?
)

@JsonClass(generateAdapter = true)
data class ArtistDto(
    @Json(name = "name") val name: String,
    @Json(name = "playcount") val playcount: String?,
    @Json(name = "listeners") val listeners: String?,
    @Json(name = "mbid") val mbid: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "image") val image: List<ImageDto>?
)

@JsonClass(generateAdapter = true)
data class ImageDto(
    @Json(name = "#text") val url: String?,
    @Json(name = "size") val size: String?
)

@JsonClass(generateAdapter = true)
data class AttrDto(
    @Json(name = "page") val page: String?,
    @Json(name = "perPage") val perPage: String?,
    @Json(name = "total") val total: String?
)
