package com.recapped.app.domain.model

data class Artist(
    val mbid: String,
    val name: String,
    val playcount: Long,
    val listeners: Long,
    val imageUrl: String?,
    val rank: Int
)

data class ArtistDetail(
    val artist: Artist,
    val bio: String?,
    val tags: List<String>,
    val topTracks: List<Track>
)

data class Track(
    val name: String,
    val playcount: Long,
    val imageUrl: String?
)

data class SongDetail(
    val name: String,
    val artistName: String,
    val albumTitle: String,
    val imageUrl: String?,
    val releaseDate: String?,
    val trackCount: Int,
    val albumTracks: List<AlbumTrack>,
    val deezerTrackId: Long,
    val isrc: String?
)

data class AlbumTrack(
    val name: String,
    val durationSeconds: Int,
    val deezerTrackId: Long
)