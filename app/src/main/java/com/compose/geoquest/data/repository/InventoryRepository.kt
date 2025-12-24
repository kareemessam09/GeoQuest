package com.compose.geoquest.data.repository

import com.compose.geoquest.data.local.InventoryDao
import com.compose.geoquest.data.model.InventoryItem
import com.compose.geoquest.data.model.Treasure
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing collected inventory items
 */
@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryDao: InventoryDao
) {

    fun getAllItems(): Flow<List<InventoryItem>> = inventoryDao.getAllItems()

    fun getTotalValue(): Flow<Int?> = inventoryDao.getTotalValue()

    fun getItemCount(): Flow<Int> = inventoryDao.getItemCount()

    suspend fun collectTreasure(treasure: Treasure, userLat: Double, userLng: Double): Boolean {
        // Check if already collected
        val existing = inventoryDao.getItemByTreasureId(treasure.id)
        if (existing != null) {
            return false // Already collected
        }

        val item = InventoryItem(
            treasureId = treasure.id,
            rewardType = treasure.reward.type.name,
            rewardName = treasure.reward.name,
            value = treasure.reward.value,
            collectedLatitude = userLat,
            collectedLongitude = userLng
        )

        inventoryDao.insertItem(item)
        return true
    }


    suspend fun clearInventory() {
        inventoryDao.clearAll()
    }
}

