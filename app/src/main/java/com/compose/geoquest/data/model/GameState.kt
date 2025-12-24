package com.compose.geoquest.data.model

/**
 * Represents the current game state
 */
data class GameState(
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val selectedTreasure: Treasure? = null,
    val distanceToTarget: Float? = null,
    val isInRange: Boolean = false,
    val treasures: List<Treasure> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    companion object {
        // Distance in meters to unlock a treasure
        const val UNLOCK_RADIUS_METERS = 15f

        // Distance in meters to show treasure on map
        const val VISIBILITY_RADIUS_METERS = 1000f
    }
}

/**
 * Proximity level for "hot and cold" feedback
 */
enum class ProximityLevel {
    FREEZING,   // > 500m
    COLD,       // 200-500m
    COOL,       // 100-200m
    WARM,       // 50-100m
    HOT,        // 15-50m
    BURNING     // < 15m (in range!)
}

/**
 * Extension function to determine proximity level from distance
 */
fun Float.toProximityLevel(): ProximityLevel {
    return when {
        this <= GameState.UNLOCK_RADIUS_METERS -> ProximityLevel.BURNING
        this <= 50f -> ProximityLevel.HOT
        this <= 100f -> ProximityLevel.WARM
        this <= 200f -> ProximityLevel.COOL
        this <= 500f -> ProximityLevel.COLD
        else -> ProximityLevel.FREEZING
    }
}

