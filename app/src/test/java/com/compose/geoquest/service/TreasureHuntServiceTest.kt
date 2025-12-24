package com.compose.geoquest.service

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for TreasureHuntService companion object
 * Tests the service configuration and static methods
 */
class TreasureHuntServiceTest {

    @Test
    fun `service constants are valid`() {
        assertNotNull(TreasureHuntService.CHANNEL_ID)
        assertTrue(TreasureHuntService.CHANNEL_ID.isNotEmpty())

        assertTrue(TreasureHuntService.NOTIFICATION_ID > 0)

        assertNotNull(TreasureHuntService.ACTION_START)
        assertNotNull(TreasureHuntService.ACTION_STOP)

        assertNotEquals(TreasureHuntService.ACTION_START, TreasureHuntService.ACTION_STOP)
    }

    @Test
    fun `extra keys are unique`() {
        val extras = setOf(
            TreasureHuntService.EXTRA_TREASURE_ID,
            TreasureHuntService.EXTRA_TREASURE_NAME,
            TreasureHuntService.EXTRA_TREASURE_LAT,
            TreasureHuntService.EXTRA_TREASURE_LNG
        )

        assertEquals(4, extras.size) // All should be unique
    }

    @Test
    fun `isServiceRunning returns false by default`() {
        // Service should not be running initially
        assertFalse(TreasureHuntService.isServiceRunning())
    }

    @Test
    fun `channel ID is descriptive`() {
        assertTrue(TreasureHuntService.CHANNEL_ID.contains("treasure"))
    }

    @Test
    fun `notification ID does not conflict with other managers`() {
        // ProximityNotificationManager uses 1001 and 1002
        // TreasureHuntService uses 2001
        assertTrue(TreasureHuntService.NOTIFICATION_ID > 1002)
    }
}

