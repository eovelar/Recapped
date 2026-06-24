package com.recapped.app.domain.model

data class TopSong(
    val rank: Int,
    val name: String,
    val artistName: String,
    val playcount: Long,
    val imageUrl: String?
)