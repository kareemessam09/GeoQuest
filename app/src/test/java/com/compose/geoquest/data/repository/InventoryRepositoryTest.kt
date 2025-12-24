package com.compose.geoquest.data.repository

import com.compose.geoquest.data.local.InventoryDao
import com.compose.geoquest.data.model.InventoryItem
import com.compose.geoquest.data.model.RewardType
import com.compose.geoquest.data.model.Treasure
import com.compose.geoquest.data.model.TreasureReward
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for InventoryRepository
 * Tests treasure collection and inventory management
 */
class InventoryRepositoryTest {

    private lateinit var inventoryDao: InventoryDao
    private lateinit var inventoryRepository: InventoryRepository

    @Before
    fun setup() {
        inventoryDao = mockk(relaxed = true)
        inventoryRepository = InventoryRepository(inventoryDao)
    }

    @Test
    fun `getAllItems returns items from dao`() = runTest {
        // Given
        val items = listOf(
            InventoryItem(
                id = 1,
                treasureId = "treasure_1",
                rewardType = "GOLD",
                rewardName = "Golden Coins",
                value = 100,
                collectedLatitude = 30.65,
                collectedLongitude = 32.05
            )
        )
        coEvery { inventoryDao.getAllItems() } returns flowOf(items)

        // When
        val result = inventoryRepository.getAllItems().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("treasure_1", result[0].treasureId)
    }

    @Test
    fun `getTotalValue returns sum from dao`() = runTest {
        // Given
        coEvery { inventoryDao.getTotalValue() } returns flowOf(500)

        // When
        val result = inventoryRepository.getTotalValue().first()

        // Then
        assertEquals(500, result)
    }

    @Test
    fun `getItemCount returns count from dao`() = runTest {
        // Given
        coEvery { inventoryDao.getItemCount() } returns flowOf(5)

        // When
        val result = inventoryRepository.getItemCount().first()

        // Then
        assertEquals(5, result)
    }

    @Test
    fun `collectTreasure returns false if already collected`() = runTest {
        // Given
        val treasure = createTestTreasure()
        val existingItem = InventoryItem(
            treasureId = treasure.id,
            rewardType = "GOLD",
            rewardName = "Test",
            value = 100,
            collectedLatitude = 0.0,
            collectedLongitude = 0.0
        )
        coEvery { inventoryDao.getItemByTreasureId(treasure.id) } returns existingItem

        // When
        val result = inventoryRepository.collectTreasure(treasure, 30.65, 32.05)

        // Then
        assertFalse(result)
        coVerify(exactly = 0) { inventoryDao.insertItem(any()) }
    }

    @Test
    fun `collectTreasure returns true and inserts item if not collected`() = runTest {
        // Given
        val treasure = createTestTreasure()
        coEvery { inventoryDao.getItemByTreasureId(treasure.id) } returns null

        val itemSlot = slot<InventoryItem>()
        coEvery { inventoryDao.insertItem(capture(itemSlot)) } returns Unit

        // When
        val result = inventoryRepository.collectTreasure(treasure, 30.65, 32.05)

        // Then
        assertTrue(result)
        coVerify { inventoryDao.insertItem(any()) }

        val capturedItem = itemSlot.captured
        assertEquals(treasure.id, capturedItem.treasureId)
        assertEquals(treasure.reward.type.name, capturedItem.rewardType)
        assertEquals(treasure.reward.name, capturedItem.rewardName)
        assertEquals(treasure.reward.value, capturedItem.value)
        assertEquals(30.65, capturedItem.collectedLatitude, 0.001)
        assertEquals(32.05, capturedItem.collectedLongitude, 0.001)
    }

    @Test
    fun `clearInventory calls dao clearAll`() = runTest {
        // Given
        coEvery { inventoryDao.clearAll() } returns Unit

        // When
        inventoryRepository.clearInventory()

        // Then
        coVerify { inventoryDao.clearAll() }
    }

    private fun createTestTreasure() = Treasure(
        id = "test_treasure",
        name = "Test Treasure",
        latitude = 30.65,
        longitude = 32.05,
        reward = TreasureReward(
            type = RewardType.GOLD,
            name = "Golden Coins",
            value = 100
        )
    )
}

