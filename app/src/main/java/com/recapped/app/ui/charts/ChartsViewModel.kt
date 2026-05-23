package com.recapped.app.ui.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.domain.Resource
import com.recapped.app.domain.model.Artist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChartsContentType {
    ARTISTS,
    SONGS
}

enum class ChartsViewMode {
    GRID,
    LIST
}

data class ChartSong(
    val rank: Int,
    val name: String,
    val artistName: String,
    val playcount: Long,
    val imageUrl: String?
)

sealed interface ChartsPhase {
    data object Loading : ChartsPhase

    data class Success(
        val artists: List<Artist>,
        val songs: List<ChartSong> = emptyList()
    ) : ChartsPhase

    data class Error(val message: String) : ChartsPhase
}

data class ChartsUiState(
    val phase: ChartsPhase = ChartsPhase.Loading,
    val query: String = "",
    val contentType: ChartsContentType = ChartsContentType.ARTISTS,
    val viewMode: ChartsViewMode = ChartsViewMode.GRID
) {
    val filteredArtists: List<Artist>
        get() = when (phase) {
            is ChartsPhase.Success -> {
                if (query.isBlank()) phase.artists
                else phase.artists.filter { artist ->
                    artist.name.contains(query, ignoreCase = true)
                }
            }

            else -> emptyList()
        }

    val filteredSongs: List<ChartSong>
        get() = when (phase) {
            is ChartsPhase.Success -> {
                if (query.isBlank()) phase.songs
                else phase.songs.filter { song ->
                    song.name.contains(query, ignoreCase = true) ||
                            song.artistName.contains(query, ignoreCase = true)
                }
            }

            else -> emptyList()
        }
}

@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChartsUiState())
    val state: StateFlow<ChartsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            artistRepository.getTopArtists().collect { res ->
                when (res) {
                    is Resource.Loading -> {
                        _state.update { current ->
                            current.copy(phase = ChartsPhase.Loading)
                        }
                    }

                    is Resource.Error -> {
                        _state.update { current ->
                            current.copy(phase = ChartsPhase.Error(res.message))
                        }
                    }

                    is Resource.Success -> {
                        val artists = res.data

                        _state.update { current ->
                            current.copy(
                                phase = ChartsPhase.Success(
                                    artists = artists,
                                    songs = emptyList()
                                )
                            )
                        }

                        val songs = loadSongsFromTopArtists(artists)

                        _state.update { current ->
                            val currentPhase = current.phase
                            if (currentPhase is ChartsPhase.Success) {
                                current.copy(
                                    phase = currentPhase.copy(
                                        songs = songs
                                    )
                                )
                            } else {
                                current
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadSongsFromTopArtists(
        artists: List<Artist>
    ): List<ChartSong> {
        val songs = mutableListOf<ChartSong>()

        artists.take(5).forEach { artist ->
            val result = artistRepository
                .getArtistDetail(artist.name)
                .first { resource -> resource !is Resource.Loading }

            if (result is Resource.Success) {
                result.data.topTracks.take(3).forEach { track ->
                    songs.add(
                        ChartSong(
                            rank = 0,
                            name = track.name,
                            artistName = artist.name,
                            playcount = track.playcount,
                            imageUrl = track.imageUrl
                        )
                    )
                }
            }
        }

        return songs
            .distinctBy { song -> "${song.artistName}-${song.name}" }
            .sortedByDescending { song -> song.playcount }
            .mapIndexed { index, song ->
                song.copy(rank = index + 1)
            }
    }

    fun onQueryChange(q: String) {
        _state.update { current ->
            current.copy(query = q)
        }
    }

    fun onContentTypeChange(type: ChartsContentType) {
        _state.update { current ->
            current.copy(contentType = type)
        }
    }

    fun onViewModeChange(mode: ChartsViewMode) {
        _state.update { current ->
            current.copy(viewMode = mode)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}