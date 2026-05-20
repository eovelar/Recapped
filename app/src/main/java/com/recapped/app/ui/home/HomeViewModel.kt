package com.recapped.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.Artist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado del Home. Mismas 3 fases (Loading/Success/Error) que pide la consigna,
 * con un `header` siempre disponible (depende sólo de Firebase Auth, no de la red).
 */
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

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        // Header: del usuario logueado (Firebase). Reactivo a cambios.
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.update { it.copy(header = HomeHeader.fromName(user?.displayName ?: user?.email)) }
            }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            artistRepository.getTopArtists().collect { res ->
                _state.update { current ->
                    current.copy(
                        phase = when (res) {
                            is Resource.Loading -> HomePhase.Loading
                            is Resource.Error -> HomePhase.Error(res.message)
                            is Resource.Success -> {
                                val all = res.data
                                HomePhase.Success(
                                    topArtists = all.take(3),
                                    totalArtists = all.size,
                                    totalScrobbles = all.sumOf { it.playcount }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
