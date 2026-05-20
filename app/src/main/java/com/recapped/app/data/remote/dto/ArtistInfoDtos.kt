package com.recapped.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArtistInfoResponse(
    @Json(name = "artist") val artist: ArtistInfoDto
)

@JsonClass(generateAdapter = true)
data class ArtistInfoDto(
    @Json(name = "name") val name: String,
    @Json(name = "mbid") val mbid: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "image") val image: List<ImageDto>?,
    @Json(name = "stats") val stats: StatsDto?,
    @Json(name = "tags") val tags: TagsWrapper?,
    @Json(name = "bio") val bio: BioDto?
)

@JsonClass(generateAdapter = true)
data class StatsDto(
    @Json(name = "listeners") val listeners: String?,
    @Json(name = "playcount") val playcount: String?
)

@JsonClass(generateAdapter = true)
data class TagsWrapper(
    @Json(name = "tag") val tag: List<TagDto>?
)

@JsonClass(generateAdapter = true)
data class TagDto(@Json(name = "name") val name: String)

@JsonClass(generateAdapter = true)
data class BioDto(
    @Json(name = "summary") val summary: String?,
    @Json(name = "content") val content: String?
)

@JsonClass(generateAdapter = true)
data class TopTracksResponse(
    @Json(name = "toptracks") val topTracks: TopTracksWrapper
)

@JsonClass(generateAdapter = true)
data class TopTracksWrapper(
    @Json(name = "track") val track: List<TrackDto>
)

@JsonClass(generateAdapter = true)
data class TrackDto(
    @Json(name = "name") val name: String,
    @Json(name = "playcount") val playcount: String?,
    @Json(name = "listeners") val listeners: String?,
    @Json(name = "image") val image: List<ImageDto>?
)
