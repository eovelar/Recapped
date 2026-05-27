package com.recapped.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.AuthRepository
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

data class EditProfileUiState(
    val displayName: String = "Usuario",
    val email: String = "",
    val lastFmUsername: String = "",
    val initial: String = "U",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val artistRepository: ArtistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            val user = authRepository.currentUser.first()

            val displayName = user?.displayName?.takeIf { name -> name.isNotBlank() }
                ?: user?.email?.substringBefore("@")
                ?: "Usuario"

            val email = user?.email.orEmpty()

            val username = runCatching {
                userProfileRepository.getLastFmUsername().orEmpty()
            }.getOrElse {
                ""
            }

            _state.update {
                it.copy(
                    displayName = displayName,
                    email = email,
                    lastFmUsername = username,
                    initial = displayName.firstOrNull()?.uppercase() ?: "U",
                    isLoading = false
                )
            }
        }
    }

    fun onLastFmUsernameChange(value: String) {
        _state.update {
            it.copy(
                lastFmUsername = value
                    .replace(" ", "")
                    .take(40),
                error = null,
                success = false
            )
        }
    }

    fun saveChanges(onSaved: () -> Unit) {
        val username = state.value.lastFmUsername.trim()

        if (username.isBlank()) {
            _state.update {
                it.copy(error = "Ingresá tu usuario de Last.fm.")
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSaving = true,
                    error = null,
                    success = false
                )
            }

            val validationResult = artistRepository.validateLastFmUsername(username)

            if (validationResult is Resource.Error) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = validationResult.message.ifBlank {
                            "No encontramos ese usuario de Last.fm."
                        }
                    )
                }
                return@launch
            }

            runCatching {
                userProfileRepository.saveLastFmUsername(username)
            }.onSuccess {
                _state.update {
                    it.copy(
                        isSaving = false,
                        success = true
                    )
                }
                onSaved()
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.message ?: "No se pudo guardar el usuario de Last.fm."
                    )
                }
            }
        }
    }
}