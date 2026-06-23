package com.recapped.app.data.repository

import com.recapped.app.BuildConfig
import com.recapped.app.data.remote.DeezerApi
import com.recapped.app.data.remote.LastFmApi
import com.recapped.app.data.remote.dto.DeezerTrackDto
import com.recapped.app.data.remote.dto.ImageDto
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.RecapArtist
import com.recapped.app.domain.model.RecapGenre
import com.recapped.app.domain.model.RecapPeriod
import com.recapped.app.domain.model.RecapRecommendation
import com.recapped.app.domain.model.RecapResult
import com.recapped.app.domain.model.RecapTrack
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class RecapRepository @Inject constructor(
    private val lastFmApi: LastFmApi,
    private val deezerApi: DeezerApi,
    private val userProfileRepository: UserProfileRepository,
    private val aiRecapRepository: AiRecapRepository
) {

    suspend fun generateRecap(
        period: RecapPeriod
    ): Resource<RecapResult> {
        return try {
            val username = userProfileRepository
                .getLastFmUsername()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: return Resource.Error(
                    "Primero vinculá tu usuario de Last.fm."
                )

            val now = Instant.now().epochSecond
            val from = now - (period.days * SECONDS_PER_DAY)

            val data = coroutineScope {
                val artistsDeferred = async {
                    lastFmApi.getUserTopArtists(
                        user = username,
                        period = period.lastFmValue,
                        limit = TOP_ARTISTS_LIMIT,
                        apiKey = BuildConfig.LASTFM_API_KEY
                    )
                }

                val tracksDeferred = async {
                    lastFmApi.getUserTopTracks(
                        user = username,
                        period = period.lastFmValue,
                        limit = TOP_TRACKS_LIMIT,
                        apiKey = BuildConfig.LASTFM_API_KEY
                    )
                }

                val recentTracksDeferred = async {
                    lastFmApi.getUserRecentTracks(
                        user = username,
                        from = from,
                        to = now,
                        limit = 1,
                        apiKey = BuildConfig.LASTFM_API_KEY
                    )
                }

                Triple(
                    artistsDeferred.await(),
                    tracksDeferred.await(),
                    recentTracksDeferred.await()
                )
            }

            val artistsResponse = data.first
            val tracksResponse = data.second
            val recentTracksResponse = data.third

            val totalScrobbles = recentTracksResponse
                .recentTracks
                .attr
                ?.total
                ?.toIntOrNull()
                ?: 0

            val uniqueArtists = artistsResponse
                .topArtists
                .attr
                ?.total
                ?.toIntOrNull()
                ?: artistsResponse.topArtists.artist.size

            val uniqueTracks = tracksResponse
                .topTracks
                .attr
                ?.total
                ?.toIntOrNull()
                ?: tracksResponse.topTracks.track.size

            if (
                artistsResponse.topArtists.artist.isEmpty() &&
                tracksResponse.topTracks.track.isEmpty()
            ) {
                return Resource.Error(
                    "No encontramos reproducciones para este período."
                )
            }

            val topArtists = buildTopArtists(
                artistsResponse.topArtists.artist
                    .take(DISPLAYED_ARTISTS)
            )

            val topTracks = coroutineScope {
                tracksResponse
                    .topTracks
                    .track
                    .take(DISPLAYED_TRACKS)
                    .mapIndexed { index, track ->
                        async {
                            val artistName = track.artist
                                ?.name
                                ?.takeIf { it.isNotBlank() }
                                ?: "Artista desconocido"

                            val lastFmImage = pickImage(track.image)

                            val imageUrl = if (
                                lastFmImage.isNullOrBlank() ||
                                isLastFmPlaceholder(lastFmImage)
                            ) {
                                getDeezerTrackImage(
                                    artistName = artistName,
                                    trackName = track.name
                                )
                            } else {
                                lastFmImage
                            }

                            RecapTrack(
                                rank = index + 1,
                                name = track.name,
                                artistName = artistName,
                                playcount = track.playcount
                                    ?.toIntOrNull()
                                    ?: 0,
                                imageUrl = imageUrl
                            )
                        }
                    }
                    .awaitAll()
            }

            val genres = calculateGenres(topArtists)

            when (
                val aiResult = aiRecapRepository.generateRecapContent(
                    period = period,
                    totalScrobbles = totalScrobbles,
                    uniqueArtists = uniqueArtists,
                    uniqueTracks = uniqueTracks,
                    topArtists = topArtists,
                    topTracks = topTracks,
                    genres = genres
                )
            ) {
                is Resource.Success -> {
                    val recommendations = coroutineScope {
                        aiResult.data.recommendations.map { recommendation ->
                            async {
                                RecapRecommendation(
                                    name = recommendation.name,
                                    genre = recommendation.genre,
                                    reason = recommendation.reason,
                                    imageUrl = getDeezerArtistImage(
                                        recommendation.name
                                    )
                                )
                            }
                        }.awaitAll()
                    }

                    Resource.Success(
                        RecapResult(
                            username = username,
                            period = period,
                            generatedAt = System.currentTimeMillis(),
                            totalScrobbles = totalScrobbles,
                            uniqueArtists = uniqueArtists,
                            uniqueTracks = uniqueTracks,
                            topArtists = topArtists,
                            topTracks = topTracks,
                            genres = genres,
                            aiHeadline = aiResult.data.headline,
                            aiSummary = aiResult.data.summary,
                            recommendations = recommendations
                        )
                    )
                }

                is Resource.Error -> {
                    Resource.Error(
                        aiResult.message,
                        aiResult.cause
                    )
                }

                Resource.Loading -> {
                    Resource.Error(
                        "No pudimos generar el análisis del recap."
                    )
                }
            }
        } catch (error: Exception) {
            Resource.Error(
                error.message
                    ?: "No pudimos generar el recap.",
                error
            )
        }
    }

    private suspend fun buildTopArtists(
        artists: List<com.recapped.app.data.remote.dto.ArtistDto>
    ): List<RecapArtist> {
        return coroutineScope {
            artists.mapIndexed { index, artist ->
                async {
                    val artistInfo = try {
                        lastFmApi.getArtistInfo(
                            artist = artist.name,
                            apiKey = BuildConfig.LASTFM_API_KEY
                        ).artist
                    } catch (_: Exception) {
                        null
                    }

                    val lastFmImage = pickImage(artistInfo?.image)
                        ?: pickImage(artist.image)

                    val imageUrl = if (
                        lastFmImage.isNullOrBlank() ||
                        isLastFmPlaceholder(lastFmImage)
                    ) {
                        getDeezerArtistImage(artist.name)
                    } else {
                        lastFmImage
                    }

                    RecapArtist(
                        rank = index + 1,
                        name = artist.name,
                        playcount = artist.playcount
                            ?.toIntOrNull()
                            ?: 0,
                        imageUrl = imageUrl,
                        tags = artistInfo
                            ?.tags
                            ?.tag
                            .orEmpty()
                            .map { it.name.trim() }
                            .filter { it.isNotBlank() }
                            .take(TAGS_PER_ARTIST)
                    )
                }
            }.awaitAll()
        }
    }

    private fun calculateGenres(
        artists: List<RecapArtist>
    ): List<RecapGenre> {
        val genreWeights = mutableMapOf<String, Int>()
        val displayNames = mutableMapOf<String, String>()

        artists.forEach { artist ->
            val weight = artist.playcount.coerceAtLeast(1)

            artist.tags.forEach { tag ->
                val cleanTag = tag.trim()
                val normalizedTag = cleanTag.lowercase()

                if (
                    cleanTag.isNotBlank() &&
                    normalizedTag !in IGNORED_TAGS
                ) {
                    genreWeights[normalizedTag] =
                        genreWeights.getOrDefault(
                            normalizedTag,
                            0
                        ) + weight

                    displayNames.putIfAbsent(
                        normalizedTag,
                        cleanTag.replaceFirstChar {
                            it.uppercase()
                        }
                    )
                }
            }
        }

        if (genreWeights.isEmpty()) {
            return listOf(
                RecapGenre(
                    name = "Música variada",
                    percentage = 100
                )
            )
        }

        val selectedGenres = genreWeights.entries
            .sortedByDescending { it.value }
            .take(DISPLAYED_GENRES)

        val totalWeight = selectedGenres.sumOf { it.value }
            .coerceAtLeast(1)

        return selectedGenres.map { entry ->
            RecapGenre(
                name = displayNames[entry.key]
                    ?: entry.key.replaceFirstChar {
                        it.uppercase()
                    },
                percentage = (
                        entry.value.toDouble() /
                                totalWeight.toDouble() *
                                100.0
                        ).roundToInt()
            )
        }
    }

    private suspend fun getDeezerArtistImage(
        artistName: String
    ): String? {
        return try {
            val response = deezerApi.searchArtist(artistName)

            val artist = response.data.firstOrNull {
                it.name.equals(
                    artistName,
                    ignoreCase = true
                )
            } ?: response.data.firstOrNull()

            artist?.pictureXl
                ?: artist?.pictureBig
                ?: artist?.pictureMedium
                ?: artist?.picture
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getDeezerTrackImage(
        artistName: String,
        trackName: String
    ): String? {
        val queries = listOf(
            """artist:"$artistName" track:"$trackName"""",
            "$trackName $artistName",
            "$artistName $trackName"
        )

        for (query in queries) {
            val imageUrl = try {
                val response = deezerApi.searchTrack(query)
                val track = pickBestDeezerTrack(
                    tracks = response.data,
                    artistName = artistName,
                    trackName = trackName
                )

                track?.album?.coverXl
                    ?: track?.album?.coverBig
                    ?: track?.album?.coverMedium
                    ?: track?.album?.cover
            } catch (_: Exception) {
                null
            }

            if (!imageUrl.isNullOrBlank()) {
                return imageUrl
            }
        }

        return null
    }

    private fun pickBestDeezerTrack(
        tracks: List<DeezerTrackDto>,
        artistName: String,
        trackName: String
    ): DeezerTrackDto? {
        val normalizedArtist = normalizeMusicText(artistName)
        val normalizedTrack = normalizeMusicText(trackName)

        return tracks.firstOrNull { track ->
            normalizeMusicText(track.title.orEmpty()) == normalizedTrack &&
                    normalizeMusicText(track.artist?.name.orEmpty()) == normalizedArtist
        } ?: tracks.firstOrNull { track ->
            normalizeMusicText(track.title.orEmpty()).contains(normalizedTrack) &&
                    normalizeMusicText(track.artist?.name.orEmpty()).contains(normalizedArtist)
        } ?: tracks.firstOrNull { track ->
            normalizeMusicText(track.title.orEmpty()) == normalizedTrack
        } ?: tracks.firstOrNull()
    }

    private fun pickImage(
        images: List<ImageDto>?
    ): String? {
        val validImages = images
            .orEmpty()
            .filter {
                !it.url.isNullOrBlank()
            }

        return validImages.firstOrNull {
            it.size == "mega"
        }?.url
            ?: validImages.firstOrNull {
                it.size == "extralarge"
            }?.url
            ?: validImages.firstOrNull {
                it.size == "large"
            }?.url
            ?: validImages.firstOrNull {
                it.size == "medium"
            }?.url
            ?: validImages.firstOrNull()?.url
    }

    private fun normalizeMusicText(
        value: String
    ): String {
        return value
            .lowercase()
            .replace("’", "'")
            .replace("&", "and")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun isLastFmPlaceholder(
        url: String
    ): Boolean {
        return url.contains(
            "2a96cbd8b46e442fc41c2b86b821562f"
        )
    }

    private companion object {
        const val SECONDS_PER_DAY = 86_400L
        const val TOP_ARTISTS_LIMIT = 20
        const val TOP_TRACKS_LIMIT = 20
        const val DISPLAYED_ARTISTS = 4
        const val DISPLAYED_TRACKS = 5
        const val DISPLAYED_GENRES = 5
        const val TAGS_PER_ARTIST = 3

        val IGNORED_TAGS = setOf(
            "seen live",
            "favorites",
            "favourite",
            "favorite",
            "spotify",
            "albums i own",
            "under 2000 listeners"
        )
    }
}