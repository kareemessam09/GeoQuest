package com.compose.geoquest.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.compose.geoquest.ui.theme.GeoQuestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for SettingsScreen components
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun switchSettingItem_displaysTitle() {
        composeTestRule.setContent {
            GeoQuestTheme {
                SwitchSettingItem(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Feedback",
                    subtitle = "Vibrate when near treasure",
                    checked = true,
                    onCheckedChange = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Haptic Feedback")
            .assertIsDisplayed()
    }

    @Test
    fun switchSettingItem_displaysSubtitle() {
        composeTestRule.setContent {
            GeoQuestTheme {
                SwitchSettingItem(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Feedback",
                    subtitle = "Vibrate when near treasure",
                    checked = true,
                    onCheckedChange = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Vibrate when near treasure")
            .assertIsDisplayed()
    }

    @Test
    fun switchSettingItem_togglesOnClick() {
        var isChecked = false

        composeTestRule.setContent {
            GeoQuestTheme {
                SwitchSettingItem(
                    icon = Icons.Default.Vibration,
                    title = "Test Setting",
                    subtitle = "Test description",
                    checked = isChecked,
                    onCheckedChange = { newValue -> isChecked = newValue }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Test Setting")
            .performClick()

        assert(isChecked)
    }

    @Test
    fun settingsSection_displaysTitle() {
        composeTestRule.setContent {
            GeoQuestTheme {
                SettingsSection(title = "Preferences") {
                    // Empty content
                }
            }
        }

        composeTestRule
            .onNodeWithText("Preferences")
            .assertIsDisplayed()
    }
}

