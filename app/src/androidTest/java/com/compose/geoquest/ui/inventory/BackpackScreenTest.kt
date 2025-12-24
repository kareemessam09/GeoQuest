package com.compose.geoquest.ui.inventory

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.compose.geoquest.data.model.InventoryItem
import com.compose.geoquest.ui.theme.GeoQuestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for BackpackScreen components
 */
@RunWith(AndroidJUnit4::class)
class BackpackScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyBackpack_displaysEmptyMessage() {
        composeTestRule.setContent {
            GeoQuestTheme {
                EmptyBackpack()
            }
        }

        composeTestRule
            .onNodeWithText("Your backpack is empty!")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Go find some treasures on the map")
            .assertIsDisplayed()
    }

    @Test
    fun emptyBackpack_displaysBackpackEmoji() {
        composeTestRule.setContent {
            GeoQuestTheme {
                EmptyBackpack()
            }
        }

        composeTestRule
            .onNodeWithText("🎒")
            .assertIsDisplayed()
    }

    @Test
    fun statsHeader_displaysTreasureCount() {
        composeTestRule.setContent {
            GeoQuestTheme {
                StatsHeader(itemCount = 5, totalValue = 500)
            }
        }

        composeTestRule
            .onNodeWithText("5")
            .assertIsDisplayed()
    }

    @Test
    fun statsHeader_displaysTotalValue() {
        composeTestRule.setContent {
            GeoQuestTheme {
                StatsHeader(itemCount = 5, totalValue = 500)
            }
        }

        composeTestRule
            .onNodeWithText("500")
            .assertIsDisplayed()
    }

    @Test
    fun statsHeader_displaysTreasuresLabel() {
        composeTestRule.setContent {
            GeoQuestTheme {
                StatsHeader(itemCount = 5, totalValue = 500)
            }
        }

        composeTestRule
            .onNodeWithText("Treasures")
            .assertIsDisplayed()
    }

    @Test
    fun statsHeader_displaysTotalPointsLabel() {
        composeTestRule.setContent {
            GeoQuestTheme {
                StatsHeader(itemCount = 5, totalValue = 500)
            }
        }

        composeTestRule
            .onNodeWithText("Total Points")
            .assertIsDisplayed()
    }

    @Test
    fun inventoryItemCard_displaysItemName() {
        val item = InventoryItem(
            treasureId = "t1",
            rewardType = "GOLD",
            rewardName = "Ancient Coins",
            value = 100,
            collectedLatitude = 30.65,
            collectedLongitude = 32.05
        )

        composeTestRule.setContent {
            GeoQuestTheme {
                InventoryItemCard(item = item)
            }
        }

        composeTestRule
            .onNodeWithText("Ancient Coins")
            .assertIsDisplayed()
    }

    @Test
    fun inventoryItemCard_displaysItemValue() {
        val item = InventoryItem(
            treasureId = "t1",
            rewardType = "GEM",
            rewardName = "Ruby",
            value = 250,
            collectedLatitude = 30.65,
            collectedLongitude = 32.05
        )

        composeTestRule.setContent {
            GeoQuestTheme {
                InventoryItemCard(item = item)
            }
        }

        composeTestRule
            .onNodeWithText("+250 pts")
            .assertIsDisplayed()
    }

    @Test
    fun inventoryItemCard_displaysGoldEmoji() {
        val item = InventoryItem(
            treasureId = "t1",
            rewardType = "GOLD",
            rewardName = "Coins",
            value = 100,
            collectedLatitude = 30.65,
            collectedLongitude = 32.05
        )

        composeTestRule.setContent {
            GeoQuestTheme {
                InventoryItemCard(item = item)
            }
        }

        composeTestRule
            .onNodeWithText("🪙")
            .assertIsDisplayed()
    }

    @Test
    fun inventoryItemCard_displaysGemEmoji() {
        val item = InventoryItem(
            treasureId = "t1",
            rewardType = "GEM",
            rewardName = "Ruby",
            value = 200,
            collectedLatitude = 30.65,
            collectedLongitude = 32.05
        )

        composeTestRule.setContent {
            GeoQuestTheme {
                InventoryItemCard(item = item)
            }
        }

        composeTestRule
            .onNodeWithText("💎")
            .assertIsDisplayed()
    }
}
