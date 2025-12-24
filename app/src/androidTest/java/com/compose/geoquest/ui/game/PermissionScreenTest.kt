package com.compose.geoquest.ui.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.compose.geoquest.ui.theme.GeoQuestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for PermissionScreen
 */
@RunWith(AndroidJUnit4::class)
class PermissionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun permissionScreen_displaysTitle() {
        composeTestRule.setContent {
            GeoQuestTheme {
                PermissionScreen(onPermissionGranted = {})
            }
        }

        composeTestRule
            .onNodeWithText("Location Permission Required")
            .assertIsDisplayed()
    }

    @Test
    fun permissionScreen_displaysLocationEmoji() {
        composeTestRule.setContent {
            GeoQuestTheme {
                PermissionScreen(onPermissionGranted = {})
            }
        }

        composeTestRule
            .onNodeWithText("🗺️")
            .assertIsDisplayed()
    }

    @Test
    fun permissionScreen_displaysGrantButton() {
        composeTestRule.setContent {
            GeoQuestTheme {
                PermissionScreen(onPermissionGranted = {})
            }
        }

        composeTestRule
            .onNodeWithText("Grant Location Permission")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun permissionScreen_displaysDescription() {
        composeTestRule.setContent {
            GeoQuestTheme {
                PermissionScreen(onPermissionGranted = {})
            }
        }

        composeTestRule
            .onNodeWithText("GeoQuest is a GPS-based treasure hunting game", substring = true)
            .assertIsDisplayed()
    }
}
