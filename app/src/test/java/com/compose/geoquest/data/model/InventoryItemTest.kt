package com.compose.geoquest.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for InventoryItem model
 */
class InventoryItemTest {

    @Test
    fun `inventoryItem has correct default values`() {
        val item = InventoryItem(
            treasureId = "treasure_1",
            rewardType = "GOLD",
            rewardName = "Golden Coins",
            value = 100,
            collectedLatitude = 30.65,
            collectedLongitude = 32.05
        )

        assertEquals(0, item.id) // Auto-generated, default 0
        assertNotNull(item.collectedAt)
        assertTrue(item.collectedAt <= System.currentTimeMillis())
    }

    @Test
    fun `inventoryItem stores all properties correctly`() {
        val timestamp = System.currentTimeMillis()
        val item = InventoryItem(
            id = 1,
            treasureId = "treasure_123",
            rewardType = "GEM",
            rewardName = "Ruby",
            value = 250,
            collectedLatitude = 30.6500,
            collectedLongitude = 32.0500,
            collectedAt = timestamp
        )

        assertEquals(1, item.id)
        assertEquals("treasure_123", item.treasureId)
        assertEquals("GEM", item.rewardType)
        assertEquals("Ruby", item.rewardName)
        assertEquals(250, item.value)
        assertEquals(30.6500, item.collectedLatitude, 0.0001)
        assertEquals(32.0500, item.collectedLongitude, 0.0001)
        assertEquals(timestamp, item.collectedAt)
    }

    @Test
    fun `inventoryItem equality works correctly`() {
        val item1 = InventoryItem(
            id = 1,
            treasureId = "treasure_1",
            rewardType = "GOLD",
            rewardName = "Coins",
            value = 100,
            collectedLatitude = 30.65,
            collectedLongitude = 32.05,
            collectedAt = 1000L
        )

        val item2 = InventoryItem(
            id = 1,
            treasureId = "treasure_1",
            rewardType = "GOLD",
            rewardName = "Coins",
            value = 100,
            collectedLatitude = 30.65,
            collectedLongitude = 32.05,
            collectedAt = 1000L
        )

        assertEquals(item1, item2)
    }

    @Test
    fun `inventoryItem copy works correctly`() {
        val original = InventoryItem(
            treasureId = "treasure_1",
            rewardType = "GOLD",
            rewardName = "Coins",
            value = 100,
            collectedLatitude = 30.65,
            collectedLongitude = 32.05
        )

        val copy = original.copy(value = 200)

        assertEquals(200, copy.value)
        assertEquals(original.treasureId, copy.treasureId)
        assertEquals(original.rewardType, copy.rewardType)
    }
}

