package com.recapped.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado UI del login (state hoisting: la screen sólo recibe esto + callbacks).
 */
data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    /** Llamado desde la Activity con el ID token devuelto por Credential Manager. */
    fun onGoogleIdToken(idToken: String) {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { authRepository.signInWithGoogleIdToken(idToken) }
                .onSuccess { _state.update { it.copy(loading = false) } }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, error = e.message ?: "Error al iniciar sesión") }
                }
        }
    }

    fun onSignInError(msg: String) {
        _state.update { it.copy(loading = false, error = msg) }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
