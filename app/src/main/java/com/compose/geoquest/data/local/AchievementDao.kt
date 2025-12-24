package com.compose.geoquest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements")
    fun getAllUnlockedAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    suspend fun getAchievement(achievementId: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockAchievement(achievement: AchievementEntity)

    @Query("SELECT COUNT(*) FROM achievements")
    fun getUnlockedCount(): Flow<Int>
}

@Dao
interface UserStatsDao {

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getStats(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getStatsOnce(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStatsEntity)

    @Query("UPDATE user_stats SET totalTreasuresCollected = totalTreasuresCollected + 1, totalPointsEarned = totalPointsEarned + :points WHERE id = 1")
    suspend fun incrementTreasureCollected(points: Int)

    @Query("UPDATE user_stats SET totalDistanceWalked = totalDistanceWalked + :distance WHERE id = 1")
    suspend fun addDistance(distance: Float)

    @Query("UPDATE user_stats SET lastPlayDate = :timestamp WHERE id = 1")
    suspend fun updateLastPlayDate(timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_stats SET fastestCollectionTimeMs = :timeMs WHERE id = 1 AND (fastestCollectionTimeMs IS NULL OR fastestCollectionTimeMs > :timeMs)")
    suspend fun updateFastestCollection(timeMs: Long)
}

