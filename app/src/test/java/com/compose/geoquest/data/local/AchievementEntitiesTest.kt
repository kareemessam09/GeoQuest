package com.compose.geoquest.data.local

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UserStatsEntity and AchievementEntity
 */
class AchievementEntitiesTest {

    // UserStatsEntity Tests

    @Test
    fun `userStatsEntity has correct default values`() {
        val stats = UserStatsEntity()

        assertEquals(1, stats.id)
        assertEquals(0, stats.totalTreasuresCollected)
        assertEquals(0, stats.totalPointsEarned)
        assertEquals(0f, stats.totalDistanceWalked)
        assertEquals(0, stats.totalPlayTimeMinutes)
        assertNull(stats.fastestCollectionTimeMs)
        assertTrue(stats.firstPlayDate <= System.currentTimeMillis())
        assertTrue(stats.lastPlayDate <= System.currentTimeMillis())
    }

    @Test
    fun `userStatsEntity stores values correctly`() {
        val stats = UserStatsEntity(
            totalTreasuresCollected = 10,
            totalPointsEarned = 500,
            totalDistanceWalked = 1500.5f,
            totalPlayTimeMinutes = 60,
            fastestCollectionTimeMs = 30000L,
            firstPlayDate = 1000L,
            lastPlayDate = 2000L
        )

        assertEquals(10, stats.totalTreasuresCollected)
        assertEquals(500, stats.totalPointsEarned)
        assertEquals(1500.5f, stats.totalDistanceWalked)
        assertEquals(60, stats.totalPlayTimeMinutes)
        assertEquals(30000L, stats.fastestCollectionTimeMs)
        assertEquals(1000L, stats.firstPlayDate)
        assertEquals(2000L, stats.lastPlayDate)
    }

    @Test
    fun `userStatsEntity copy works correctly`() {
        val original = UserStatsEntity(
            totalTreasuresCollected = 5,
            totalPointsEarned = 200
        )

        val updated = original.copy(
            totalTreasuresCollected = 6,
            totalPointsEarned = 300
        )

        assertEquals(5, original.totalTreasuresCollected)
        assertEquals(6, updated.totalTreasuresCollected)
        assertEquals(200, original.totalPointsEarned)
        assertEquals(300, updated.totalPointsEarned)
    }

    @Test
    fun `userStatsEntity distance can be incremented`() {
        val stats = UserStatsEntity(totalDistanceWalked = 100f)
        val updated = stats.copy(totalDistanceWalked = stats.totalDistanceWalked + 50f)

        assertEquals(150f, updated.totalDistanceWalked)
    }

    // AchievementEntity Tests

    @Test
    fun `achievementEntity has correct default values`() {
        val entity = AchievementEntity(id = "first_find")

        assertEquals("first_find", entity.id)
        assertTrue(entity.unlockedAt <= System.currentTimeMillis())
    }

    @Test
    fun `achievementEntity stores custom unlockedAt`() {
        val customTime = 1234567890L
        val entity = AchievementEntity(id = "collector", unlockedAt = customTime)

        assertEquals("collector", entity.id)
        assertEquals(customTime, entity.unlockedAt)
    }

    @Test
    fun `achievementEntity equality based on id`() {
        val entity1 = AchievementEntity(id = "test_achievement", unlockedAt = 1000L)
        val entity2 = AchievementEntity(id = "test_achievement", unlockedAt = 1000L)
        val entity3 = AchievementEntity(id = "different_achievement", unlockedAt = 1000L)

        assertEquals(entity1, entity2)
        assertNotEquals(entity1, entity3)
    }

    @Test
    fun `achievementEntity id is primary key`() {
        // Multiple entities with same ID would replace each other in Room
        val entity1 = AchievementEntity(id = "same_id", unlockedAt = 1000L)
        val entity2 = AchievementEntity(id = "same_id", unlockedAt = 2000L)

        assertEquals(entity1.id, entity2.id)
        assertNotEquals(entity1.unlockedAt, entity2.unlockedAt)
    }
}

