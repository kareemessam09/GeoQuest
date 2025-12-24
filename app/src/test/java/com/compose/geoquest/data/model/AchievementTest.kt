package com.compose.geoquest.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Achievement system
 */
class AchievementTest {

    @Test
    fun `all predefined achievements have unique IDs`() {
        val ids = Achievements.ALL.map { it.id }
        val uniqueIds = ids.toSet()

        assertEquals(
            "All achievement IDs should be unique",
            ids.size,
            uniqueIds.size
        )
    }

    @Test
    fun `achievement default isUnlocked is false`() {
        Achievements.ALL.forEach { achievement ->
            assertFalse(
                "Achievement ${achievement.id} should be locked by default",
                achievement.isUnlocked
            )
        }
    }

    @Test
    fun `achievement default unlockedAt is null`() {
        Achievements.ALL.forEach { achievement ->
            assertNull(
                "Achievement ${achievement.id} unlockedAt should be null",
                achievement.unlockedAt
            )
        }
    }

    @Test
    fun `first_find achievement has correct requirement`() {
        val firstFind = Achievements.ALL.find { it.id == "first_find" }

        assertNotNull(firstFind)
        assertTrue(firstFind!!.requirement is AchievementRequirement.FirstTreasure)
    }

    @Test
    fun `collector_5 achievement requires 5 treasures`() {
        val collector5 = Achievements.ALL.find { it.id == "collector_5" }

        assertNotNull(collector5)
        val requirement = collector5!!.requirement as AchievementRequirement.TreasuresCollected
        assertEquals(5, requirement.count)
    }

    @Test
    fun `speed_runner achievement has correct requirement type`() {
        val speedRunner = Achievements.ALL.find { it.id == "speed_runner" }

        assertNotNull(speedRunner)
        assertTrue(speedRunner!!.requirement is AchievementRequirement.SpeedRunner)
    }

    @Test
    fun `all achievements have non-empty titles and descriptions`() {
        Achievements.ALL.forEach { achievement ->
            assertTrue(
                "Achievement ${achievement.id} title should not be empty",
                achievement.title.isNotEmpty()
            )
            assertTrue(
                "Achievement ${achievement.id} description should not be empty",
                achievement.description.isNotEmpty()
            )
        }
    }

    @Test
    fun `all achievements have emoji icons`() {
        Achievements.ALL.forEach { achievement ->
            assertTrue(
                "Achievement ${achievement.id} should have an emoji icon",
                achievement.iconEmoji.isNotEmpty()
            )
        }
    }
}

