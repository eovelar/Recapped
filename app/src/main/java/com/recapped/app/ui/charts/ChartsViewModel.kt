package com.recapped.app.ui.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.Artist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado UI del listado.
 *
 * - phase modela los 3 estados que exige la consigna: Loading / Success / Error
 *   (incluye también Idle inicial).
 * - query y filteredArtists soportan la búsqueda reactiva sobre el listado.
 */
sealed interface ChartsPhase {
    data object Loading : ChartsPhase
    data class Success(val artists: List<Artist>) : ChartsPhase
    data class Error(val message: String) : ChartsPhase
}

data class ChartsUiState(
    val phase: ChartsPhase = ChartsPhase.Loading,
    val query: String = ""
) {
    /** Artistas filtrados por query (case-insensitive). */
    val filteredArtists: List<Artist>
        get() = when (phase) {
            is ChartsPhase.Success -> {
                if (query.isBlank()) phase.artists
                else phase.artists.filter { it.name.contains(query, ignoreCase = true) }
            }
            else -> emptyList()
        }
}

@OptIn(FlowPreview::class)
@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChartsUiState())
    val state: StateFlow<ChartsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            artistRepository.getTopArtists().collect { res ->
                _state.update { current ->
                    current.copy(
                        phase = when (res) {
                            is Resource.Loading -> ChartsPhase.Loading
                            is Resource.Success -> ChartsPhase.Success(res.data)
                            is Resource.Error -> ChartsPhase.Error(res.message)
                        }
                    )
                }
            }
        }
    }

    fun onQueryChange(q: String) {
        _state.update { it.copy(query = q) }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
