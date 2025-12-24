package com.compose.geoquest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for collected inventory items
 * Persisted locally on the device
 */
@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val treasureId: String,
    val rewardType: String,
    val rewardName: String,
    val value: Int,
    val collectedAt: Long = System.currentTimeMillis(),
    val collectedLatitude: Double,
    val collectedLongitude: Double
)

