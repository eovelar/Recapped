package com.recapped.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.data.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val displayName: String = "Usuario",
    val lastFmUsername: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    private var uid: String? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                uid = user?.uid
                _state.update {
                    it.copy(
                        displayName = user?.displayName?.takeIf { name -> name.isNotBlank() }
                            ?: user?.email?.substringBefore("@")
                            ?: "Usuario"
                    )
                }
            }
        }
    }

    fun onLastFmUsernameChange(value: String) {
        _state.update {
            it.copy(
                lastFmUsername = value
                    .replace(" ", "")
                    .take(40),
                error = null
            )
        }
    }

    fun finish(onCompleted: () -> Unit) {
        val currentUid = uid
        val username = state.value.lastFmUsername.trim()

        if (currentUid == null) {
            _state.update { it.copy(error = "No se encontró la sesión activa.") }
            return
        }

        if (username.isBlank()) {
            _state.update { it.copy(error = "Ingresá tu usuario de Last.fm para vincularlo.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            runCatching {
                onboardingRepository.saveLastFmUsername(currentUid, username)
                onboardingRepository.completeOnboarding(currentUid)
            }.onSuccess {
                _state.update { it.copy(isSaving = false) }
                onCompleted()
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.message ?: "No se pudo guardar la configuración."
                    )
                }
            }
        }
    }

    fun skip(onCompleted: () -> Unit) {
        val currentUid = uid
        if (currentUid == null) {
            _state.update { it.copy(error = "No se encontró la sesión activa.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            onboardingRepository.completeOnboarding(currentUid)
            _state.update { it.copy(isSaving = false) }
            onCompleted()
        }
    }
}
