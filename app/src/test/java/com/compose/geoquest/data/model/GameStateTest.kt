package com.compose.geoquest.data.model

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
        assertTrue(state.nearbyTreasureIds.isEmpty())
        assertFalse(state.canCollectSelected)
        assertFalse(state.isNearby)
        assertTrue(state.treasures.isEmpty())
        assertTrue(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `COLLECT_RADIUS_METERS is 15 meters`() {
        assertEquals(15f, GameState.COLLECT_RADIUS_METERS)
    }

    @Test
    fun `NOTIFICATION_RADIUS_METERS is 100 meters`() {
        assertEquals(100f, GameState.NOTIFICATION_RADIUS_METERS)
    }

    @Test
    fun `VISIBILITY_RADIUS_METERS is 1000 meters`() {
        assertEquals(1000f, GameState.VISIBILITY_RADIUS_METERS)
    }

    @Test
    fun `canCollectSelected returns true when distance is within 15m`() {
        val treasure = Treasure(
            id = "t1",
            name = "Test Treasure",
            latitude = 30.0,
            longitude = 31.0,
            reward = TreasureReward(RewardType.GOLD, "Gold", 100)
        )

        // Too far (50m) - cannot collect
        val stateTooFar = GameState(
            selectedTreasure = treasure,
            distanceToTarget = 50f
        )
        assertFalse(stateTooFar.canCollectSelected)

        // Within range (10m) - can collect
        val stateInRange = GameState(
            selectedTreasure = treasure,
            distanceToTarget = 10f
        )
        assertTrue(stateInRange.canCollectSelected)

        // At boundary (15m) - can collect
        val stateAtBoundary = GameState(
            selectedTreasure = treasure,
            distanceToTarget = 15f
        )
        assertTrue(stateAtBoundary.canCollectSelected)

        // No treasure selected - cannot collect
        val stateNoTreasure = GameState(
            distanceToTarget = 10f
        )
        assertFalse(stateNoTreasure.canCollectSelected)

        // No distance - cannot collect
        val stateNoDistance = GameState(
            selectedTreasure = treasure,
            distanceToTarget = null
        )
        assertFalse(stateNoDistance.canCollectSelected)
    }

    @Test
    fun `isNearby returns true when distance is between 15m and 100m`() {
        // Within notification range but not collection range
        val stateNearby = GameState(distanceToTarget = 50f)
        assertTrue(stateNearby.isNearby)

        // At notification boundary
        val stateAtBoundary = GameState(distanceToTarget = 100f)
        assertTrue(stateAtBoundary.isNearby)

        // Within collection range - not "nearby", can collect
        val stateCanCollect = GameState(distanceToTarget = 15f)
        assertFalse(stateCanCollect.isNearby)

        // Too far
        val stateTooFar = GameState(distanceToTarget = 150f)
        assertFalse(stateTooFar.isNearby)

        // No distance
        val stateNoDistance = GameState(distanceToTarget = null)
        assertFalse(stateNoDistance.isNearby)
    }
}

