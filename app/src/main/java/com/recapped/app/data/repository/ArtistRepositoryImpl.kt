package com.recapped.app.data.repository

import com.recapped.app.BuildConfig
import com.recapped.app.data.remote.DeezerApi
import com.recapped.app.data.remote.LastFmApi
import com.recapped.app.data.remote.dto.ArtistDto
import com.recapped.app.data.remote.dto.ImageDto
import com.recapped.app.data.remote.dto.TrackDto
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.Artist
import com.recapped.app.domain.model.ArtistDetail
import com.recapped.app.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistRepositoryImpl @Inject constructor(
    private val api: LastFmApi,
    private val deezerApi: DeezerApi
) : ArtistRepository {

    private val apiKey get() = BuildConfig.LASTFM_API_KEY

    override fun getTopArtists(): Flow<Resource<List<Artist>>> = flow {
        emit(Resource.Loading)

        val response = api.getTopArtists(
            apiKey = apiKey,
            limit = 50
        )

        val list = mapArtistsWithImages(
            artists = response.artists.artist
        )

        emit(Resource.Success(list))
    }
        .catch { e -> emit(mapError(e)) }
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

        val response = api.getUserTopArtists(
            user = cleanUsername,
            period = period,
            apiKey = apiKey,
            limit = 12
        )

        val list = mapArtistsWithImages(
            artists = response.topArtists.artist
        )

        emit(Resource.Success(list))
    }
        .catch { e -> emit(mapError(e)) }
        .flowOn(Dispatchers.IO)

    override suspend fun validateLastFmUsername(username: String): Resource<Unit> {
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
        } catch (e: Exception) {
            mapError(e)
        }
    }

    override fun getArtistDetail(name: String): Flow<Resource<ArtistDetail>> = flow {
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

            val finalImage = if (lastFmImage.isNullOrBlank() || isLastFmPlaceholder(lastFmImage)) {
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

            val topTracks = tracks.mapIndexed { idx, track ->
                val lastFmTrackImage = pickImage(track.image)

                val shouldSearchDeezerCover = idx < 10 &&
                        (lastFmTrackImage.isNullOrBlank() || isLastFmPlaceholder(lastFmTrackImage))

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
        .catch { e -> emit(mapError(e)) }
        .flowOn(Dispatchers.IO)

    private suspend fun mapArtistsWithImages(
        artists: List<ArtistDto>
    ): List<Artist> {
        return artists.mapIndexed { idx, dto ->
            val lastFmImage = pickImage(dto.image)

            val shouldSearchDeezer = idx < 6 &&
                    (lastFmImage.isNullOrBlank() || isLastFmPlaceholder(lastFmImage))

            val finalImage = if (shouldSearchDeezer) {
                getDeezerArtistImage(dto.name)
            } else {
                lastFmImage
            }

            dto.toDomain(
                rank = idx + 1,
                imageUrlOverride = finalImage
            )
        }
    }

    private fun mapError(e: Throwable): Resource.Error = when (e) {
        is IOException -> {
            Resource.Error("No hay conexión a Internet", e)
        }

        is HttpException -> {
            when (e.code()) {
                404 -> Resource.Error(
                    "No encontramos ese usuario de Last.fm. Revisá que esté escrito correctamente.",
                    e
                )

                403 -> Resource.Error(
                    "No se pudo acceder a ese perfil de Last.fm.",
                    e
                )

                else -> Resource.Error(
                    "Error de Last.fm (${e.code()}). Intentá nuevamente.",
                    e
                )
            }
        }

        else -> {
            Resource.Error(e.message ?: "Error inesperado", e)
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
            ?: validImages.firstOrNull { it.size == "extralarge" }?.url
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

            val artist = response.data.firstOrNull { deezerArtist ->
                deezerArtist.name.equals(artistName, ignoreCase = true)
            } ?: response.data.firstOrNull()

            artist?.pictureXl
                ?: artist?.pictureBig
                ?: artist?.pictureMedium
                ?: artist?.picture
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getDeezerTrackCover(
        artistName: String,
        trackName: String
    ): String? {
        return try {
            val query = "$artistName $trackName"
            val response = deezerApi.searchTrack(query)

            val track = response.data.firstOrNull { deezerTrack ->
                deezerTrack.title.equals(trackName, ignoreCase = true) &&
                        deezerTrack.artist?.name.equals(artistName, ignoreCase = true)
            } ?: response.data.firstOrNull()

            track?.album?.coverXl
                ?: track?.album?.coverBig
                ?: track?.album?.coverMedium
                ?: track?.album?.cover
        } catch (e: Exception) {
            null
        }
    }
}