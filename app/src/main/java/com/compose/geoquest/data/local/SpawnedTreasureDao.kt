package com.compose.geoquest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpawnedTreasureDao {

    @Query("SELECT * FROM spawned_treasures WHERE isCollected = 0")
    fun getAvailableTreasures(): Flow<List<SpawnedTreasureEntity>>

    @Query("SELECT * FROM spawned_treasures")
    fun getAllTreasures(): Flow<List<SpawnedTreasureEntity>>

    @Query("SELECT * FROM spawned_treasures WHERE id = :id")
    suspend fun getTreasureById(id: String): SpawnedTreasureEntity?

    @Query("SELECT COUNT(*) FROM spawned_treasures")
    suspend fun getTreasureCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreasures(treasures: List<SpawnedTreasureEntity>)

    @Query("UPDATE spawned_treasures SET isCollected = 1 WHERE id = :treasureId")
    suspend fun markAsCollected(treasureId: String)

    @Query("DELETE FROM spawned_treasures")
    suspend fun deleteAll()

    @Query("DELETE FROM spawned_treasures WHERE spawnedAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}

