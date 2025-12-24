package com.compose.geoquest.data.repository

import com.compose.geoquest.data.local.SpawnedTreasureDao
import com.compose.geoquest.data.local.SpawnedTreasureEntity
import com.compose.geoquest.data.model.RewardType
import com.compose.geoquest.data.model.Treasure
import com.compose.geoquest.data.model.TreasureReward
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.random.Random

/**
 * Generates and manages dynamically spawned treasures around user's location
 */
@Singleton
class TreasureSpawner @Inject constructor(
    private val spawnedTreasureDao: SpawnedTreasureDao
) {

    companion object {
        // Spawn configuration
        const val MIN_TREASURES = 5
        const val MAX_TREASURES = 10

        // Distance ranges in meters
        const val MIN_DISTANCE = 100.0    // Minimum 100m away
        const val MAX_DISTANCE = 1000.0   // Maximum 1km away

        // Respawn after 24 hours
        const val RESPAWN_INTERVAL_MS = 24 * 60 * 60 * 1000L

        // Treasure names pool
        private val TREASURE_NAMES = listOf(
            "Ancient Gold Chest",
            "Ruby Cave",
            "Diamond Vault",
            "Emerald Temple",
            "Pharaoh's Tomb",
            "Pirate's Bounty",
            "Dragon's Hoard",
            "Sultan's Treasury",
            "Lost Artifact",
            "Mystic Shrine",
            "Hidden Crypt",
            "Crystal Cavern",
            "Golden Pyramid",
            "Secret Oasis",
            "Buried Fortune"
        )

        // Reward configurations with weights
        private val REWARD_CONFIGS = listOf(
            RewardConfig(RewardType.GOLD, listOf("Golden Coins", "Gold Bars", "Gold Nuggets"), 50..150, weight = 40),
            RewardConfig(RewardType.GEM, listOf("Ruby", "Emerald", "Sapphire", "Amethyst"), 100..300, weight = 30),
            RewardConfig(RewardType.ARTIFACT, listOf("Ancient Scarab", "Pharaoh's Mask", "Mystic Compass", "Old Map"), 200..500, weight = 20),
            RewardConfig(RewardType.RARE_ARTIFACT, listOf("Queen's Crown", "King's Scepter", "Diamond Necklace", "Golden Idol"), 500..1500, weight = 10)
        )
    }

    private data class RewardConfig(
        val type: RewardType,
        val names: List<String>,
        val valueRange: IntRange,
        val weight: Int
    )

    /**
     * Get available (uncollected) treasures as Flow
     */
    fun getAvailableTreasures(): Flow<List<Treasure>> {
        return spawnedTreasureDao.getAvailableTreasures().map { entities ->
            entities.map { it.toTreasure() }
        }
    }

    /**
     * Check if treasures need to be spawned and spawn them if necessary
     */
    suspend fun ensureTreasuresSpawned(userLat: Double, userLng: Double): Boolean {
        // Clean up old treasures (older than respawn interval)
        val cutoffTime = System.currentTimeMillis() - RESPAWN_INTERVAL_MS
        spawnedTreasureDao.deleteOlderThan(cutoffTime)

        // Check if we have enough treasures
        val currentCount = spawnedTreasureDao.getTreasureCount()

        if (currentCount < MIN_TREASURES) {
            // Spawn new treasures
            val treasuresToSpawn = Random.nextInt(MIN_TREASURES, MAX_TREASURES + 1)
            spawnTreasures(userLat, userLng, treasuresToSpawn)
            return true
        }

        return false
    }

    /**
     * Force respawn treasures (for testing or after collecting all)
     */
    suspend fun respawnTreasures(userLat: Double, userLng: Double) {
        spawnedTreasureDao.deleteAll()
        val treasureCount = Random.nextInt(MIN_TREASURES, MAX_TREASURES + 1)
        spawnTreasures(userLat, userLng, treasureCount)
    }

    /**
     * Spawn treasures around the user's location
     */
    private suspend fun spawnTreasures(userLat: Double, userLng: Double, count: Int) {
        val treasures = mutableListOf<SpawnedTreasureEntity>()
        val usedNames = mutableSetOf<String>()

        repeat(count) { index ->
            // Generate random position
            val (lat, lng) = generateRandomPosition(userLat, userLng)

            // Pick a unique name
            var name: String
            do {
                name = TREASURE_NAMES.random()
            } while (name in usedNames && usedNames.size < TREASURE_NAMES.size)
            usedNames.add(name)

            // Pick a reward based on weighted probability
            val reward = pickWeightedReward()

            treasures.add(
                SpawnedTreasureEntity(
                    id = "treasure_${UUID.randomUUID()}",
                    name = name,
                    latitude = lat,
                    longitude = lng,
                    rewardType = reward.type.name,
                    rewardName = reward.name,
                    rewardValue = reward.value
                )
            )
        }

        spawnedTreasureDao.insertTreasures(treasures)
    }

    /**
     * Generate a random position within MIN_DISTANCE to MAX_DISTANCE from center
     */
    private fun generateRandomPosition(centerLat: Double, centerLng: Double): Pair<Double, Double> {
        // Random distance between min and max
        val distance = Random.nextDouble(MIN_DISTANCE, MAX_DISTANCE)

        // Random angle (0 to 360 degrees)
        val angle = Random.nextDouble(0.0, 360.0)

        // Convert to radians
        val angleRad = Math.toRadians(angle)

        // Calculate offset in degrees
        // 1 degree latitude ≈ 111,111 meters
        val latOffset = (distance * cos(angleRad)) / 111111.0

        // 1 degree longitude varies by latitude
        val lngOffset = (distance * kotlin.math.sin(angleRad)) / (111111.0 * cos(Math.toRadians(centerLat)))

        val newLat = centerLat + latOffset
        val newLng = centerLng + lngOffset

        return Pair(newLat, newLng)
    }

    /**
     * Pick a reward based on weighted probability
     */
    private fun pickWeightedReward(): GeneratedReward {
        val totalWeight = REWARD_CONFIGS.sumOf { it.weight }
        var random = Random.nextInt(totalWeight)

        for (config in REWARD_CONFIGS) {
            random -= config.weight
            if (random < 0) {
                return GeneratedReward(
                    type = config.type,
                    name = config.names.random(),
                    value = Random.nextInt(config.valueRange.first, config.valueRange.last + 1)
                )
            }
        }

        // Fallback (shouldn't happen)
        val fallback = REWARD_CONFIGS.first()
        return GeneratedReward(fallback.type, fallback.names.first(), fallback.valueRange.first)
    }

    private data class GeneratedReward(
        val type: RewardType,
        val name: String,
        val value: Int
    )

    /**
     * Mark a treasure as collected
     */
    suspend fun markTreasureCollected(treasureId: String) {
        spawnedTreasureDao.markAsCollected(treasureId)
    }


    /**
     * Convert entity to domain model
     */
    private fun SpawnedTreasureEntity.toTreasure(): Treasure {
        return Treasure(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            reward = TreasureReward(
                type = RewardType.valueOf(rewardType),
                name = rewardName,
                value = rewardValue
            ),
            isCollected = isCollected
        )
    }
}

