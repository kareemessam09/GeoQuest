package com.compose.geoquest.data.local

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SpawnedTreasureEntity
 */
class SpawnedTreasureEntityTest {

    @Test
    fun `entity has correct default values`() {
        val entity = SpawnedTreasureEntity(
            id = "treasure_1",
            name = "Test Treasure",
            latitude = 30.65,
            longitude = 32.05,
            rewardType = "GOLD",
            rewardName = "Coins",
            rewardValue = 100
        )

        assertFalse(entity.isCollected)
        assertTrue(entity.spawnedAt <= System.currentTimeMillis())
    }

    @Test
    fun `entity stores coordinates correctly`() {
        val entity = SpawnedTreasureEntity(
            id = "treasure_1",
            name = "Test Treasure",
            latitude = 30.6500,
            longitude = 32.0500,
            rewardType = "GOLD",
            rewardName = "Coins",
            rewardValue = 100
        )

        assertEquals(30.6500, entity.latitude, 0.0001)
        assertEquals(32.0500, entity.longitude, 0.0001)
    }

    @Test
    fun `entity reward types are stored as strings`() {
        val goldEntity = SpawnedTreasureEntity(
            id = "t1", name = "Gold", latitude = 0.0, longitude = 0.0,
            rewardType = "GOLD", rewardName = "Coins", rewardValue = 100
        )
        val gemEntity = SpawnedTreasureEntity(
            id = "t2", name = "Gem", latitude = 0.0, longitude = 0.0,
            rewardType = "GEM", rewardName = "Ruby", rewardValue = 200
        )
        val artifactEntity = SpawnedTreasureEntity(
            id = "t3", name = "Artifact", latitude = 0.0, longitude = 0.0,
            rewardType = "ARTIFACT", rewardName = "Scarab", rewardValue = 300
        )

        assertEquals("GOLD", goldEntity.rewardType)
        assertEquals("GEM", gemEntity.rewardType)
        assertEquals("ARTIFACT", artifactEntity.rewardType)
    }

    @Test
    fun `entity copy marks as collected`() {
        val original = SpawnedTreasureEntity(
            id = "treasure_1",
            name = "Test",
            latitude = 0.0,
            longitude = 0.0,
            rewardType = "GOLD",
            rewardName = "Coins",
            rewardValue = 100,
            isCollected = false
        )

        val collected = original.copy(isCollected = true)

        assertFalse(original.isCollected)
        assertTrue(collected.isCollected)
    }

    @Test
    fun `entity equality works correctly`() {
        val entity1 = SpawnedTreasureEntity(
            id = "treasure_1",
            name = "Test",
            latitude = 30.65,
            longitude = 32.05,
            rewardType = "GOLD",
            rewardName = "Coins",
            rewardValue = 100,
            spawnedAt = 1000L
        )

        val entity2 = SpawnedTreasureEntity(
            id = "treasure_1",
            name = "Test",
            latitude = 30.65,
            longitude = 32.05,
            rewardType = "GOLD",
            rewardName = "Coins",
            rewardValue = 100,
            spawnedAt = 1000L
        )

        assertEquals(entity1, entity2)
    }
}

