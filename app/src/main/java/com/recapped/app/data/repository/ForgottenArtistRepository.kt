package com.recapped.app.data.repository

import com.recapped.app.BuildConfig
import com.recapped.app.data.remote.DeezerApi
import com.recapped.app.data.remote.LastFmApi
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.ForgottenArtist
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class ForgottenArtistRepository @Inject constructor(
    private val lastFmApi: LastFmApi,
    private val deezerApi: DeezerApi
) {

    private val cacheMutex = Mutex()
    private var cachedResult: CachedForgottenArtist? = null

    suspend fun getForgottenArtist(
        username: String
    ): Resource<ForgottenArtist?> {
        val cleanUsername = username.trim()

        if (cleanUsername.isBlank()) {
            return Resource.Error(
                "No hay una cuenta de Last.fm vinculada."
            )
        }

        return cacheMutex.withLock {
            getValidCache(cleanUsername)?.let { artist ->
                return@withLock Resource.Success(artist)
            }

            try {
                val artist = loadForgottenArtist(cleanUsername)

                cachedResult = CachedForgottenArtist(
                    username = cleanUsername,
                    artist = artist,
                    expiresAt = System.currentTimeMillis() +
                            CACHE_DURATION_MILLIS
                )

                Resource.Success(artist)
            } catch (error: Exception) {
                Resource.Error(
                    error.message
                        ?: "No pudimos buscar artistas olvidados.",
                    error
                )
            }
        }
    }

    private suspend fun loadForgottenArtist(
        username: String
    ): ForgottenArtist? = coroutineScope {
        val nowSeconds = System.currentTimeMillis() / 1000
        val thirtyDaysAgo = nowSeconds -
                TimeUnit.DAYS.toSeconds(30)
        val oneYearAgo = nowSeconds -
                TimeUnit.DAYS.toSeconds(365)

        val yearlyArtistsDeferred = async {
            lastFmApi.getUserTopArtists(
                user = username,
                period = "12month",
                limit = 100,
                apiKey = BuildConfig.LASTFM_API_KEY
            ).topArtists.artist
        }

        val recentArtistsDeferred = async {
            lastFmApi.getUserTopArtists(
                user = username,
                period = "1month",
                limit = 1000,
                apiKey = BuildConfig.LASTFM_API_KEY
            ).topArtists.artist
        }

        val oldTracksDeferred = async {
            lastFmApi.getUserRecentTracks(
                user = username,
                from = oneYearAgo,
                to = thirtyDaysAgo,
                limit = 200,
                page = 1,
                apiKey = BuildConfig.LASTFM_API_KEY
            ).recentTracks.tracks
        }

        val yearlyArtists = yearlyArtistsDeferred.await()

        if (yearlyArtists.isEmpty()) {
            return@coroutineScope null
        }

        val recentArtistNames = recentArtistsDeferred
            .await()
            .map { artist ->
                artist.name.trim().lowercase()
            }
            .toSet()

        val candidateNames = yearlyArtists
            .asSequence()
            .map { artist ->
                artist.name.trim().lowercase()
            }
            .filter { artistName ->
                artistName !in recentArtistNames
            }
            .toSet()

        if (candidateNames.isEmpty()) {
            return@coroutineScope null
        }

        val lastForgottenTrack = oldTracksDeferred
            .await()
            .firstOrNull { track ->
                track.date?.unixTimestamp != null &&
                        track.artist.name
                            .trim()
                            .lowercase() in candidateNames
            }
            ?: return@coroutineScope null

        val artistName = lastForgottenTrack.artist.name

        val lastListenedSeconds = lastForgottenTrack.date
            ?.unixTimestamp
            ?.toLongOrNull()
            ?: return@coroutineScope null

        val daysSinceLastListen = TimeUnit.SECONDS.toDays(
            nowSeconds - lastListenedSeconds
        )

        ForgottenArtist(
            name = artistName,
            imageUrl = getArtistImage(artistName),
            lastListenedAt = lastListenedSeconds * 1000,
            daysSinceLastListen = daysSinceLastListen
        )
    }

    private fun getValidCache(
        username: String
    ): ForgottenArtist? {
        val cache = cachedResult ?: return null

        if (
            !cache.username.equals(username, ignoreCase = true) ||
            System.currentTimeMillis() >= cache.expiresAt
        ) {
            cachedResult = null
            return null
        }

        return cache.artist
    }

    private suspend fun getArtistImage(
        artistName: String
    ): String? {
        return runCatching {
            val artists = deezerApi
                .searchArtist(artistName)
                .data

            val artist = artists.firstOrNull {
                it.name.equals(
                    artistName,
                    ignoreCase = true
                )
            } ?: artists.firstOrNull()

            artist?.pictureXl
                ?: artist?.pictureBig
                ?: artist?.pictureMedium
                ?: artist?.picture
        }.getOrNull()
    }

    private data class CachedForgottenArtist(
        val username: String,
        val artist: ForgottenArtist?,
        val expiresAt: Long
    )

    private companion object {
        val CACHE_DURATION_MILLIS: Long =
            TimeUnit.MINUTES.toMillis(30)
    }
}