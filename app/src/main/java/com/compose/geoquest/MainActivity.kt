package com.compose.geoquest

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.compose.geoquest.data.preferences.UserPreferences
import com.compose.geoquest.ui.game.PermissionScreen
import com.compose.geoquest.ui.navigation.GeoQuestNavHost
import com.compose.geoquest.ui.theme.GeoQuestTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkModePreference by userPreferences.darkMode.collectAsState(initial = "system")

            val isDarkTheme = when (darkModePreference) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            GeoQuestTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GeoQuestApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GeoQuestApp() {
    val navController = rememberNavController()

    // Build permission list based on Android version
    val permissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        // Notification permission required for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    // Check if location permission is granted (required)
    val hasLocationPermission = permissionState.permissions
        .find { it.permission == Manifest.permission.ACCESS_FINE_LOCATION }
        ?.status?.isGranted == true

    var hasPermission by remember { mutableStateOf(hasLocationPermission) }

    // Update permission state when it changes
    LaunchedEffect(hasLocationPermission) {
        hasPermission = hasLocationPermission
    }

    if (hasPermission) {
        GeoQuestNavHost(navController = navController)
    } else {
        PermissionScreen(
            onPermissionGranted = {
                hasPermission = true
            }
        )
    }
}