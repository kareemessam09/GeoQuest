package com.compose.geoquest.ui.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.compose.geoquest.ui.theme.GeoQuestTheme
import com.compose.geoquest.ui.game.PermissionScreen
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun permissionScreen_canBeDisplayed() {
        composeTestRule.setContent {
            GeoQuestTheme {
                PermissionScreen(onPermissionGranted = {})
            }
        }

        composeTestRule
            .onNodeWithText("Location Permission Required")
            .assertIsDisplayed()
    }
}
