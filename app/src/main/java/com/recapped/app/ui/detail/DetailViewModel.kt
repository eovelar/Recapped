package com.recapped.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
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

data class DetailUiState(
    val phase: DetailPhase = DetailPhase.Loading,
    val artistName: String = ""
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    savedState: SavedStateHandle
) : ViewModel() {

    private val initialName: String = savedState.get<String>("artistName").orEmpty()

    private val _state = MutableStateFlow(DetailUiState(artistName = initialName))
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init { if (initialName.isNotBlank()) load(initialName) }

    fun load(name: String = initialName) {
        viewModelScope.launch {
            artistRepository.getArtistDetail(name).collect { res ->
                _state.update { current ->
                    current.copy(
                        artistName = name,
                        phase = when (res) {
                            is Resource.Loading -> DetailPhase.Loading
                            is Resource.Success -> DetailPhase.Success(res.data)
                            is Resource.Error -> DetailPhase.Error(res.message)
                        }
                    )
                }
            }
        }
    }
}
