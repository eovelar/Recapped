package com.recapped.app.data.local.entity

import androidx.room.Entity
import com.recapped.app.domain.model.Artist

@Entity(
    tableName = "artists",
    primaryKeys = ["username", "period", "name"]
)
data class ArtistEntity(
    val username: String,
    val period: String,
    val name: String,
    val mbid: String,
    val playcount: Long,
    val listeners: Long,
    val imageUrl: String?,
    val rank: Int
)

fun ArtistEntity.toDomain(): Artist {
    return Artist(
        mbid = mbid,
        name = name,
        playcount = playcount,
        listeners = listeners,
        imageUrl = imageUrl,
        rank = rank
    )
}

fun Artist.toEntity(
    username: String,
    period: String
): ArtistEntity {
    return ArtistEntity(
        username = username,
        period = period,
        name = name,
        mbid = mbid,
        playcount = playcount,
        listeners = listeners,
        imageUrl = imageUrl,
        rank = rank
    )
}