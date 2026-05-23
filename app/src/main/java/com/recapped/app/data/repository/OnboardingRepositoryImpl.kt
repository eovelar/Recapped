package com.recapped.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore by preferencesDataStore(name = "recapped_onboarding")

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OnboardingRepository {

    override fun observeUserData(uid: String): Flow<OnboardingUserData> {
        val completedKey = completedKey(uid)
        val lastFmKey = lastFmKey(uid)

        return context.onboardingDataStore.data.map { preferences ->
            OnboardingUserData(
                isCompleted = preferences[completedKey] ?: false,
                lastFmUsername = preferences[lastFmKey]
            )
        }
    }

    override suspend fun saveLastFmUsername(uid: String, username: String) {
        context.onboardingDataStore.edit { preferences ->
            preferences[lastFmKey(uid)] = username.trim()
        }
    }

    override suspend fun completeOnboarding(uid: String) {
        context.onboardingDataStore.edit { preferences ->
            preferences[completedKey(uid)] = true
        }
    }

    private fun completedKey(uid: String) = booleanPreferencesKey("onboarding_completed_$uid")
    private fun lastFmKey(uid: String) = stringPreferencesKey("lastfm_username_$uid")
}
