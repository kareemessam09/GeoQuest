package com.compose.geoquest.data.model


data class GameState(
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val selectedTreasure: Treasure? = null,
    val distanceToTarget: Float? = null,
    val treasures: List<Treasure> = emptyList(),
    val nearbyTreasureIds: Set<String> = emptySet(), // Treasures within geofence (100m)
    val isLoading: Boolean = true,
    val error: String? = null
) {
    companion object {
        const val COLLECT_RADIUS_METERS = 20f

        const val NOTIFICATION_RADIUS_METERS = 100f

        const val VISIBILITY_RADIUS_METERS = 1000f
    }

    val canCollectSelected: Boolean
        get() = selectedTreasure != null &&
                distanceToTarget != null &&
                distanceToTarget <= COLLECT_RADIUS_METERS


    val isNearby: Boolean
        get() = distanceToTarget != null &&
                distanceToTarget <= NOTIFICATION_RADIUS_METERS &&
                distanceToTarget > COLLECT_RADIUS_METERS
}



enum class ProximityLevel {
    FREEZING,   // > 500m
    COLD,       // 200-500m
    COOL,       // 100-200m
    WARM,       // 50-100m
    HOT,        // 15-50m
    BURNING     // < 15m
}



fun Float.toProximityLevel(): ProximityLevel {
    return when {
        this <= 15f -> ProximityLevel.BURNING
        this <= 50f -> ProximityLevel.HOT
        this <= 100f -> ProximityLevel.WARM
        this <= 200f -> ProximityLevel.COOL
        this <= 500f -> ProximityLevel.COLD
        else -> ProximityLevel.FREEZING
    }
}

