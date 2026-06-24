package com.recapped.app.data.repository

import com.recapped.app.BuildConfig
import com.recapped.app.data.remote.DeezerApi
import com.recapped.app.data.remote.LastFmApi
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.ForgottenArtist
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForgottenArtistRepository @Inject constructor(
    private val lastFmApi: LastFmApi,
    private val deezerApi: DeezerApi
) {

    suspend fun getForgottenArtist(
        username: String
    ): Resource<ForgottenArtist?> {
        return try {
            val cleanUsername = username.trim()

            if (cleanUsername.isBlank()) {
                return Resource.Error(
                    "No hay una cuenta de Last.fm vinculada."
                )
            }

            val nowSeconds = System.currentTimeMillis() / 1000
            val thirtyDaysAgo = nowSeconds -
                    TimeUnit.DAYS.toSeconds(30)
            val oneYearAgo = nowSeconds -
                    TimeUnit.DAYS.toSeconds(365)

            val yearlyArtists = lastFmApi.getUserTopArtists(
                user = cleanUsername,
                period = "12month",
                limit = 100,
                apiKey = BuildConfig.LASTFM_API_KEY
            ).topArtists.artist

            if (yearlyArtists.isEmpty()) {
                return Resource.Success(null)
            }

            val recentArtists = lastFmApi.getUserTopArtists(
                user = cleanUsername,
                period = "1month",
                limit = 1000,
                apiKey = BuildConfig.LASTFM_API_KEY
            ).topArtists.artist
                .map { it.name.lowercase() }
                .toSet()

            val candidates = yearlyArtists.filter { artist ->
                artist.name.lowercase() !in recentArtists
            }

            if (candidates.isEmpty()) {
                return Resource.Success(null)
            }

            val candidateNames = candidates
                .map { it.name.lowercase() }
                .toSet()

            val oldTracks = lastFmApi.getUserRecentTracks(
                user = cleanUsername,
                from = oneYearAgo,
                to = thirtyDaysAgo,
                limit = 200,
                page = 1,
                apiKey = BuildConfig.LASTFM_API_KEY
            ).recentTracks.tracks

            val lastForgottenTrack = oldTracks.firstOrNull { track ->
                track.date?.unixTimestamp != null &&
                        track.artist.name.lowercase() in candidateNames
            } ?: return Resource.Success(null)

            val artistName = lastForgottenTrack.artist.name
            val lastListenedSeconds = lastForgottenTrack.date
                ?.unixTimestamp
                ?.toLongOrNull()
                ?: return Resource.Success(null)

            val daysSinceLastListen = TimeUnit.SECONDS.toDays(
                nowSeconds - lastListenedSeconds
            )

            val imageUrl = getArtistImage(artistName)

            Resource.Success(
                ForgottenArtist(
                    name = artistName,
                    imageUrl = imageUrl,
                    lastListenedAt = lastListenedSeconds * 1000,
                    daysSinceLastListen = daysSinceLastListen
                )
            )
        } catch (error: Exception) {
            Resource.Error(
                error.message
                    ?: "No pudimos buscar artistas olvidados.",
                error
            )
        }
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
}