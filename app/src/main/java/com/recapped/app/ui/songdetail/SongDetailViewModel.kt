package com.recapped.app.ui.songdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.SpotifyLinkResult
import com.recapped.app.data.repository.SpotifyRepository
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.SongDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SongDetailPhase {
    data object Loading : SongDetailPhase

    data class Success(
        val detail: SongDetail
    ) : SongDetailPhase

    data class Error(
        val message: String
    ) : SongDetailPhase
}

sealed interface SpotifyAction {
    data object Idle : SpotifyAction
    data object Loading : SpotifyAction

    data class Authorize(
        val url: String
    ) : SpotifyAction

    data class OpenTrack(
        val url: String
    ) : SpotifyAction

    data class Error(
        val message: String
    ) : SpotifyAction
}

data class SongDetailUiState(
    val phase: SongDetailPhase = SongDetailPhase.Loading,
    val spotifyAction: SpotifyAction = SpotifyAction.Idle
)

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val spotifyRepository: SpotifyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SongDetailUiState())
    val state: StateFlow<SongDetailUiState> = _state.asStateFlow()

    private var currentArtistName: String = ""
    private var currentTrackName: String = ""

    private var pendingDeezerTrackId: Long? = null
    private var pendingIsrc: String? = null

    fun load(
        artistName: String,
        trackName: String
    ) {
        if (
            artistName == currentArtistName &&
            trackName == currentTrackName &&
            state.value.phase is SongDetailPhase.Success
        ) {
            return
        }

        currentArtistName = artistName
        currentTrackName = trackName

        viewModelScope.launch {
            _state.update {
                it.copy(phase = SongDetailPhase.Loading)
            }

            when (
                val result = artistRepository.getSongDetail(
                    artistName = artistName,
                    trackName = trackName
                )
            ) {
                Resource.Loading -> {
                    _state.update {
                        it.copy(phase = SongDetailPhase.Loading)
                    }
                }

                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            phase = SongDetailPhase.Success(result.data)
                        )
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            phase = SongDetailPhase.Error(result.message)
                        )
                    }
                }
            }
        }
    }

    fun openInSpotify(
        deezerTrackId: Long,
        knownIsrc: String? = null
    ) {
        pendingDeezerTrackId = deezerTrackId
        pendingIsrc = knownIsrc

        viewModelScope.launch {
            _state.update {
                it.copy(spotifyAction = SpotifyAction.Loading)
            }

            val isrc = knownIsrc
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: when (
                    val result = artistRepository.getTrackIsrc(
                        deezerTrackId
                    )
                ) {
                    is Resource.Success -> result.data

                    is Resource.Error -> {
                        showSpotifyError(result.message)
                        return@launch
                    }

                    Resource.Loading -> {
                        showSpotifyError(
                            "No pudimos obtener la información de la canción."
                        )
                        return@launch
                    }
                }

            pendingIsrc = isrc
            requestSpotifyTrack(isrc)
        }
    }

    fun onSpotifyAuthorizationCallback(
        callbackUrl: String
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(spotifyAction = SpotifyAction.Loading)
            }

            when (
                val result = spotifyRepository.completeAuthorization(
                    callbackUrl
                )
            ) {
                is Resource.Success -> {
                    val isrc = pendingIsrc

                    if (isrc.isNullOrBlank()) {
                        showSpotifyError(
                            "No encontramos la canción pendiente."
                        )
                    } else {
                        requestSpotifyTrack(isrc)
                    }
                }

                is Resource.Error -> {
                    showSpotifyError(result.message)
                }

                Resource.Loading -> Unit
            }
        }
    }

    fun consumeSpotifyAction() {
        _state.update {
            it.copy(spotifyAction = SpotifyAction.Idle)
        }
    }

    fun retry() {
        if (
            currentArtistName.isNotBlank() &&
            currentTrackName.isNotBlank()
        ) {
            load(
                artistName = currentArtistName,
                trackName = currentTrackName
            )
        }
    }

    private suspend fun requestSpotifyTrack(
        isrc: String
    ) {
        when (
            val result = spotifyRepository.getTrackUrlByIsrc(isrc)
        ) {
            is SpotifyLinkResult.Success -> {
                _state.update {
                    it.copy(
                        spotifyAction = SpotifyAction.OpenTrack(
                            result.spotifyUrl
                        )
                    )
                }
            }

            is SpotifyLinkResult.AuthorizationRequired -> {
                _state.update {
                    it.copy(
                        spotifyAction = SpotifyAction.Authorize(
                            result.authorizationUrl
                        )
                    )
                }
            }

            is SpotifyLinkResult.Error -> {
                showSpotifyError(result.message)
            }
        }
    }

    private fun showSpotifyError(
        message: String
    ) {
        _state.update {
            it.copy(
                spotifyAction = SpotifyAction.Error(message)
            )
        }
    }
}