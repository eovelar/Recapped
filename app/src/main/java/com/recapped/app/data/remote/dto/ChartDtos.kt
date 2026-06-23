package com.recapped.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TopArtistsResponse(
    @Json(name = "artists")
    val artists: ArtistsWrapper
)

@JsonClass(generateAdapter = true)
data class UserTopArtistsResponse(
    @Json(name = "topartists")
    val topArtists: ArtistsWrapper
)

@JsonClass(generateAdapter = true)
data class ArtistsWrapper(
    @Json(name = "artist")
    val artist: List<ArtistDto>,

    @Json(name = "@attr")
    val attr: AttrDto?
)

@JsonClass(generateAdapter = true)
data class ArtistDto(
    @Json(name = "name")
    val name: String,

    @Json(name = "playcount")
    val playcount: String?,

    @Json(name = "listeners")
    val listeners: String?,

    @Json(name = "mbid")
    val mbid: String?,

    @Json(name = "url")
    val url: String?,

    @Json(name = "image")
    val image: List<ImageDto>?
)

@JsonClass(generateAdapter = true)
data class ImageDto(
    @Json(name = "#text")
    val url: String?,

    @Json(name = "size")
    val size: String?
)

@JsonClass(generateAdapter = true)
data class AttrDto(
    @Json(name = "page")
    val page: String?,

    @Json(name = "perPage")
    val perPage: String?,

    @Json(name = "total")
    val total: String?,

    @Json(name = "totalPages")
    val totalPages: String?
)

@JsonClass(generateAdapter = true)
data class RecentTracksResponse(
    @Json(name = "recenttracks")
    val recentTracks: RecentTracksWrapper
)

@JsonClass(generateAdapter = true)
data class RecentTracksWrapper(
    @Json(name = "track")
    val tracks: List<RecentTrackDto>,

    @Json(name = "@attr")
    val attr: AttrDto?
)

@JsonClass(generateAdapter = true)
data class RecentTrackDto(
    @Json(name = "name")
    val name: String,

    @Json(name = "artist")
    val artist: RecentTrackArtistDto,

    @Json(name = "album")
    val album: RecentTrackAlbumDto?,

    @Json(name = "image")
    val image: List<ImageDto>?,

    @Json(name = "date")
    val date: RecentTrackDateDto?,

    @Json(name = "@attr")
    val attr: RecentTrackAttrDto?
)

@JsonClass(generateAdapter = true)
data class RecentTrackArtistDto(
    @Json(name = "#text")
    val name: String,

    @Json(name = "mbid")
    val mbid: String?
)

@JsonClass(generateAdapter = true)
data class RecentTrackAlbumDto(
    @Json(name = "#text")
    val name: String?,

    @Json(name = "mbid")
    val mbid: String?
)

@JsonClass(generateAdapter = true)
data class RecentTrackDateDto(
    @Json(name = "uts")
    val unixTimestamp: String?,

    @Json(name = "#text")
    val formattedDate: String?
)

@JsonClass(generateAdapter = true)
data class RecentTrackAttrDto(
    @Json(name = "nowplaying")
    val nowPlaying: String?
)