package com.compose.geoquest.data.model

import android.location.Location


data class Treasure(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val reward: TreasureReward,
    val isCollected: Boolean = false
) {

    fun toLocation(): Location {
        return Location("treasure").apply {
            latitude = this@Treasure.latitude
            longitude = this@Treasure.longitude
        }
    }
}


enum class RewardType {
    GOLD,
    GEM,
    ARTIFACT,
    RARE_ARTIFACT
}


data class TreasureReward(
    val type: RewardType,
    val name: String,
    val value: Int,
    val iconResName: String = "ic_treasure"
)

