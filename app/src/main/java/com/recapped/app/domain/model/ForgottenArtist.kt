package com.recapped.app.domain.model

data class ForgottenArtist(
    val name: String,
    val imageUrl: String?,
    val lastListenedAt: Long,
    val daysSinceLastListen: Long
)