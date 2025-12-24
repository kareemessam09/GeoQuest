package com.compose.geoquest.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.compose.geoquest.data.model.Achievement
import com.compose.geoquest.data.model.AchievementRequirement
import com.compose.geoquest.ui.theme.GeoQuestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for AchievementNotification component
 */
@RunWith(AndroidJUnit4::class)
class AchievementNotificationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTestAchievement() = Achievement(
        id = "first_find",
        title = "First Find",
        description = "Collect your first treasure",
        iconEmoji = "🏆",
        requirement = AchievementRequirement.FirstTreasure,
        isUnlocked = true
    )

    @Test
    fun achievementNotification_displaysWhenAchievementNotNull() {
        composeTestRule.setContent {
            GeoQuestTheme {
                AchievementNotification(
                    achievement = createTestAchievement(),
                    onDismiss = {}
                )
            }
        }

        // The actual text in the component includes emoji
        composeTestRule
            .onNodeWithText("🎉 Achievement Unlocked!")
            .assertIsDisplayed()
    }

    @Test
    fun achievementNotification_displaysAchievementTitle() {
        composeTestRule.setContent {
            GeoQuestTheme {
                AchievementNotification(
                    achievement = createTestAchievement(),
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("First Find")
            .assertIsDisplayed()
    }

    @Test
    fun achievementNotification_displaysAchievementIcon() {
        composeTestRule.setContent {
            GeoQuestTheme {
                AchievementNotification(
                    achievement = createTestAchievement(),
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("🏆")
            .assertIsDisplayed()
    }

    @Test
    fun achievementNotification_notDisplayedWhenNull() {
        composeTestRule.setContent {
            GeoQuestTheme {
                AchievementNotification(
                    achievement = null,
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("🎉 Achievement Unlocked!")
            .assertDoesNotExist()
    }
}
