package com.parisara.cycle.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {
    private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    private val THEME_OPTION_KEY = stringPreferencesKey("theme_option")

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
        }

    val themeOption: Flow<ThemeOption> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeStr = preferences[THEME_OPTION_KEY] ?: ThemeOption.SYSTEM.name
            try {
                ThemeOption.valueOf(themeStr)
            } catch (e: IllegalArgumentException) {
                ThemeOption.SYSTEM
            }
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setThemeOption(option: ThemeOption) {
        context.dataStore.edit { preferences ->
            preferences[THEME_OPTION_KEY] = option.name
        }
    }
}

enum class ThemeOption {
    SYSTEM, LIGHT, DARK
}
