package com.compose.geoquest.data.repository

import com.compose.geoquest.data.local.AchievementDao
import com.compose.geoquest.data.local.AchievementEntity
import com.compose.geoquest.data.local.UserStatsDao
import com.compose.geoquest.data.local.UserStatsEntity
import com.compose.geoquest.data.model.Achievement
import com.compose.geoquest.data.model.AchievementRequirement
import com.compose.geoquest.data.model.Achievements
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao,
    private val userStatsDao: UserStatsDao
) {

    fun getAllAchievements(): Flow<List<Achievement>> {
        return achievementDao.getAllUnlockedAchievements().map { unlockedList ->
            val unlockedIds = unlockedList.associate { it.id to it.unlockedAt }

            Achievements.ALL.map { achievement ->
                achievement.copy(
                    isUnlocked = unlockedIds.containsKey(achievement.id),
                    unlockedAt = unlockedIds[achievement.id]
                )
            }
        }
    }

    fun getUserStats(): Flow<UserStatsEntity?> = userStatsDao.getStats()

    suspend fun checkAndUnlockAchievements(): List<Achievement> {
        val stats = userStatsDao.getStatsOnce() ?: return emptyList()
        val newlyUnlocked = mutableListOf<Achievement>()

        for (achievement in Achievements.ALL) {
            if (achievementDao.getAchievement(achievement.id) != null) continue

            val shouldUnlock = when (val req = achievement.requirement) {
                is AchievementRequirement.FirstTreasure ->
                    stats.totalTreasuresCollected >= 1

                is AchievementRequirement.TreasuresCollected ->
                    stats.totalTreasuresCollected >= req.count

                is AchievementRequirement.TotalPointsEarned ->
                    stats.totalPointsEarned >= req.points

                is AchievementRequirement.DistanceWalked ->
                    stats.totalDistanceWalked >= req.meters

                is AchievementRequirement.DaysPlayed -> {
                    val daysSinceStart = (System.currentTimeMillis() - stats.firstPlayDate) / (24 * 60 * 60 * 1000)
                    daysSinceStart >= req.days
                }

                is AchievementRequirement.SpeedRunner -> {
                    stats.fastestCollectionTimeMs != null && stats.fastestCollectionTimeMs < 60_000
                }
            }

            if (shouldUnlock) {
                achievementDao.unlockAchievement(AchievementEntity(achievement.id))
                newlyUnlocked.add(achievement.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis()))
            }
        }

        return newlyUnlocked
    }


    suspend fun initializeStats() {
        if (userStatsDao.getStatsOnce() == null) {
            userStatsDao.insertOrUpdate(UserStatsEntity())
        }
    }


    suspend fun recordTreasureCollected(points: Int, collectionTimeMs: Long) {
        initializeStats()
        userStatsDao.incrementTreasureCollected(points)
        userStatsDao.updateFastestCollection(collectionTimeMs)
        userStatsDao.updateLastPlayDate()
    }


    suspend fun recordDistanceWalked(meters: Float) {
        initializeStats()
        userStatsDao.addDistance(meters)
    }

    fun getUnlockedCount(): Flow<Int> = achievementDao.getUnlockedCount()


    fun getAchievementProgress(): Flow<Float> {
        return achievementDao.getUnlockedCount().map { unlocked ->
            unlocked.toFloat() / Achievements.ALL.size
        }
    }
}

