package com.recapped.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.data.repository.ForgottenArtistRepository
import com.recapped.app.data.repository.SpotifyLinkResult
import com.recapped.app.data.repository.SpotifyRepository
import com.recapped.app.data.repository.UserProfileRepository
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.Artist
import com.recapped.app.domain.model.ForgottenArtist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomePhase {
    data object Loading : HomePhase

    data class Success(
        val topArtists: List<Artist>,
        val totalArtists: Int,
        val totalScrobbles: Long
    ) : HomePhase

    data class Error(
        val message: String
    ) : HomePhase
}

sealed interface HomeSpotifyAction {
    data object Idle : HomeSpotifyAction
    data object Loading : HomeSpotifyAction

    data class Authorize(
        val url: String
    ) : HomeSpotifyAction

    data class OpenArtist(
        val url: String
    ) : HomeSpotifyAction

    data class Error(
        val message: String
    ) : HomeSpotifyAction
}

data class HomeHeader(
    val displayName: String,
    val initial: String
) {
    companion object {
        fun fromName(name: String?): HomeHeader {
            val safe = name
                ?.takeIf { it.isNotBlank() }
                ?: "usuario"

            return HomeHeader(
                displayName = safe,
                initial = safe.first().uppercase()
            )
        }
    }
}

data class HomeUiState(
    val header: HomeHeader =
        HomeHeader("usuario", "U"),
    val phase: HomePhase = HomePhase.Loading,
    val forgottenArtist: ForgottenArtist? = null,
    val isLoadingForgottenArtist: Boolean = false,
    val spotifyAction: HomeSpotifyAction =
        HomeSpotifyAction.Idle
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val forgottenArtistRepository:
    ForgottenArtistRepository,
    private val spotifyRepository: SpotifyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> =
        _state.asStateFlow()

    private var pendingSpotifyArtist: String? = null

    init {
        observeCurrentUser()
        load()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.update {
                    it.copy(
                        header = HomeHeader.fromName(
                            user?.displayName
                                ?: user?.email
                        )
                    )
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    phase = HomePhase.Loading,
                    forgottenArtist = null,
                    isLoadingForgottenArtist = true
                )
            }

            val username = try {
                userProfileRepository.getLastFmUsername()
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        phase = HomePhase.Error(
                            error.message
                                ?: "No se pudo leer tu usuario de Last.fm."
                        ),
                        isLoadingForgottenArtist = false
                    )
                }
                return@launch
            }

            if (username.isNullOrBlank()) {
                _state.update {
                    it.copy(
                        phase = HomePhase.Error(
                            "No hay una cuenta de Last.fm vinculada."
                        ),
                        isLoadingForgottenArtist = false
                    )
                }
                return@launch
            }

            loadForgottenArtist(username)

            artistRepository
                .getUserTopArtists(
                    username = username,
                    period = "1month"
                )
                .collect { result ->
                    when (result) {
                        Resource.Loading -> {
                            _state.update {
                                it.copy(
                                    phase = HomePhase.Loading
                                )
                            }
                        }

                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    phase = HomePhase.Error(
                                        result.message.ifBlank {
                                            "No se pudo cargar el historial."
                                        }
                                    )
                                )
                            }
                        }

                        is Resource.Success -> {
                            val artists = result.data

                            _state.update {
                                it.copy(
                                    phase = HomePhase.Success(
                                        topArtists =
                                            artists.take(6),
                                        totalArtists =
                                            artists.size,
                                        totalScrobbles =
                                            artists.sumOf {
                                                    artist ->
                                                artist.playcount
                                            }
                                    )
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun loadForgottenArtist(
        username: String
    ) {
        viewModelScope.launch {
            when (
                val result =
                    forgottenArtistRepository
                        .getForgottenArtist(username)
            ) {
                Resource.Loading -> Unit

                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            forgottenArtist = result.data,
                            isLoadingForgottenArtist = false
                        )
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            forgottenArtist = null,
                            isLoadingForgottenArtist = false
                        )
                    }
                }
            }
        }
    }

    fun openForgottenArtistInSpotify() {
        val artistName = _state.value
            .forgottenArtist
            ?.name
            ?.takeIf { it.isNotBlank() }
            ?: return

        if (
            _state.value.spotifyAction
                    is HomeSpotifyAction.Loading
        ) {
            return
        }

        pendingSpotifyArtist = artistName

        viewModelScope.launch {
            _state.update {
                it.copy(
                    spotifyAction =
                        HomeSpotifyAction.Loading
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
                    spotifyAction =
                        HomeSpotifyAction.Loading
                )
            }

            when (
                val result =
                    spotifyRepository
                        .completeAuthorization(callbackUrl)
            ) {
                is Resource.Success -> {
                    val artistName =
                        pendingSpotifyArtist

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
                spotifyAction =
                    HomeSpotifyAction.Idle
            )
        }
    }

    private suspend fun requestSpotifyArtist(
        artistName: String
    ) {
        when (
            val result =
                spotifyRepository
                    .getArtistUrl(artistName)
        ) {
            is SpotifyLinkResult.Success -> {
                _state.update {
                    it.copy(
                        spotifyAction =
                            HomeSpotifyAction.OpenArtist(
                                result.spotifyUrl
                            )
                    )
                }
            }

            is SpotifyLinkResult.AuthorizationRequired -> {
                _state.update {
                    it.copy(
                        spotifyAction =
                            HomeSpotifyAction.Authorize(
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
                spotifyAction =
                    HomeSpotifyAction.Error(message)
            )
        }
    }
}