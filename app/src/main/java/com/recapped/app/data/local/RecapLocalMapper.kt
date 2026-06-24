package com.recapped.app.data.local

import com.recapped.app.data.local.entity.RecapEntity
import com.recapped.app.domain.model.RecapResult
import com.squareup.moshi.Moshi
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecapLocalMapper @Inject constructor(
    moshi: Moshi
) {
    private val adapter = moshi.adapter(RecapResult::class.java)

    fun toEntity(
        recap: RecapResult,
        userId: String
    ): RecapEntity {
        return RecapEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            username = recap.username,
            period = recap.period.key,
            generatedAt = recap.generatedAt,
            totalScrobbles = recap.totalScrobbles,
            uniqueArtists = recap.uniqueArtists,
            uniqueTracks = recap.uniqueTracks,
            topArtist = recap.topArtists
                .firstOrNull()
                ?.name
                .orEmpty(),
            headline = recap.aiHeadline,
            recapJson = adapter.toJson(recap)
        )
    }

    fun toDomain(entity: RecapEntity): RecapResult? {
        return runCatching {
            adapter.fromJson(entity.recapJson)
        }.getOrNull()
    }
}