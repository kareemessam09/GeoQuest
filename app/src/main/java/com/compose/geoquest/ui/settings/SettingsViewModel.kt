package com.compose.geoquest.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.geoquest.data.preferences.UserPreferences
import com.compose.geoquest.widget.TreasureDistanceWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val hapticEnabled: StateFlow<Boolean> = userPreferences.hapticEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val soundEnabled: StateFlow<Boolean> = userPreferences.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val highAccuracyMode: StateFlow<Boolean> = userPreferences.highAccuracyMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val darkMode: StateFlow<String> = userPreferences.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val notificationEnabled: StateFlow<Boolean> = userPreferences.notificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val units: StateFlow<String> = userPreferences.units
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "metric")

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.setHapticEnabled(enabled)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.setSoundEnabled(enabled)
        }
    }

    fun setHighAccuracyMode(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.setHighAccuracyMode(enabled)
        }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.setDarkMode(mode)
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.setNotificationEnabled(enabled)
        }
    }

    fun setUnits(units: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.setUnits(units)
            // Update widget with new units
            TreasureDistanceWidget.setUnits(context, units)
        }
    }
}

