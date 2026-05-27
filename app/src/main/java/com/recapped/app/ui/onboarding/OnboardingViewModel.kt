package com.recapped.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.data.repository.OnboardingRepository
import com.recapped.app.data.repository.UserProfileRepository
import com.recapped.app.domain.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val onboardingRepository: OnboardingRepository,
    private val artistRepository: ArtistRepository
) : ViewModel() {

    private val userProfileRepository = UserProfileRepository()

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

    fun validateLastFmUsername(onValid: () -> Unit) {
        val username = state.value.lastFmUsername.trim()

        if (username.isBlank()) {
            _state.update {
                it.copy(error = "Ingresá tu usuario de Last.fm para vincularlo.")
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

            runCatching {
                artistRepository
                    .getUserTopArtists(
                        username = username,
                        period = "overall"
                    )
                    .first { resource -> resource !is Resource.Loading }
            }.onSuccess { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isSaving = false,
                                error = null
                            )
                        }

                        onValid()
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isSaving = false,
                                error = friendlyLastFmError(result.message)
                            )
                        }
                    }

                    Resource.Loading -> {
                        _state.update {
                            it.copy(isSaving = false)
                        }
                    }
                }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = friendlyLastFmError(
                            throwable.message ?: "No se pudo validar el usuario de Last.fm."
                        )
                    )
                }
            }
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
                it.copy(error = "Ingresá tu usuario de Last.fm para vincularlo.")
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

            runCatching {
                userProfileRepository.createOrUpdateUserProfile()
                userProfileRepository.saveLastFmUsername(username)

                onboardingRepository.completeOnboarding(currentUid)
            }.onSuccess {
                _state.update {
                    it.copy(isSaving = false)
                }

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

            runCatching {
                userProfileRepository.createOrUpdateUserProfile()
                onboardingRepository.completeOnboarding(currentUid)
            }.onSuccess {
                _state.update {
                    it.copy(isSaving = false)
                }

                onCompleted()
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.message ?: "No se pudo completar el onboarding."
                    )
                }
            }
        }
    }

    private fun friendlyLastFmError(message: String): String {
        return when {
            message.contains("404", ignoreCase = true) ->
                "No encontramos ese usuario de Last.fm. Revisá que esté escrito correctamente."

            message.contains("No hay conexión", ignoreCase = true) ->
                "No hay conexión a Internet. Intentá nuevamente."

            message.contains("usuario de Last.fm", ignoreCase = true) ->
                message

            else ->
                "No se pudo validar el usuario de Last.fm. Revisá el nombre e intentá otra vez."
        }
    }
}