package com.recapped.app.data.repository

import kotlinx.coroutines.flow.Flow

data class OnboardingUserData(
    val isCompleted: Boolean = false,
    val lastFmUsername: String? = null
)

interface OnboardingRepository {
    fun observeUserData(uid: String): Flow<OnboardingUserData>
    suspend fun saveLastFmUsername(uid: String, username: String)
    suspend fun completeOnboarding(uid: String)
}
