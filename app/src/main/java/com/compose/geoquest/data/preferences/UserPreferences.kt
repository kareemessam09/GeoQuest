package com.compose.geoquest.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HIGH_ACCURACY_MODE = booleanPreferencesKey("high_accuracy_mode")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val UNITS = stringPreferencesKey("units")
    }

    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HAPTIC_ENABLED] ?: true
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTIC_ENABLED] = enabled
        }
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SOUND_ENABLED] ?: true
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    val highAccuracyMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HIGH_ACCURACY_MODE] ?: true
    }

    suspend fun setHighAccuracyMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HIGH_ACCURACY_MODE] = enabled
        }
    }

    val darkMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: "system"
    }

    suspend fun setDarkMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = mode
        }
    }

    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATION_ENABLED] ?: true
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATION_ENABLED] = enabled
        }
    }


    // Distance Units
    val units: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.UNITS] ?: "metric"
    }

    suspend fun setUnits(units: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.UNITS] = units
        }
    }
}
