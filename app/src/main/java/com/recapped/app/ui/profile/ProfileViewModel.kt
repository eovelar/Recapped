package com.recapped.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recapped.app.data.repository.AppSettingsRepository
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.data.repository.RecapHistoryRepository
import com.recapped.app.domain.model.RecapPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val displayName: String = "Usuario",
    val email: String? = null,
    val defaultPeriod: RecapPeriod = RecapPeriod.Month,
    val notificationsEnabled: Boolean = false,
    val recapCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val recapHistoryRepository: RecapHistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> =
        _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        observeProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                currentUserId = user?.uid

                if (user == null) {
                    _state.value = ProfileUiState(
                        isLoading = false
                    )
                    return@collectLatest
                }

                val displayName = user.displayName
                    ?.takeIf { it.isNotBlank() }
                    ?: user.email?.substringBefore("@")
                    ?: "Usuario"

                combine(
                    appSettingsRepository.observeSettings(
                        user.uid
                    ),
                    recapHistoryRepository.observeRecaps()
                ) { settings, recaps ->
                    ProfileUiState(
                        displayName = displayName,
                        email = user.email,
                        defaultPeriod =
                            settings.defaultPeriod,
                        notificationsEnabled =
                            settings.notificationsEnabled,
                        recapCount = recaps.size,
                        isLoading = false
                    )
                }.collect { profileState ->
                    _state.value = profileState
                }
            }
        }
    }

    fun setDefaultPeriod(period: RecapPeriod) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            runCatching {
                appSettingsRepository.setDefaultPeriod(
                    userId = userId,
                    period = period
                )
            }.onFailure { error ->
                showError(
                    error.message
                        ?: "No pudimos guardar el período."
                )
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            runCatching {
                appSettingsRepository.setNotificationsEnabled(
                    userId = userId,
                    enabled = enabled
                )
            }.onFailure { error ->
                showError(
                    error.message
                        ?: "No pudimos guardar la configuración."
                )
            }
        }
    }

    fun clearError() {
        _state.update {
            it.copy(error = null)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    private fun showError(message: String) {
        _state.update {
            it.copy(error = message)
        }
    }
}