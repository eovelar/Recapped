package com.recapped.app.ui.recap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.AppSettingsRepository
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.data.repository.RecapHistoryRepository
import com.recapped.app.data.repository.RecapRepository
import com.recapped.app.data.repository.SpotifyLinkResult
import com.recapped.app.data.repository.SpotifyRepository
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.RecapPeriod
import com.recapped.app.domain.model.RecapResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RecapSpotifyAction {
    data object Idle : RecapSpotifyAction
    data object Loading : RecapSpotifyAction

    data class Authorize(
        val url: String
    ) : RecapSpotifyAction

    data class OpenArtist(
        val url: String
    ) : RecapSpotifyAction

    data class Error(
        val message: String
    ) : RecapSpotifyAction
}

data class RecapUiState(
    val selectedPeriod: RecapPeriod = RecapPeriod.Month,
    val isGenerating: Boolean = false,
    val result: RecapResult? = null,
    val error: String? = null,
    val spotifyAction: RecapSpotifyAction =
        RecapSpotifyAction.Idle,
    val loadingSpotifyArtist: String? = null
)

@HiltViewModel
class RecapViewModel @Inject constructor(
    private val recapRepository: RecapRepository,
    private val recapHistoryRepository: RecapHistoryRepository,
    private val spotifyRepository: SpotifyRepository,
    private val authRepository: AuthRepository,
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecapUiState())
    val state: StateFlow<RecapUiState> = _state.asStateFlow()

    private var pendingSpotifyArtist: String? = null

    init {
        observeDefaultPeriod()
    }

    private fun observeDefaultPeriod() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user == null) {
                    return@collectLatest
                }

                appSettingsRepository
                    .observeSettings(user.uid)
                    .collect { settings ->
                        if (!_state.value.isGenerating) {
                            _state.update {
                                it.copy(
                                    selectedPeriod =
                                        settings.defaultPeriod
                                )
                            }
                        }
                    }
            }
        }
    }

    fun selectPeriod(period: RecapPeriod) {
        if (_state.value.isGenerating) {
            return
        }

        _state.update {
            it.copy(
                selectedPeriod = period,
                result = null,
                error = null
            )
        }
    }

    fun generateRecap(
        onSuccess: () -> Unit
    ) {
        if (_state.value.isGenerating) {
            return
        }

        val period = _state.value.selectedPeriod

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isGenerating = true,
                    result = null,
                    error = null
                )
            }

            when (
                val result =
                    recapRepository.generateRecap(period)
            ) {
                Resource.Loading -> Unit

                is Resource.Success -> {
                    try {
                        recapHistoryRepository.saveRecap(
                            result.data
                        )

                        _state.update {
                            it.copy(
                                isGenerating = false,
                                result = result.data,
                                error = null
                            )
                        }

                        onSuccess()
                    } catch (error: Exception) {
                        _state.update {
                            it.copy(
                                isGenerating = false,
                                result = result.data,
                                error = error.message
                                    ?: "El recap se generó, pero no pudo guardarse."
                            )
                        }
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isGenerating = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun openStoredRecap(recap: RecapResult) {
        _state.update {
            it.copy(
                selectedPeriod = recap.period,
                result = recap,
                error = null
            )
        }
    }

    fun openArtistInSpotify(
        artistName: String
    ) {
        if (
            artistName.isBlank() ||
            _state.value.spotifyAction
                    is RecapSpotifyAction.Loading
        ) {
            return
        }

        pendingSpotifyArtist = artistName

        viewModelScope.launch {
            _state.update {
                it.copy(
                    spotifyAction =
                        RecapSpotifyAction.Loading,
                    loadingSpotifyArtist = artistName
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
                        RecapSpotifyAction.Loading
                )
            }

            when (
                val result =
                    spotifyRepository.completeAuthorization(
                        callbackUrl
                    )
            ) {
                is Resource.Success -> {
                    val artistName = pendingSpotifyArtist

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
                spotifyAction = RecapSpotifyAction.Idle,
                loadingSpotifyArtist = null
            )
        }
    }

    fun clearError() {
        _state.update {
            it.copy(error = null)
        }
    }

    fun clearResult() {
        _state.update {
            it.copy(result = null)
        }
    }

    private suspend fun requestSpotifyArtist(
        artistName: String
    ) {
        when (
            val result =
                spotifyRepository.getArtistUrl(artistName)
        ) {
            is SpotifyLinkResult.Success -> {
                _state.update {
                    it.copy(
                        spotifyAction =
                            RecapSpotifyAction.OpenArtist(
                                result.spotifyUrl
                            ),
                        loadingSpotifyArtist = null
                    )
                }
            }

            is SpotifyLinkResult.AuthorizationRequired -> {
                _state.update {
                    it.copy(
                        spotifyAction =
                            RecapSpotifyAction.Authorize(
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
                    RecapSpotifyAction.Error(message),
                loadingSpotifyArtist = null
            )
        }
    }
}