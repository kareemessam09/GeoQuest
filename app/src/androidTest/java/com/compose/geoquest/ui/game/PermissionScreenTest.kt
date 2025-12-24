package com.compose.geoquest.ui.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
            .onNodeWithText("Permissions Required")
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
            .onNodeWithText("Grant Permissions")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun permissionScreen_displaysRationale() {
        composeTestRule.setContent {
            GeoQuestTheme {
                PermissionScreen(onPermissionGranted = {})
            }
        }

        composeTestRule
            .onNodeWithText("To hunt for treasures, we need:", substring = true)
            .assertIsDisplayed()
    }
}

