package com.compose.geoquest.data.model

/**
 * Achievement system - gamification feature
 * Demonstrates domain modeling and game design patterns
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val requirement: AchievementRequirement,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

sealed class AchievementRequirement {
    data class TreasuresCollected(val count: Int) : AchievementRequirement()
    data class TotalPointsEarned(val points: Int) : AchievementRequirement()
    data class DistanceWalked(val meters: Int) : AchievementRequirement()
    data class DaysPlayed(val days: Int) : AchievementRequirement()
    data object FirstTreasure : AchievementRequirement()
    data object SpeedRunner : AchievementRequirement() // Collect treasure within 1 min of selecting
}

/**
 * Predefined achievements for the game
 */
object Achievements {
    val ALL = listOf(
        Achievement(
            id = "first_find",
            title = "First Discovery",
            description = "Find your first treasure",
            iconEmoji = "🎯",
            requirement = AchievementRequirement.FirstTreasure
        ),
        Achievement(
            id = "collector_5",
            title = "Treasure Hunter",
            description = "Collect 5 treasures",
            iconEmoji = "🏆",
            requirement = AchievementRequirement.TreasuresCollected(5)
        ),
        Achievement(
            id = "collector_10",
            title = "Master Explorer",
            description = "Collect 10 treasures",
            iconEmoji = "👑",
            requirement = AchievementRequirement.TreasuresCollected(10)
        ),
        Achievement(
            id = "points_1000",
            title = "Getting Rich",
            description = "Earn 1,000 total points",
            iconEmoji = "💰",
            requirement = AchievementRequirement.TotalPointsEarned(1000)
        ),
        Achievement(
            id = "points_5000",
            title = "Wealthy Adventurer",
            description = "Earn 5,000 total points",
            iconEmoji = "💎",
            requirement = AchievementRequirement.TotalPointsEarned(5000)
        ),
        Achievement(
            id = "speed_runner",
            title = "Speed Runner",
            description = "Collect a treasure within 1 minute of selecting it",
            iconEmoji = "⚡",
            requirement = AchievementRequirement.SpeedRunner
        ),
        Achievement(
            id = "walker_1km",
            title = "Sunday Stroll",
            description = "Walk 1 kilometer total",
            iconEmoji = "🚶",
            requirement = AchievementRequirement.DistanceWalked(1000)
        ),
        Achievement(
            id = "walker_5km",
            title = "Marathon Hunter",
            description = "Walk 5 kilometers total",
            iconEmoji = "🏃",
            requirement = AchievementRequirement.DistanceWalked(5000)
        )
    )
}

