package com.recapped.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recaps",
    indices = [
        Index(value = ["userId", "generatedAt"])
    ]
)
data class RecapEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val username: String,
    val period: String,
    val generatedAt: Long,
    val totalScrobbles: Int,
    val uniqueArtists: Int,
    val uniqueTracks: Int,
    val topArtist: String,
    val headline: String,
    val recapJson: String
)