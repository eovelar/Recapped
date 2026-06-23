package com.recapped.app.ui.songdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
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

data class SongDetailUiState(
    val phase: SongDetailPhase = SongDetailPhase.Loading
)

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val artistRepository: ArtistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SongDetailUiState())
    val state: StateFlow<SongDetailUiState> = _state.asStateFlow()

    private var currentArtistName: String = ""
    private var currentTrackName: String = ""

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
}