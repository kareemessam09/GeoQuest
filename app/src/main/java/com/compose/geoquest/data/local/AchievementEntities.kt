package com.compose.geoquest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val unlockedAt: Long = System.currentTimeMillis()
)


@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val id: Int = 1,
    val totalTreasuresCollected: Int = 0,
    val totalPointsEarned: Int = 0,
    val totalDistanceWalked: Float = 0f,
    val totalPlayTimeMinutes: Long = 0,
    val firstPlayDate: Long = System.currentTimeMillis(),
    val lastPlayDate: Long = System.currentTimeMillis(),
    val fastestCollectionTimeMs: Long? = null
)

