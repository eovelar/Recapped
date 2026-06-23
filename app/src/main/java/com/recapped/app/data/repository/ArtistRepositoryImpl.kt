package com.recapped.app.data.repository

import com.recapped.app.BuildConfig
import com.recapped.app.data.local.dao.ArtistDao
import com.recapped.app.data.local.entity.toDomain
import com.recapped.app.data.local.entity.toEntity
import com.recapped.app.data.remote.DeezerApi
import com.recapped.app.data.remote.LastFmApi
import com.recapped.app.data.remote.dto.ArtistDto
import com.recapped.app.data.remote.dto.ImageDto
import com.recapped.app.data.remote.dto.TrackDto
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.AlbumTrack
import com.recapped.app.domain.model.Artist
import com.recapped.app.domain.model.ArtistDetail
import com.recapped.app.domain.model.SongDetail
import com.recapped.app.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistRepositoryImpl @Inject constructor(
    private val api: LastFmApi,
    private val deezerApi: DeezerApi,
    private val artistDao: ArtistDao
) : ArtistRepository {

    private val apiKey get() = BuildConfig.LASTFM_API_KEY

    override fun getTopArtists(): Flow<Resource<List<Artist>>> = flow {
        emit(Resource.Loading)

        val response = api.getTopArtists(
            apiKey = apiKey,
            limit = 50
        )

        val list = mapArtistsWithImages(response.artists.artist)
        emit(Resource.Success(list))
    }
        .catch { error -> emit(mapError(error)) }
        .flowOn(Dispatchers.IO)

    override fun getUserTopArtists(
        username: String,
        period: String
    ): Flow<Resource<List<Artist>>> = flow {
        emit(Resource.Loading)

        val cleanUsername = username.trim()

        if (cleanUsername.isBlank()) {
            emit(Resource.Error("No hay usuario de Last.fm vinculado."))
            return@flow
        }

        val cachedArtists = artistDao
            .observeArtists(cleanUsername, period)
            .first()
            .map { it.toDomain() }

        if (cachedArtists.isNotEmpty()) {
            emit(Resource.Success(cachedArtists))
        }

        try {
            val response = api.getUserTopArtists(
                user = cleanUsername,
                period = period,
                apiKey = apiKey,
                limit = 12
            )

            val remoteArtists = mapArtistsWithImages(
                response.topArtists.artist
            )

            artistDao.replaceArtists(
                username = cleanUsername,
                period = period,
                artists = remoteArtists.map { artist ->
                    artist.toEntity(
                        username = cleanUsername,
                        period = period
                    )
                }
            )

            val updatedArtists = artistDao
                .observeArtists(cleanUsername, period)
                .first()
                .map { it.toDomain() }

            emit(Resource.Success(updatedArtists))
        } catch (error: Exception) {
            if (cachedArtists.isEmpty()) {
                emit(mapError(error))
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun validateLastFmUsername(
        username: String
    ): Resource<Unit> {
        return try {
            val cleanUsername = username.trim()

            if (cleanUsername.isBlank()) {
                return Resource.Error("Ingresá tu usuario de Last.fm.")
            }

            api.getUserTopArtists(
                user = cleanUsername,
                period = "1month",
                apiKey = apiKey,
                limit = 1
            )

            Resource.Success(Unit)
        } catch (error: Exception) {
            mapError(error)
        }
    }

    override suspend fun getSongDetail(
        artistName: String,
        trackName: String
    ): Resource<SongDetail> {
        return try {
            val searchResponse = deezerApi.searchTrack(
                "$artistName $trackName"
            )

            val deezerTrack = searchResponse.data.firstOrNull { track ->
                track.title.equals(trackName, ignoreCase = true) &&
                        track.artist?.name.equals(
                            artistName,
                            ignoreCase = true
                        )
            } ?: searchResponse.data.firstOrNull()

            if (deezerTrack == null) {
                return Resource.Error(
                    "No encontramos información de esta canción."
                )
            }

            val deezerTrackId = deezerTrack.id
                ?: return Resource.Error(
                    "No encontramos el identificador de esta canción."
                )

            val trackDetail = deezerApi.getTrack(deezerTrackId)
            val albumId = trackDetail.album?.id
                ?: deezerTrack.album?.id

            if (albumId == null) {
                return Resource.Error(
                    "No encontramos información del álbum."
                )
            }

            val albumDetail = deezerApi.getAlbum(albumId)

            val albumTracks = albumDetail.tracks
                ?.data
                .orEmpty()
                .mapNotNull { track ->
                    val name = track.title
                        ?.takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null
                    val trackId = track.id
                        ?: return@mapNotNull null

                    AlbumTrack(
                        name = name,
                        durationSeconds = track.duration ?: 0,
                        deezerTrackId = trackId
                    )
                }

            Resource.Success(
                SongDetail(
                    name = trackDetail.title
                        ?: deezerTrack.title
                        ?: trackName,
                    artistName = trackDetail.artist?.name
                        ?: deezerTrack.artist?.name
                        ?: artistName,
                    albumTitle = albumDetail.title
                        ?: trackDetail.album?.title
                        ?: deezerTrack.album?.title
                        ?: "Álbum no disponible",
                    imageUrl = albumDetail.coverXl
                        ?: albumDetail.coverBig
                        ?: albumDetail.coverMedium
                        ?: albumDetail.cover
                        ?: trackDetail.album?.coverXl
                        ?: trackDetail.album?.coverBig
                        ?: trackDetail.album?.coverMedium
                        ?: trackDetail.album?.cover
                        ?: deezerTrack.album?.coverXl
                        ?: deezerTrack.album?.coverBig
                        ?: deezerTrack.album?.coverMedium
                        ?: deezerTrack.album?.cover,
                    releaseDate = albumDetail.releaseDate,
                    trackCount = albumDetail.trackCount
                        ?: albumTracks.size,
                    albumTracks = albumTracks,
                    deezerTrackId = deezerTrackId,
                    isrc = trackDetail.isrc
                        ?.takeIf { it.isNotBlank() }
                )
            )
        } catch (error: Exception) {
            mapError(error)
        }
    }

    override suspend fun getTrackIsrc(
        deezerTrackId: Long
    ): Resource<String> {
        return try {
            val isrc = deezerApi.getTrack(deezerTrackId)
                .isrc
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: return Resource.Error(
                    "No encontramos el código ISRC de esta canción."
                )

            Resource.Success(isrc)
        } catch (error: Exception) {
            mapError(error)
        }
    }

    override fun getArtistDetail(
        name: String
    ): Flow<Resource<ArtistDetail>> = flow {
        emit(Resource.Loading)

        val detail = coroutineScope {
            val infoDeferred = async {
                api.getArtistInfo(
                    artist = name,
                    apiKey = apiKey
                )
            }

            val tracksDeferred = async {
                api.getArtistTopTracks(
                    artist = name,
                    apiKey = apiKey
                )
            }

            val info = infoDeferred.await().artist
            val tracks = tracksDeferred.await().topTracks.track
            val lastFmImage = pickImage(info.image)

            val finalImage =
                if (
                    lastFmImage.isNullOrBlank() ||
                    isLastFmPlaceholder(lastFmImage)
                ) {
                    getDeezerArtistImage(info.name)
                } else {
                    lastFmImage
                }

            val artist = Artist(
                mbid = info.mbid.orEmpty(),
                name = info.name,
                playcount = info.stats?.playcount?.toLongOrNull() ?: 0L,
                listeners = info.stats?.listeners?.toLongOrNull() ?: 0L,
                imageUrl = finalImage,
                rank = 0
            )

            val topTracks = tracks.mapIndexed { index, track ->
                val lastFmTrackImage = pickImage(track.image)

                val shouldSearchDeezerCover =
                    index < 10 &&
                            (
                                    lastFmTrackImage.isNullOrBlank() ||
                                            isLastFmPlaceholder(lastFmTrackImage)
                                    )

                val finalTrackImage = if (shouldSearchDeezerCover) {
                    getDeezerTrackCover(
                        artistName = info.name,
                        trackName = track.name
                    )
                } else {
                    lastFmTrackImage
                }

                track.toDomain(
                    imageUrlOverride = finalTrackImage
                )
            }

            ArtistDetail(
                artist = artist,
                bio = info.bio?.summary
                    ?.replace(Regex("<a [^>]+>.*?</a>"), "")
                    ?.trim(),
                tags = info.tags?.tag?.map { it.name }.orEmpty(),
                topTracks = topTracks
            )
        }

        emit(Resource.Success(detail))
    }
        .catch { error -> emit(mapError(error)) }
        .flowOn(Dispatchers.IO)

    private suspend fun mapArtistsWithImages(
        artists: List<ArtistDto>
    ): List<Artist> {
        return artists.mapIndexed { index, dto ->
            val lastFmImage = pickImage(dto.image)

            val shouldSearchDeezer =
                index < 10 &&
                        (
                                lastFmImage.isNullOrBlank() ||
                                        isLastFmPlaceholder(lastFmImage)
                                )

            val finalImage = if (shouldSearchDeezer) {
                getDeezerArtistImage(dto.name)
            } else {
                lastFmImage
            }

            dto.toDomain(
                rank = index + 1,
                imageUrlOverride = finalImage
            )
        }
    }

    private fun mapError(error: Throwable): Resource.Error {
        return when (error) {
            is IOException -> {
                Resource.Error("No hay conexión a Internet", error)
            }

            is HttpException -> {
                when (error.code()) {
                    404 -> Resource.Error(
                        "No encontramos ese usuario de Last.fm. Revisá que esté escrito correctamente.",
                        error
                    )

                    403 -> Resource.Error(
                        "No se pudo acceder a ese perfil de Last.fm.",
                        error
                    )

                    else -> Resource.Error(
                        "Error de Last.fm (${error.code()}). Intentá nuevamente.",
                        error
                    )
                }
            }

            else -> {
                Resource.Error(
                    error.message ?: "Error inesperado",
                    error
                )
            }
        }
    }

    private fun ArtistDto.toDomain(
        rank: Int,
        imageUrlOverride: String? = null
    ) = Artist(
        mbid = mbid.orEmpty(),
        name = name,
        playcount = playcount?.toLongOrNull() ?: 0L,
        listeners = listeners?.toLongOrNull() ?: 0L,
        imageUrl = imageUrlOverride ?: pickImage(image),
        rank = rank
    )

    private fun TrackDto.toDomain(
        imageUrlOverride: String? = null
    ) = Track(
        name = name,
        playcount = playcount?.toLongOrNull() ?: 0L,
        imageUrl = imageUrlOverride ?: pickImage(image)
    )

    private fun pickImage(images: List<ImageDto>?): String? {
        val validImages = images
            .orEmpty()
            .filter { !it.url.isNullOrBlank() }

        return validImages.firstOrNull { it.size == "mega" }?.url
            ?: validImages.firstOrNull {
                it.size == "extralarge"
            }?.url
            ?: validImages.firstOrNull { it.size == "large" }?.url
            ?: validImages.firstOrNull { it.size == "medium" }?.url
            ?: validImages.firstOrNull { it.size == "small" }?.url
            ?: validImages.firstOrNull()?.url
    }

    private fun isLastFmPlaceholder(url: String): Boolean {
        return url.contains("2a96cbd8b46e442fc41c2b86b821562f")
    }

    private suspend fun getDeezerArtistImage(
        artistName: String
    ): String? {
        return try {
            val response = deezerApi.searchArtist(artistName)

            val artist = response.data.firstOrNull {
                it.name.equals(artistName, ignoreCase = true)
            } ?: response.data.firstOrNull()

            artist?.pictureXl
                ?: artist?.pictureBig
                ?: artist?.pictureMedium
                ?: artist?.picture
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getDeezerTrackCover(
        artistName: String,
        trackName: String
    ): String? {
        return try {
            val response = deezerApi.searchTrack(
                "$artistName $trackName"
            )

            val track = response.data.firstOrNull {
                it.title.equals(trackName, ignoreCase = true) &&
                        it.artist?.name.equals(
                            artistName,
                            ignoreCase = true
                        )
            } ?: response.data.firstOrNull()

            track?.album?.coverXl
                ?: track?.album?.coverBig
                ?: track?.album?.coverMedium
                ?: track?.album?.cover
        } catch (_: Exception) {
            null
        }
    }
}
