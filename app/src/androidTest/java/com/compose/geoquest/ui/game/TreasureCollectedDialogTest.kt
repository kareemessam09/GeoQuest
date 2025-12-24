package com.compose.geoquest.ui.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.compose.geoquest.data.model.RewardType
import com.compose.geoquest.data.model.Treasure
import com.compose.geoquest.data.model.TreasureReward
import com.compose.geoquest.ui.theme.GeoQuestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for TreasureCollectedDialog
 */
@RunWith(AndroidJUnit4::class)
class TreasureCollectedDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTreasure = Treasure(
        id = "test_1",
        name = "Golden Chest",
        latitude = 30.65,
        longitude = 32.05,
        reward = TreasureReward(
            type = RewardType.GOLD,
            name = "Ancient Coins",
            value = 150
        )
    )

    @Test
    fun treasureDialog_displaysTreasureName() {
        composeTestRule.setContent {
            GeoQuestTheme {
                TreasureCollectedDialog(
                    treasure = testTreasure,
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Golden Chest")
            .assertIsDisplayed()
    }

    @Test
    fun treasureDialog_displaysRewardName() {
        composeTestRule.setContent {
            GeoQuestTheme {
                TreasureCollectedDialog(
                    treasure = testTreasure,
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Ancient Coins")
            .assertIsDisplayed()
    }

    @Test
    fun treasureDialog_displaysPointsValue() {
        composeTestRule.setContent {
            GeoQuestTheme {
                TreasureCollectedDialog(
                    treasure = testTreasure,
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("+150 points", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun treasureDialog_displaysCollectButton() {
        composeTestRule.setContent {
            GeoQuestTheme {
                TreasureCollectedDialog(
                    treasure = testTreasure,
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Awesome!")
            .assertIsDisplayed()
    }

    @Test
    fun treasureDialog_dismissesOnButtonClick() {
        var dismissed = false

        composeTestRule.setContent {
            GeoQuestTheme {
                TreasureCollectedDialog(
                    treasure = testTreasure,
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Awesome!")
            .performClick()

        assert(dismissed)
    }

    @Test
    fun treasureDialog_displaysGemReward() {
        val gemTreasure = testTreasure.copy(
            reward = TreasureReward(
                type = RewardType.GEM,
                name = "Ruby",
                value = 250
            )
        )

        composeTestRule.setContent {
            GeoQuestTheme {
                TreasureCollectedDialog(
                    treasure = gemTreasure,
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Ruby")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("+250 points", substring = true)
            .assertIsDisplayed()
    }
}

