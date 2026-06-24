package com.recapped.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.recapped.app.domain.model.RecapPeriod
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(
    name = "recapped_settings"
)

data class AppSettings(
    val defaultPeriod: RecapPeriod = RecapPeriod.Month,
    val notificationsEnabled: Boolean = false
)

@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun observeSettings(userId: String): Flow<AppSettings> {
        return context.settingsDataStore.data.map { preferences ->
            AppSettings(
                defaultPeriod = RecapPeriod.fromKey(
                    preferences[defaultPeriodKey(userId)]
                        ?: RecapPeriod.Month.key
                ),
                notificationsEnabled =
                    preferences[notificationsKey(userId)] ?: false
            )
        }
    }

    suspend fun setDefaultPeriod(
        userId: String,
        period: RecapPeriod
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[defaultPeriodKey(userId)] = period.key
        }
    }

    suspend fun setNotificationsEnabled(
        userId: String,
        enabled: Boolean
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[notificationsKey(userId)] = enabled
        }
    }

    private fun defaultPeriodKey(userId: String) =
        stringPreferencesKey("default_recap_period_$userId")

    private fun notificationsKey(userId: String) =
        booleanPreferencesKey("notifications_enabled_$userId")
}