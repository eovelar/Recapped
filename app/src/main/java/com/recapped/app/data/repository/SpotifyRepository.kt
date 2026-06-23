package com.recapped.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.recapped.app.BuildConfig
import com.recapped.app.data.remote.SpotifyAccountsApi
import com.recapped.app.data.remote.SpotifyApi
import com.recapped.app.domain.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SpotifyLinkResult {

    data class Success(
        val spotifyUrl: String
    ) : SpotifyLinkResult

    data class AuthorizationRequired(
        val authorizationUrl: String
    ) : SpotifyLinkResult

    data class Error(
        val message: String
    ) : SpotifyLinkResult
}

@Singleton
class SpotifyRepository @Inject constructor(
    private val spotifyApi: SpotifyApi,
    private val spotifyAccountsApi: SpotifyAccountsApi,
    @ApplicationContext context: Context
) {

    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    suspend fun getTrackUrlByIsrc(
        isrc: String
    ): SpotifyLinkResult {
        val accessToken = getValidAccessToken()
            ?: return SpotifyLinkResult.AuthorizationRequired(
                createAuthorizationUrl()
            )

        return searchTrack(
            isrc = isrc,
            accessToken = accessToken,
            retryAfterUnauthorized = true
        )
    }

    suspend fun completeAuthorization(
        callbackUrl: String
    ): Resource<Unit> {
        return try {
            val callbackUri = Uri.parse(callbackUrl)
            val error = callbackUri.getQueryParameter("error")

            if (!error.isNullOrBlank()) {
                clearPendingAuthorization()

                return Resource.Error(
                    "No se autorizó el acceso a Spotify."
                )
            }

            val code = callbackUri.getQueryParameter("code")
                ?: return Resource.Error(
                    "Spotify no devolvió el código de autorización."
                )

            val returnedState = callbackUri.getQueryParameter("state")
            val savedState = preferences.getString(KEY_AUTH_STATE, null)
            val codeVerifier = preferences.getString(
                KEY_CODE_VERIFIER,
                null
            )

            if (
                returnedState.isNullOrBlank() ||
                savedState.isNullOrBlank() ||
                returnedState != savedState
            ) {
                clearPendingAuthorization()

                return Resource.Error(
                    "La respuesta de autorización de Spotify no es válida."
                )
            }

            if (codeVerifier.isNullOrBlank()) {
                clearPendingAuthorization()

                return Resource.Error(
                    "No encontramos la autorización pendiente de Spotify."
                )
            }

            val tokenResponse = spotifyAccountsApi.requestAccessToken(
                code = code,
                redirectUri = BuildConfig.SPOTIFY_REDIRECT_URI,
                clientId = BuildConfig.SPOTIFY_CLIENT_ID,
                codeVerifier = codeVerifier
            )

            saveTokens(
                accessToken = tokenResponse.accessToken,
                refreshToken = tokenResponse.refreshToken,
                expiresInSeconds = tokenResponse.expiresIn
            )

            clearPendingAuthorization()
            Resource.Success(Unit)
        } catch (error: Exception) {
            Resource.Error(
                spotifyErrorMessage(error),
                error
            )
        }
    }

    private suspend fun searchTrack(
        isrc: String,
        accessToken: String,
        retryAfterUnauthorized: Boolean
    ): SpotifyLinkResult {
        return try {
            val response = spotifyApi.searchTrack(
                authorization = "Bearer $accessToken",
                query = "isrc:${isrc.trim()}"
            )

            val spotifyUrl = response.tracks
                ?.items
                ?.firstOrNull()
                ?.externalUrls
                ?.spotify

            if (spotifyUrl.isNullOrBlank()) {
                SpotifyLinkResult.Error(
                    "No encontramos esta canción en Spotify."
                )
            } else {
                SpotifyLinkResult.Success(spotifyUrl)
            }
        } catch (error: HttpException) {
            if (
                error.code() == 401 &&
                retryAfterUnauthorized
            ) {
                preferences.edit()
                    .remove(KEY_ACCESS_TOKEN)
                    .remove(KEY_ACCESS_TOKEN_EXPIRES_AT)
                    .apply()

                val refreshedToken = refreshAccessToken()

                if (refreshedToken != null) {
                    searchTrack(
                        isrc = isrc,
                        accessToken = refreshedToken,
                        retryAfterUnauthorized = false
                    )
                } else {
                    SpotifyLinkResult.AuthorizationRequired(
                        createAuthorizationUrl()
                    )
                }
            } else {
                SpotifyLinkResult.Error(
                    spotifyErrorMessage(error)
                )
            }
        } catch (error: Exception) {
            SpotifyLinkResult.Error(
                spotifyErrorMessage(error)
            )
        }
    }

    private suspend fun getValidAccessToken(): String? {
        val accessToken = preferences.getString(
            KEY_ACCESS_TOKEN,
            null
        )

        val expiresAt = preferences.getLong(
            KEY_ACCESS_TOKEN_EXPIRES_AT,
            0L
        )

        val tokenIsValid =
            !accessToken.isNullOrBlank() &&
                    System.currentTimeMillis() < expiresAt

        if (tokenIsValid) {
            return accessToken
        }

        return refreshAccessToken()
    }

    private suspend fun refreshAccessToken(): String? {
        val refreshToken = preferences.getString(
            KEY_REFRESH_TOKEN,
            null
        ) ?: return null

        return try {
            val response = spotifyAccountsApi.refreshAccessToken(
                refreshToken = refreshToken,
                clientId = BuildConfig.SPOTIFY_CLIENT_ID
            )

            saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
                    ?: refreshToken,
                expiresInSeconds = response.expiresIn
            )

            response.accessToken
        } catch (_: Exception) {
            clearTokens()
            null
        }
    }

    private fun createAuthorizationUrl(): String {
        val codeVerifier = generateRandomValue(64)
        val codeChallenge = createCodeChallenge(codeVerifier)
        val state = generateRandomValue(32)

        preferences.edit()
            .putString(KEY_CODE_VERIFIER, codeVerifier)
            .putString(KEY_AUTH_STATE, state)
            .apply()

        return Uri.Builder()
            .scheme("https")
            .authority("accounts.spotify.com")
            .appendPath("authorize")
            .appendQueryParameter(
                "client_id",
                BuildConfig.SPOTIFY_CLIENT_ID
            )
            .appendQueryParameter(
                "response_type",
                "code"
            )
            .appendQueryParameter(
                "redirect_uri",
                BuildConfig.SPOTIFY_REDIRECT_URI
            )
            .appendQueryParameter(
                "code_challenge_method",
                "S256"
            )
            .appendQueryParameter(
                "code_challenge",
                codeChallenge
            )
            .appendQueryParameter(
                "state",
                state
            )
            .build()
            .toString()
    }

    private fun createCodeChallenge(
        codeVerifier: String
    ): String {
        val digest = MessageDigest
            .getInstance("SHA-256")
            .digest(codeVerifier.toByteArray(Charsets.US_ASCII))

        return Base64.encodeToString(
            digest,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    private fun generateRandomValue(
        length: Int
    ): String {
        val characters =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "0123456789-._~"

        val secureRandom = SecureRandom()

        return buildString(length) {
            repeat(length) {
                append(
                    characters[
                        secureRandom.nextInt(characters.length)
                    ]
                )
            }
        }
    }

    private fun saveTokens(
        accessToken: String,
        refreshToken: String?,
        expiresInSeconds: Long
    ) {
        val expiresAt =
            System.currentTimeMillis() +
                    (expiresInSeconds * 1000L) -
                    TOKEN_EXPIRATION_MARGIN

        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putLong(KEY_ACCESS_TOKEN_EXPIRES_AT, expiresAt)
            .apply()

        if (!refreshToken.isNullOrBlank()) {
            preferences.edit()
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply()
        }
    }

    private fun clearPendingAuthorization() {
        preferences.edit()
            .remove(KEY_CODE_VERIFIER)
            .remove(KEY_AUTH_STATE)
            .apply()
    }

    private fun clearTokens() {
        preferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_ACCESS_TOKEN_EXPIRES_AT)
            .apply()
    }

    private fun spotifyErrorMessage(
        error: Throwable
    ): String {
        return when (error) {
            is HttpException -> {
                when (error.code()) {
                    400 -> "Spotify rechazó la solicitud."
                    401 -> "La sesión de Spotify venció."
                    403 -> "Spotify no autorizó esta operación."
                    429 -> "Se alcanzó temporalmente el límite de Spotify."
                    else -> "Error de Spotify (${error.code()})."
                }
            }

            else -> {
                error.message
                    ?: "No pudimos conectarnos con Spotify."
            }
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "spotify_preferences"
        const val KEY_ACCESS_TOKEN = "spotify_access_token"
        const val KEY_REFRESH_TOKEN = "spotify_refresh_token"
        const val KEY_ACCESS_TOKEN_EXPIRES_AT =
            "spotify_access_token_expires_at"
        const val KEY_CODE_VERIFIER = "spotify_code_verifier"
        const val KEY_AUTH_STATE = "spotify_auth_state"
        const val TOKEN_EXPIRATION_MARGIN = 60_000L
    }
}