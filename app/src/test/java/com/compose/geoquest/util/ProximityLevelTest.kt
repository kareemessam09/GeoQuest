package com.compose.geoquest.util

import com.compose.geoquest.data.model.ProximityLevel
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for proximity level logic
 * Tests the hot/cold navigation system
 */
class ProximityLevelTest {

    @Test
    fun `all proximity levels are defined`() {
        assertEquals(6, ProximityLevel.entries.size)
        assertNotNull(ProximityLevel.BURNING)
        assertNotNull(ProximityLevel.HOT)
        assertNotNull(ProximityLevel.WARM)
        assertNotNull(ProximityLevel.COOL)
        assertNotNull(ProximityLevel.COLD)
        assertNotNull(ProximityLevel.FREEZING)
    }

    @Test
    fun `proximity levels have correct order from far to close`() {
        val levels = ProximityLevel.entries

        // FREEZING should be first (farthest)
        assertEquals(ProximityLevel.FREEZING, levels[0])
        // BURNING should be last (closest)
        assertEquals(ProximityLevel.BURNING, levels[5])
    }

    @Test
    fun `proximity levels ordinal increases toward closer`() {
        // Ordinal increases from far (FREEZING) to close (BURNING)
        assertTrue(ProximityLevel.FREEZING.ordinal < ProximityLevel.COLD.ordinal)
        assertTrue(ProximityLevel.COLD.ordinal < ProximityLevel.COOL.ordinal)
        assertTrue(ProximityLevel.COOL.ordinal < ProximityLevel.WARM.ordinal)
        assertTrue(ProximityLevel.WARM.ordinal < ProximityLevel.HOT.ordinal)
        assertTrue(ProximityLevel.HOT.ordinal < ProximityLevel.BURNING.ordinal)
    }

    @Test
    fun `proximity level can be compared`() {
        // Higher ordinal = closer to target
        assertTrue(ProximityLevel.BURNING.ordinal > ProximityLevel.HOT.ordinal)
        assertTrue(ProximityLevel.HOT.ordinal > ProximityLevel.FREEZING.ordinal)
    }

    @Test
    fun `BURNING is the closest level`() {
        ProximityLevel.entries.forEach { level ->
            if (level != ProximityLevel.BURNING) {
                assertTrue(ProximityLevel.BURNING.ordinal > level.ordinal)
            }
        }
    }

    @Test
    fun `FREEZING is the farthest level`() {
        ProximityLevel.entries.forEach { level ->
            if (level != ProximityLevel.FREEZING) {
                assertTrue(ProximityLevel.FREEZING.ordinal < level.ordinal)
            }
        }
    }
}

