package com.compose.geoquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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

@Composable
fun GeoQuestApp() {
    val navController = rememberNavController()
    var permissionFlowCompleted by remember { mutableStateOf(false) }

    if (permissionFlowCompleted) {
        GeoQuestNavHost(navController = navController)
    } else {
        PermissionScreen(
            onPermissionGranted = { permissionFlowCompleted = true }
        )
    }
}