package com.compose.geoquest.data.repository

import com.compose.geoquest.data.local.AchievementDao
import com.compose.geoquest.data.local.AchievementEntity
import com.compose.geoquest.data.local.UserStatsDao
import com.compose.geoquest.data.local.UserStatsEntity
import com.compose.geoquest.data.model.Achievements
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AchievementRepository
 * Tests achievement unlocking and statistics tracking
 */
class AchievementRepositoryTest {

    private lateinit var achievementDao: AchievementDao
    private lateinit var userStatsDao: UserStatsDao
    private lateinit var achievementRepository: AchievementRepository

    @Before
    fun setup() {
        achievementDao = mockk(relaxed = true)
        userStatsDao = mockk(relaxed = true)
        achievementRepository = AchievementRepository(achievementDao, userStatsDao)

        // Default mocks for common calls
        coEvery { userStatsDao.getStatsOnce() } returns UserStatsEntity()
        coEvery { userStatsDao.insertOrUpdate(any()) } returns Unit
    }

    @Test
    fun `getAllAchievements returns achievements with unlock status`() = runTest {
        // Given
        val unlockedEntities = listOf(
            AchievementEntity(id = "first_find", unlockedAt = 1000L)
        )
        coEvery { achievementDao.getAllUnlockedAchievements() } returns flowOf(unlockedEntities)

        // When
        val achievements = achievementRepository.getAllAchievements().first()

        // Then
        val firstFind = achievements.find { it.id == "first_find" }
        assertNotNull(firstFind)
        assertTrue(firstFind!!.isUnlocked)
        assertEquals(1000L, firstFind.unlockedAt)
    }

    @Test
    fun `getAllAchievements marks non-unlocked achievements as locked`() = runTest {
        // Given - empty unlocked list
        coEvery { achievementDao.getAllUnlockedAchievements() } returns flowOf(emptyList())

        // When
        val achievements = achievementRepository.getAllAchievements().first()

        // Then
        achievements.forEach { achievement ->
            assertFalse(achievement.isUnlocked)
            assertNull(achievement.unlockedAt)
        }
    }

    @Test
    fun `initializeStats creates new stats if not exists`() = runTest {
        // Given - override default mock to return null
        coEvery { userStatsDao.getStatsOnce() } returns null

        // When
        achievementRepository.initializeStats()

        // Then
        coVerify { userStatsDao.insertOrUpdate(any()) }
    }

    @Test
    fun `initializeStats does not create stats if already exists`() = runTest {
        // Given - use default mock (returns UserStatsEntity)

        // When
        achievementRepository.initializeStats()

        // Then - insertOrUpdate should NOT be called since stats exist
        coVerify(exactly = 0) { userStatsDao.insertOrUpdate(any()) }
    }

    @Test
    fun `recordTreasureCollected updates stats`() = runTest {
        // Given - ensure all mocks are set up
        coEvery { userStatsDao.incrementTreasureCollected(any()) } returns Unit
        coEvery { userStatsDao.updateFastestCollection(any()) } returns Unit
        coEvery { userStatsDao.updateLastPlayDate(any()) } returns Unit

        // When
        achievementRepository.recordTreasureCollected(points = 100, collectionTimeMs = 5000)

        // Then - verify the stats update calls were made
        coVerify { userStatsDao.incrementTreasureCollected(100) }
        coVerify { userStatsDao.updateFastestCollection(5000) }
        coVerify { userStatsDao.updateLastPlayDate(any()) }
    }

    @Test
    fun `recordDistanceWalked updates stats`() = runTest {
        // Given
        coEvery { userStatsDao.getStatsOnce() } returns UserStatsEntity()
        coEvery { userStatsDao.addDistance(any()) } returns Unit

        // When
        achievementRepository.recordDistanceWalked(100f)

        // Then
        coVerify { userStatsDao.addDistance(100f) }
    }

    @Test
    fun `checkAndUnlockAchievements unlocks first_find when treasure collected`() = runTest {
        // Given
        val stats = UserStatsEntity(totalTreasuresCollected = 1)
        coEvery { userStatsDao.getStatsOnce() } returns stats
        coEvery { achievementDao.getAchievement(any()) } returns null
        coEvery { achievementDao.unlockAchievement(any()) } returns Unit

        // When
        val unlocked = achievementRepository.checkAndUnlockAchievements()

        // Then
        val firstFind = unlocked.find { it.id == "first_find" }
        assertNotNull(firstFind)
    }

    @Test
    fun `checkAndUnlockAchievements does not unlock already unlocked achievements`() = runTest {
        // Given
        val stats = UserStatsEntity(totalTreasuresCollected = 1)
        coEvery { userStatsDao.getStatsOnce() } returns stats
        coEvery { achievementDao.getAchievement("first_find") } returns AchievementEntity("first_find")
        coEvery { achievementDao.getAchievement(neq("first_find")) } returns null

        // When
        val unlocked = achievementRepository.checkAndUnlockAchievements()

        // Then
        val firstFind = unlocked.find { it.id == "first_find" }
        assertNull(firstFind) // Should not be in newly unlocked list
    }

    @Test
    fun `getUnlockedCount returns count from dao`() = runTest {
        // Given
        coEvery { achievementDao.getUnlockedCount() } returns flowOf(5)

        // When
        val count = achievementRepository.getUnlockedCount().first()

        // Then
        assertEquals(5, count)
    }

    @Test
    fun `getAchievementProgress returns correct percentage`() = runTest {
        // Given
        val totalAchievements = Achievements.ALL.size
        coEvery { achievementDao.getUnlockedCount() } returns flowOf(totalAchievements / 2)

        // When
        val progress = achievementRepository.getAchievementProgress().first()

        // Then
        assertEquals(0.5f, progress, 0.01f)
    }

    @Test
    fun `getUserStats returns stats from dao`() = runTest {
        // Given
        val stats = UserStatsEntity(
            totalTreasuresCollected = 10,
            totalPointsEarned = 500,
            totalDistanceWalked = 1000f
        )
        coEvery { userStatsDao.getStats() } returns flowOf(stats)

        // When
        val result = achievementRepository.getUserStats().first()

        // Then
        assertNotNull(result)
        assertEquals(10, result!!.totalTreasuresCollected)
        assertEquals(500, result.totalPointsEarned)
        assertEquals(1000f, result.totalDistanceWalked)
    }
}

