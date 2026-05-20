package com.recapped.app.domain.model

/**
 * Modelo de dominio. Lo que la UI consume.
 * Los DTOs de Retrofit se mapean a esto en el Repository.
 */
data class Artist(
    val mbid: String,        // id único (puede estar vacío si Last.fm no lo provee)
    val name: String,
    val playcount: Long,
    val listeners: Long,
    val imageUrl: String?,   // url remota; Glide la resuelve
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
