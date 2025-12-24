package com.compose.geoquest.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val highAccuracyMode by viewModel.highAccuracyMode.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val units by viewModel.units.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "Feedback") {
                SwitchSettingItem(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Feedback",
                    subtitle = "Vibrate when near treasures",
                    checked = hapticEnabled,
                    onCheckedChange = viewModel::setHapticEnabled
                )

                SwitchSettingItem(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Sound Effects",
                    subtitle = "Play sounds for actions",
                    checked = soundEnabled,
                    onCheckedChange = viewModel::setSoundEnabled
                )
            }

            SettingsSection(title = "Location") {
                SwitchSettingItem(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "High Accuracy Mode",
                    subtitle = "Better GPS precision, more battery usage",
                    checked = highAccuracyMode,
                    onCheckedChange = viewModel::setHighAccuracyMode
                )

                DropdownSettingItem(
                    icon = Icons.Default.Straighten,
                    title = "Distance Units",
                    subtitle = if (units == "metric") "Meters / Kilometers" else "Feet / Miles",
                    selectedValue = units,
                    options = listOf("metric" to "Metric (m, km)", "imperial" to "Imperial (ft, mi)"),
                    onValueSelected = viewModel::setUnits
                )
            }

            SettingsSection(title = "Appearance") {
                DropdownSettingItem(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    subtitle = when (darkMode) {
                        "light" -> "Light Mode"
                        "dark" -> "Dark Mode"
                        else -> "System Default"
                    },
                    selectedValue = darkMode,
                    options = listOf(
                        "system" to "System Default",
                        "light" to "Light Mode",
                        "dark" to "Dark Mode"
                    ),
                    onValueSelected = viewModel::setDarkMode
                )
            }

            SettingsSection(title = "Notifications") {
                SwitchSettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Get notified about nearby treasures",
                    checked = notificationEnabled,
                    onCheckedChange = viewModel::setNotificationEnabled
                )
            }

            Spacer(modifier = Modifier.height(16.dp))



        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            content()
        }
    }
}

@Composable
fun SwitchSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun DropdownSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selectedValue: String,
    options: List<Pair<String, String>>,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onValueSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

