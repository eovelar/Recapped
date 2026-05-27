package com.recapped.app.data.repository

import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.Artist
import com.recapped.app.domain.model.ArtistDetail
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {
    fun getTopArtists(): Flow<Resource<List<Artist>>>

    fun getUserTopArtists(
        username: String,
        period: String
    ): Flow<Resource<List<Artist>>>

    fun getArtistDetail(name: String): Flow<Resource<ArtistDetail>>
}