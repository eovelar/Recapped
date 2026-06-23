package com.recapped.app.data.repository

import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.Artist
import com.recapped.app.domain.model.ArtistDetail
import com.recapped.app.domain.model.SongDetail
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {

    fun getTopArtists(): Flow<Resource<List<Artist>>>

    fun getUserTopArtists(
        username: String,
        period: String
    ): Flow<Resource<List<Artist>>>

    fun getArtistDetail(
        name: String
    ): Flow<Resource<ArtistDetail>>

    suspend fun getSongDetail(
        artistName: String,
        trackName: String
    ): Resource<SongDetail>

    suspend fun getTrackIsrc(
        deezerTrackId: Long
    ): Resource<String>

    suspend fun validateLastFmUsername(
        username: String
    ): Resource<Unit>
}