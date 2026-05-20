package com.recapped.app.data.repository

import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.Artist
import com.recapped.app.domain.model.ArtistDetail
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del Repository. La capa UI/ViewModel sólo conoce esta interfaz,
 * lo que permite swappear implementaciones (red, mock, Room) sin tocar
 * el resto del código. Es la espina dorsal del Repository Pattern.
 */
interface ArtistRepository {
    fun getTopArtists(): Flow<Resource<List<Artist>>>
    fun getArtistDetail(name: String): Flow<Resource<ArtistDetail>>
}
