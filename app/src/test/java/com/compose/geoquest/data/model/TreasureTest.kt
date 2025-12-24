package com.compose.geoquest.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Treasure model
 */
class TreasureTest {

    @Test
    fun `treasure stores coordinates correctly`() {
        val treasure = Treasure(
            id = "test_1",
            name = "Test Treasure",
            latitude = 30.6500,
            longitude = 32.0500,
            reward = TreasureReward(
                type = RewardType.GOLD,
                name = "Test Gold",
                value = 100
            )
        )

        assertEquals(30.6500, treasure.latitude, 0.0001)
        assertEquals(32.0500, treasure.longitude, 0.0001)
    }

    @Test
    fun `treasure default isCollected is false`() {
        val treasure = Treasure(
            id = "test_1",
            name = "Test Treasure",
            latitude = 30.6500,
            longitude = 32.0500,
            reward = TreasureReward(
                type = RewardType.GEM,
                name = "Ruby",
                value = 250
            )
        )

        assertFalse(treasure.isCollected)
    }

    @Test
    fun `reward types have correct enum values`() {
        assertEquals(4, RewardType.entries.size)
        assertTrue(RewardType.entries.contains(RewardType.GOLD))
        assertTrue(RewardType.entries.contains(RewardType.GEM))
        assertTrue(RewardType.entries.contains(RewardType.ARTIFACT))
        assertTrue(RewardType.entries.contains(RewardType.RARE_ARTIFACT))
    }
}

