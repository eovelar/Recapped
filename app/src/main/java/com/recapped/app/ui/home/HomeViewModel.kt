package com.recapped.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.data.repository.UserProfileRepository
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.Artist
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

    data class Error(val message: String) : HomePhase
}

data class HomeHeader(
    val displayName: String,
    val initial: String
) {
    companion object {
        fun fromName(name: String?): HomeHeader {
            val safe = name?.takeIf { it.isNotBlank() } ?: "usuario"
            return HomeHeader(
                displayName = safe,
                initial = safe.first().uppercase()
            )
        }
    }
}

data class HomeUiState(
    val header: HomeHeader = HomeHeader("usuario", "U"),
    val phase: HomePhase = HomePhase.Loading
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userProfileRepository = UserProfileRepository()

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.update {
                    it.copy(
                        header = HomeHeader.fromName(
                            user?.displayName ?: user?.email
                        )
                    )
                }
            }
        }

        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update {
                it.copy(phase = HomePhase.Loading)
            }

            val username = try {
                userProfileRepository.getLastFmUsername()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        phase = HomePhase.Error(
                            e.message ?: "No se pudo leer tu usuario de Last.fm."
                        )
                    )
                }
                return@launch
            }

            if (username.isNullOrBlank()) {
                _state.update {
                    it.copy(
                        phase = HomePhase.Error(
                            "No hay una cuenta de Last.fm vinculada."
                        )
                    )
                }
                return@launch
            }

            artistRepository
                .getUserTopArtists(
                    username = username,
                    period = "1month"
                )
                .collect { res ->
                    _state.update { current ->
                        current.copy(
                            phase = when (res) {
                                is Resource.Loading -> HomePhase.Loading

                                is Resource.Error -> HomePhase.Error(res.message)

                                is Resource.Success -> {
                                    val all = res.data

                                    HomePhase.Success(
                                        topArtists = all.take(6),
                                        totalArtists = all.size,
                                        totalScrobbles = all.sumOf { artist ->
                                            artist.playcount
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
        }
    }
}