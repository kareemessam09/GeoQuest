package com.compose.geoquest.ui.achievements

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.compose.geoquest.data.local.UserStatsEntity
import com.compose.geoquest.data.model.Achievement
import com.compose.geoquest.data.model.AchievementRequirement
import com.compose.geoquest.ui.theme.GeoQuestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for AchievementsScreen components
 */
@RunWith(AndroidJUnit4::class)
class AchievementsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTestAchievement(unlocked: Boolean = false) = Achievement(
        id = "first_find",
        title = "First Find",
        description = "Collect your first treasure",
        iconEmoji = "🏆",
        requirement = AchievementRequirement.FirstTreasure,
        isUnlocked = unlocked
    )

    @Test
    fun achievementCard_displaysAchievementTitle() {
        composeTestRule.setContent {
            GeoQuestTheme {
                AchievementCard(achievement = createTestAchievement())
            }
        }

        composeTestRule
            .onNodeWithText("First Find")
            .assertIsDisplayed()
    }

    @Test
    fun achievementCard_displaysAchievementDescription() {
        composeTestRule.setContent {
            GeoQuestTheme {
                AchievementCard(achievement = createTestAchievement())
            }
        }

        composeTestRule
            .onNodeWithText("Collect your first treasure")
            .assertIsDisplayed()
    }

    @Test
    fun achievementCard_displaysIcon() {
        composeTestRule.setContent {
            GeoQuestTheme {
                AchievementCard(achievement = createTestAchievement(unlocked = true))
            }
        }

        composeTestRule
            .onNodeWithText("🏆")
            .assertIsDisplayed()
    }

    @Test
    fun achievementCard_showsLockedIconWhenLocked() {
        composeTestRule.setContent {
            GeoQuestTheme {
                AchievementCard(achievement = createTestAchievement(unlocked = false))
            }
        }

        // The locked state uses an Icon with contentDescription "Locked"
        composeTestRule
            .onNodeWithContentDescription("Locked")
            .assertIsDisplayed()
    }

    @Test
    fun statsCard_displaysTreasureCount() {
        val stats = UserStatsEntity(
            totalTreasuresCollected = 10,
            totalPointsEarned = 500,
            totalDistanceWalked = 1500f
        )

        composeTestRule.setContent {
            GeoQuestTheme {
                StatsCard(stats = stats)
            }
        }

        composeTestRule
            .onNodeWithText("10")
            .assertIsDisplayed()
    }

    @Test
    fun statsCard_displaysPoints() {
        val stats = UserStatsEntity(
            totalTreasuresCollected = 10,
            totalPointsEarned = 500,
            totalDistanceWalked = 1500f
        )

        composeTestRule.setContent {
            GeoQuestTheme {
                StatsCard(stats = stats)
            }
        }

        composeTestRule
            .onNodeWithText("500")
            .assertIsDisplayed()
    }

    @Test
    fun statsCard_displaysDistance() {
        val stats = UserStatsEntity(
            totalTreasuresCollected = 10,
            totalPointsEarned = 500,
            totalDistanceWalked = 1500f
        )

        composeTestRule.setContent {
            GeoQuestTheme {
                StatsCard(stats = stats)
            }
        }

        // 1500m = 1.5km
        composeTestRule
            .onNodeWithText("1.5 km")
            .assertIsDisplayed()
    }

    @Test
    fun statsCard_displaysLabels() {
        val stats = UserStatsEntity(
            totalTreasuresCollected = 10,
            totalPointsEarned = 500,
            totalDistanceWalked = 1500f
        )

        composeTestRule.setContent {
            GeoQuestTheme {
                StatsCard(stats = stats)
            }
        }

        composeTestRule
            .onNodeWithText("Treasures")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Points")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Distance")
            .assertIsDisplayed()
    }
}
