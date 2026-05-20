package com.recapped.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Determina al arranque si hay sesión persistida y por dónde entrar.
 * Mientras está en Checking, MainActivity mantiene la splash visible.
 */
sealed interface AuthUiState {
    data object Checking : AuthUiState
    data object SignedOut : AuthUiState
    data class SignedIn(val uid: String, val displayName: String?) : AuthUiState
}

@HiltViewModel
class RootViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<AuthUiState> = authRepository.currentUser
        .map<_, AuthUiState> { user ->
            if (user == null) AuthUiState.SignedOut
            else AuthUiState.SignedIn(user.uid, user.displayName)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AuthUiState.Checking
        )
}
