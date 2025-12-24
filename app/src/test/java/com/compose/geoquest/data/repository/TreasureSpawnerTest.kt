package com.compose.geoquest.data.repository

import com.compose.geoquest.data.local.SpawnedTreasureDao
import com.compose.geoquest.data.local.SpawnedTreasureEntity
import com.compose.geoquest.data.model.RewardType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TreasureSpawner
 * Tests dynamic treasure generation logic
 */
class TreasureSpawnerTest {

    private lateinit var spawnedTreasureDao: SpawnedTreasureDao
    private lateinit var treasureSpawner: TreasureSpawner

    @Before
    fun setup() {
        spawnedTreasureDao = mockk(relaxed = true)
        treasureSpawner = TreasureSpawner(spawnedTreasureDao)
    }

    @Test
    fun `getAvailableTreasures returns mapped treasures from dao`() = runTest {
        // Given
        val entities = listOf(
            SpawnedTreasureEntity(
                id = "treasure_1",
                name = "Test Treasure",
                latitude = 30.65,
                longitude = 32.05,
                rewardType = "GOLD",
                rewardName = "Golden Coins",
                rewardValue = 100,
                isCollected = false
            )
        )
        coEvery { spawnedTreasureDao.getAvailableTreasures() } returns flowOf(entities)

        // When
        val treasures = treasureSpawner.getAvailableTreasures().first()

        // Then
        assertEquals(1, treasures.size)
        assertEquals("treasure_1", treasures[0].id)
        assertEquals("Test Treasure", treasures[0].name)
        assertEquals(RewardType.GOLD, treasures[0].reward.type)
    }

    @Test
    fun `ensureTreasuresSpawned spawns treasures when count is below minimum`() = runTest {
        // Given
        coEvery { spawnedTreasureDao.getTreasureCount() } returns 0
        coEvery { spawnedTreasureDao.deleteOlderThan(any()) } returns Unit
        coEvery { spawnedTreasureDao.insertTreasures(any()) } returns Unit

        // When
        val spawned = treasureSpawner.ensureTreasuresSpawned(30.65, 32.05)

        // Then
        assertTrue(spawned)
        coVerify { spawnedTreasureDao.insertTreasures(any()) }
    }

    @Test
    fun `ensureTreasuresSpawned does not spawn when count is above minimum`() = runTest {
        // Given
        coEvery { spawnedTreasureDao.getTreasureCount() } returns 10
        coEvery { spawnedTreasureDao.deleteOlderThan(any()) } returns Unit

        // When
        val spawned = treasureSpawner.ensureTreasuresSpawned(30.65, 32.05)

        // Then
        assertFalse(spawned)
        coVerify(exactly = 0) { spawnedTreasureDao.insertTreasures(any()) }
    }

    @Test
    fun `markTreasureCollected calls dao`() = runTest {
        // Given
        val treasureId = "treasure_123"
        coEvery { spawnedTreasureDao.markAsCollected(treasureId) } returns Unit

        // When
        treasureSpawner.markTreasureCollected(treasureId)

        // Then
        coVerify { spawnedTreasureDao.markAsCollected(treasureId) }
    }

    @Test
    fun `respawnTreasures deletes all and spawns new`() = runTest {
        // Given
        coEvery { spawnedTreasureDao.deleteAll() } returns Unit
        coEvery { spawnedTreasureDao.insertTreasures(any()) } returns Unit

        // When
        treasureSpawner.respawnTreasures(30.65, 32.05)

        // Then
        coVerify { spawnedTreasureDao.deleteAll() }
        coVerify { spawnedTreasureDao.insertTreasures(any()) }
    }

    @Test
    fun `spawn configuration constants are valid`() {
        assertTrue(TreasureSpawner.MIN_TREASURES > 0)
        assertTrue(TreasureSpawner.MAX_TREASURES >= TreasureSpawner.MIN_TREASURES)
        assertTrue(TreasureSpawner.MIN_DISTANCE > 0)
        assertTrue(TreasureSpawner.MAX_DISTANCE > TreasureSpawner.MIN_DISTANCE)
        assertTrue(TreasureSpawner.RESPAWN_INTERVAL_MS > 0)
    }
}

