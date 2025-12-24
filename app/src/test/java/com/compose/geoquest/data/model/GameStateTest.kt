package com.compose.geoquest.data.model

import android.location.Location
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for GameState and proximity calculations
 * Demonstrates testing skills - crucial for CV!
 */
class GameStateTest {

    @Test
    fun `proximity level BURNING when distance less than 15m`() {
        val distance = 10f
        assertEquals(ProximityLevel.BURNING, distance.toProximityLevel())
    }

    @Test
    fun `proximity level HOT when distance between 15m and 50m`() {
        val distance = 30f
        assertEquals(ProximityLevel.HOT, distance.toProximityLevel())
    }

    @Test
    fun `proximity level WARM when distance between 50m and 100m`() {
        val distance = 75f
        assertEquals(ProximityLevel.WARM, distance.toProximityLevel())
    }

    @Test
    fun `proximity level COOL when distance between 100m and 200m`() {
        val distance = 150f
        assertEquals(ProximityLevel.COOL, distance.toProximityLevel())
    }

    @Test
    fun `proximity level COLD when distance between 200m and 500m`() {
        val distance = 350f
        assertEquals(ProximityLevel.COLD, distance.toProximityLevel())
    }

    @Test
    fun `proximity level FREEZING when distance greater than 500m`() {
        val distance = 1000f
        assertEquals(ProximityLevel.FREEZING, distance.toProximityLevel())
    }

    @Test
    fun `proximity level at exact boundary 15m is BURNING`() {
        val distance = 15f
        assertEquals(ProximityLevel.BURNING, distance.toProximityLevel())
    }

    @Test
    fun `proximity level at exact boundary 50m is HOT`() {
        val distance = 50f
        assertEquals(ProximityLevel.HOT, distance.toProximityLevel())
    }

    @Test
    fun `GameState default values are correct`() {
        val state = GameState()

        assertNull(state.userLatitude)
        assertNull(state.userLongitude)
        assertNull(state.selectedTreasure)
        assertNull(state.distanceToTarget)
        assertFalse(state.isInRange)
        assertTrue(state.treasures.isEmpty())
        assertTrue(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `UNLOCK_RADIUS_METERS is 15 meters`() {
        assertEquals(15f, GameState.UNLOCK_RADIUS_METERS)
    }

    @Test
    fun `VISIBILITY_RADIUS_METERS is 1000 meters`() {
        assertEquals(1000f, GameState.VISIBILITY_RADIUS_METERS)
    }
}

