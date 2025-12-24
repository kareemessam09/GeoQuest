package com.compose.geoquest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.compose.geoquest.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventory_items ORDER BY collectedAt DESC")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE treasureId = :treasureId LIMIT 1")
    suspend fun getItemByTreasureId(treasureId: String): InventoryItem?

    @Insert
    suspend fun insertItem(item: InventoryItem)

    @Query("SELECT SUM(value) FROM inventory_items")
    fun getTotalValue(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM inventory_items")
    fun getItemCount(): Flow<Int>

    @Query("DELETE FROM inventory_items")
    suspend fun clearAll()
}

