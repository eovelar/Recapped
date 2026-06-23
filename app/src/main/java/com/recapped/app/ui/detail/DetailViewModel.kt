package com.recapped.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.SpotifyLinkResult
import com.recapped.app.data.repository.SpotifyRepository
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.ArtistDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailPhase {
    data object Loading : DetailPhase
    data class Success(val detail: ArtistDetail) : DetailPhase
    data class Error(val message: String) : DetailPhase
}

sealed interface ArtistSpotifyAction {
    data object Idle : ArtistSpotifyAction
    data object Loading : ArtistSpotifyAction

    data class Authorize(
        val url: String
    ) : ArtistSpotifyAction

    data class OpenArtist(
        val url: String
    ) : ArtistSpotifyAction

    data class Error(
        val message: String
    ) : ArtistSpotifyAction
}

data class DetailUiState(
    val phase: DetailPhase = DetailPhase.Loading,
    val artistName: String = "",
    val spotifyAction: ArtistSpotifyAction = ArtistSpotifyAction.Idle
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val spotifyRepository: SpotifyRepository,
    savedState: SavedStateHandle
) : ViewModel() {

    private val initialName =
        savedState.get<String>("artistName").orEmpty()

    private val _state = MutableStateFlow(
        DetailUiState(artistName = initialName)
    )

    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    private var pendingArtistName: String? = null

    init {
        if (initialName.isNotBlank()) {
            load(initialName)
        }
    }

    fun load(name: String = initialName) {
        viewModelScope.launch {
            artistRepository.getArtistDetail(name).collect { result ->
                _state.update { current ->
                    current.copy(
                        artistName = name,
                        phase = when (result) {
                            Resource.Loading -> DetailPhase.Loading

                            is Resource.Success -> {
                                DetailPhase.Success(result.data)
                            }

                            is Resource.Error -> {
                                DetailPhase.Error(result.message)
                            }
                        }
                    )
                }
            }
        }
    }

    fun openArtistInSpotify(
        artistName: String
    ) {
        pendingArtistName = artistName

        viewModelScope.launch {
            _state.update {
                it.copy(
                    spotifyAction = ArtistSpotifyAction.Loading
                )
            }

            requestSpotifyArtist(artistName)
        }
    }

    fun onSpotifyAuthorizationCallback(
        callbackUrl: String
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    spotifyAction = ArtistSpotifyAction.Loading
                )
            }

            when (
                val result = spotifyRepository.completeAuthorization(
                    callbackUrl
                )
            ) {
                is Resource.Success -> {
                    val artistName = pendingArtistName

                    if (artistName.isNullOrBlank()) {
                        showSpotifyError(
                            "No encontramos el artista pendiente."
                        )
                    } else {
                        requestSpotifyArtist(artistName)
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
            it.copy(
                spotifyAction = ArtistSpotifyAction.Idle
            )
        }
    }

    private suspend fun requestSpotifyArtist(
        artistName: String
    ) {
        when (
            val result = spotifyRepository.getArtistUrl(
                artistName
            )
        ) {
            is SpotifyLinkResult.Success -> {
                _state.update {
                    it.copy(
                        spotifyAction =
                            ArtistSpotifyAction.OpenArtist(
                                result.spotifyUrl
                            )
                    )
                }
            }

            is SpotifyLinkResult.AuthorizationRequired -> {
                _state.update {
                    it.copy(
                        spotifyAction =
                            ArtistSpotifyAction.Authorize(
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
                spotifyAction = ArtistSpotifyAction.Error(message)
            )
        }
    }
}