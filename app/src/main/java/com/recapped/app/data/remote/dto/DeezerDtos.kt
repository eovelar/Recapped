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

@JsonClass(generateAdapter = true)
data class DeezerTrackSearchResponse(
    @Json(name = "data") val data: List<DeezerTrackDto>
)

@JsonClass(generateAdapter = true)
data class DeezerTrackDto(
    @Json(name = "id") val id: Long?,
    @Json(name = "title") val title: String?,
    @Json(name = "artist") val artist: DeezerTrackArtistDto?,
    @Json(name = "album") val album: DeezerAlbumDto?
)

@JsonClass(generateAdapter = true)
data class DeezerTrackArtistDto(
    @Json(name = "id") val id: Long?,
    @Json(name = "name") val name: String?
)

@JsonClass(generateAdapter = true)
data class DeezerAlbumDto(
    @Json(name = "id") val id: Long?,
    @Json(name = "title") val title: String?,
    @Json(name = "cover") val cover: String?,
    @Json(name = "cover_small") val coverSmall: String?,
    @Json(name = "cover_medium") val coverMedium: String?,
    @Json(name = "cover_big") val coverBig: String?,
    @Json(name = "cover_xl") val coverXl: String?
)

@JsonClass(generateAdapter = true)
data class DeezerAlbumDetailDto(
    @Json(name = "id") val id: Long?,
    @Json(name = "title") val title: String?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "nb_tracks") val trackCount: Int?,
    @Json(name = "cover") val cover: String?,
    @Json(name = "cover_medium") val coverMedium: String?,
    @Json(name = "cover_big") val coverBig: String?,
    @Json(name = "cover_xl") val coverXl: String?,
    @Json(name = "tracks") val tracks: DeezerAlbumTracksDto?
)

@JsonClass(generateAdapter = true)
data class DeezerAlbumTracksDto(
    @Json(name = "data") val data: List<DeezerAlbumTrackDto>
)

@JsonClass(generateAdapter = true)
data class DeezerAlbumTrackDto(
    @Json(name = "id") val id: Long?,
    @Json(name = "title") val title: String?,
    @Json(name = "duration") val duration: Int?
)