package com.recapped.app.data.repository

import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.Artist
import com.recapped.app.domain.model.ArtistDetail
import com.recapped.app.domain.model.SongDetail
import com.recapped.app.domain.model.TopSong
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {

    fun getTopArtists(): Flow<Resource<List<Artist>>>

    fun getUserTopArtists(
        username: String,
        period: String
    ): Flow<Resource<List<Artist>>>

    suspend fun getUserTopSongs(
        username: String,
        period: String,
        limit: Int = 15
    ): Resource<List<TopSong>>

    suspend fun searchArtists(
        query: String,
        limit: Int = 10
    ): Resource<List<Artist>>

    suspend fun searchSongs(
        query: String,
        limit: Int = 10
    ): Resource<List<TopSong>>

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
