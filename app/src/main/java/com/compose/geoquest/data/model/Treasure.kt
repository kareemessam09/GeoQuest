package com.compose.geoquest.data.model

import android.location.Location

/**
 * Represents a treasure location in the game
 */
data class Treasure(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val reward: TreasureReward,
    val isCollected: Boolean = false
) {
    /**
     * Convert treasure coordinates to Android Location object
     * Used for distance calculations
     */
    fun toLocation(): Location {
        return Location("treasure").apply {
            latitude = this@Treasure.latitude
            longitude = this@Treasure.longitude
        }
    }
}

/**
 * Types of rewards a treasure can contain
 */
enum class RewardType {
    GOLD,
    GEM,
    ARTIFACT,
    RARE_ARTIFACT
}

/**
 * Reward contained in a treasure chest
 */
data class TreasureReward(
    val type: RewardType,
    val name: String,
    val value: Int,
    val iconResName: String = "ic_treasure" // Resource name for the icon
)

