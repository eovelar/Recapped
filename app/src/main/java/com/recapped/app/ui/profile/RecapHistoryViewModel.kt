package com.recapped.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.RecapHistoryRepository
import com.recapped.app.domain.model.StoredRecap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecapHistoryUiState(
    val isLoading: Boolean = true,
    val recaps: List<StoredRecap> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class RecapHistoryViewModel @Inject constructor(
    private val repository: RecapHistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        RecapHistoryUiState()
    )

    val state: StateFlow<RecapHistoryUiState> =
        _state.asStateFlow()

    init {
        observeRecaps()
    }

    private fun observeRecaps() {
        viewModelScope.launch {
            repository.observeRecaps()
                .catch { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                                ?: "No pudimos cargar el historial."
                        )
                    }
                }
                .collect { recaps ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            recaps = recaps,
                            error = null
                        )
                    }
                }
        }
    }

    fun deleteRecap(recapId: String) {
        viewModelScope.launch {
            runCatching {
                repository.deleteRecap(recapId)
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        error = error.message
                            ?: "No pudimos eliminar el recap."
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update {
            it.copy(error = null)
        }
    }
}