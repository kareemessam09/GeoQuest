package com.compose.geoquest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.compose.geoquest.data.model.InventoryItem

@Database(
    entities = [
        InventoryItem::class,
        AchievementEntity::class,
        UserStatsEntity::class,
        SpawnedTreasureEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GeoQuestDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun achievementDao(): AchievementDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun spawnedTreasureDao(): SpawnedTreasureDao
}

