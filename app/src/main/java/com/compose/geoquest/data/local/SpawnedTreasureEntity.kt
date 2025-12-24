package com.compose.geoquest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "spawned_treasures")
data class SpawnedTreasureEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val rewardType: String,
    val rewardName: String,
    val rewardValue: Int,
    val isCollected: Boolean = false,
    val spawnedAt: Long = System.currentTimeMillis()
)

