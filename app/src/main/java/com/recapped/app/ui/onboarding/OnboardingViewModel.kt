package com.recapped.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.data.repository.OnboardingRepository
import com.recapped.app.domain.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
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
    private val onboardingRepository: OnboardingRepository,
    private val artistRepository: ArtistRepository
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
                        displayName = user?.displayName
                            ?.takeIf { name -> name.isNotBlank() }
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
            _state.update {
                it.copy(error = "No se encontró la sesión activa.")
            }
            return
        }

        if (username.isBlank()) {
            _state.update {
                it.copy(
                    error = "Ingresá tu usuario de Last.fm para vincularlo."
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSaving = true,
                    error = null
                )
            }

            try {
                val validationResult = withTimeout(15_000L) {
                    artistRepository.validateLastFmUsername(username)
                }

                if (validationResult is Resource.Error) {
                    _state.update {
                        it.copy(
                            error = validationResult.message.ifBlank {
                                "No encontramos ese usuario de Last.fm."
                            }
                        )
                    }
                    return@launch
                }

                onboardingRepository.saveLastFmUsername(
                    currentUid,
                    username
                )
                onboardingRepository.completeOnboarding(currentUid)

                onCompleted()
            } catch (_: TimeoutCancellationException) {
                _state.update {
                    it.copy(
                        error = "Last.fm tardó demasiado en responder. Intentá nuevamente."
                    )
                }
            } catch (throwable: Exception) {
                _state.update {
                    it.copy(
                        error = throwable.message
                            ?: "No se pudo guardar la configuración."
                    )
                }
            } finally {
                _state.update {
                    it.copy(isSaving = false)
                }
            }
        }
    }

    fun skip(onCompleted: () -> Unit) {
        val currentUid = uid

        if (currentUid == null) {
            _state.update {
                it.copy(error = "No se encontró la sesión activa.")
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSaving = true,
                    error = null
                )
            }

            try {
                onboardingRepository.completeOnboarding(currentUid)
                onCompleted()
            } catch (throwable: Exception) {
                _state.update {
                    it.copy(
                        error = throwable.message
                            ?: "No se pudo completar el onboarding."
                    )
                }
            } finally {
                _state.update {
                    it.copy(isSaving = false)
                }
            }
        }
    }
}