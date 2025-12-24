package com.compose.geoquest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.compose.geoquest.ui.achievements.AchievementsScreen
import com.compose.geoquest.ui.game.MapScreen
import com.compose.geoquest.ui.inventory.BackpackScreen
import com.compose.geoquest.ui.settings.SettingsScreen


object Routes {
    const val MAP = "map"
    const val BACKPACK = "backpack"
    const val ACHIEVEMENTS = "achievements"
    const val SETTINGS = "settings"
}

@Composable
fun GeoQuestNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.MAP
    ) {
        composable(Routes.MAP) {
            MapScreen(
                onNavigateToBackpack = {
                    navController.navigate(Routes.BACKPACK)
                },
                onNavigateToAchievements = {
                    navController.navigate(Routes.ACHIEVEMENTS)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.BACKPACK) {
            BackpackScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ACHIEVEMENTS) {
            AchievementsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

