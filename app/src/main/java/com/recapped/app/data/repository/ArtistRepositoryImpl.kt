package com.recapped.app.data.repository

import com.recapped.app.BuildConfig
import com.recapped.app.data.remote.LastFmApi
import com.recapped.app.data.remote.dto.ArtistDto
import com.recapped.app.data.remote.dto.ImageDto
import com.recapped.app.data.remote.dto.TrackDto
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.Artist
import com.recapped.app.domain.model.ArtistDetail
import com.recapped.app.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del Repository.
 *
 * Para la 2ª entrega usamos Retrofit como única fuente de datos.
 * En la 3ª entrega se inyectará un Room DAO y este repo pasará a ser
 * offline-first: emit local → fetch remoto → upsert en Room → re-emit.
 */
@Singleton
class ArtistRepositoryImpl @Inject constructor(
    private val api: LastFmApi
) : ArtistRepository {

    private val apiKey get() = BuildConfig.LASTFM_API_KEY

    override fun getTopArtists(): Flow<Resource<List<Artist>>> = flow {
        emit(Resource.Loading)
        val response = api.getTopArtists(apiKey = apiKey, limit = 50)
        val list = response.artists.artist.mapIndexed { idx, dto -> dto.toDomain(idx + 1) }
        emit(Resource.Success(list))
    }
        .catch { e -> emit(mapError(e)) }
        .flowOn(Dispatchers.IO)

    override fun getArtistDetail(name: String): Flow<Resource<ArtistDetail>> = flow {
        emit(Resource.Loading)
        val detail = coroutineScope {
            // Pedidos en paralelo: info + top tracks
            val infoDeferred = async { api.getArtistInfo(artist = name, apiKey = apiKey) }
            val tracksDeferred = async { api.getArtistTopTracks(artist = name, apiKey = apiKey) }
            val info = infoDeferred.await().artist
            val tracks = tracksDeferred.await().topTracks.track

            val artist = Artist(
                mbid = info.mbid.orEmpty(),
                name = info.name,
                playcount = info.stats?.playcount?.toLongOrNull() ?: 0L,
                listeners = info.stats?.listeners?.toLongOrNull() ?: 0L,
                imageUrl = pickImage(info.image),
                rank = 0
            )
            ArtistDetail(
                artist = artist,
                bio = info.bio?.summary?.replace(Regex("<a [^>]+>.*?</a>"), "")?.trim(),
                tags = info.tags?.tag?.map { it.name }.orEmpty(),
                topTracks = tracks.map { it.toDomain() }
            )
        }
        emit(Resource.Success(detail))
    }
        .catch { e -> emit(mapError(e)) }
        .flowOn(Dispatchers.IO)

    private fun mapError(e: Throwable): Resource.Error = when (e) {
        is IOException -> Resource.Error("No hay conexión a Internet", e)
        else -> Resource.Error(e.message ?: "Error inesperado", e)
    }

    // ── Mappers DTO → Domain ──
    private fun ArtistDto.toDomain(rank: Int) = Artist(
        mbid = mbid.orEmpty(),
        name = name,
        playcount = playcount?.toLongOrNull() ?: 0L,
        listeners = listeners?.toLongOrNull() ?: 0L,
        imageUrl = pickImage(image),
        rank = rank
    )

    private fun TrackDto.toDomain() = Track(
        name = name,
        playcount = playcount?.toLongOrNull() ?: 0L,
        imageUrl = pickImage(image)
    )

    /**
     * Last.fm devuelve imágenes en varios tamaños. Elegimos "extralarge"
     * y caemos al primero disponible si no existe.
     */
    private fun pickImage(images: List<ImageDto>?): String? {
        if (images.isNullOrEmpty()) return null
        val pref = images.firstOrNull { it.size == "extralarge" && !it.url.isNullOrBlank() }
        val fallback = images.firstOrNull { !it.url.isNullOrBlank() }
        return (pref ?: fallback)?.url
    }
}
