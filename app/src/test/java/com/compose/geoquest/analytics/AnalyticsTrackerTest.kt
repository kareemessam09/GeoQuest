package com.compose.geoquest.analytics

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Analytics system
 */
class AnalyticsTrackerTest {

    @Test
    fun `all analytics events have unique names`() {
        val events = listOf(
            AnalyticsEvent.MapScreenViewed,
            AnalyticsEvent.BackpackScreenViewed,
            AnalyticsEvent.AchievementsScreenViewed,
            AnalyticsEvent.SettingsScreenViewed
        )

        val names = events.map { it.name }
        val uniqueNames = names.toSet()

        assertEquals(names.size, uniqueNames.size)
    }

    @Test
    fun `treasureCollected event contains required data`() {
        val event = AnalyticsEvent.TreasureCollected(
            treasureId = "t1",
            treasureName = "Gold Chest",
            pointsEarned = 100,
            distanceWalked = 500f,
            timeToCollectMs = 60000L
        )

        assertEquals("treasure_collected", event.name)
        assertEquals("t1", event.treasureId)
        assertEquals("Gold Chest", event.treasureName)
        assertEquals(100, event.pointsEarned)
        assertEquals(500f, event.distanceWalked)
        assertEquals(60000L, event.timeToCollectMs)
    }

    @Test
    fun `treasureSelected event contains treasure info`() {
        val event = AnalyticsEvent.TreasureSelected(
            treasureId = "t1",
            treasureName = "Ruby Cave"
        )

        assertEquals("treasure_selected", event.name)
        assertEquals("t1", event.treasureId)
        assertEquals("Ruby Cave", event.treasureName)
    }

    @Test
    fun `achievementUnlocked event contains achievement info`() {
        val event = AnalyticsEvent.AchievementUnlocked(
            achievementId = "first_find",
            achievementName = "First Find"
        )

        assertEquals("achievement_unlocked", event.name)
        assertEquals("first_find", event.achievementId)
        assertEquals("First Find", event.achievementName)
    }

    @Test
    fun `debugModeToggled event contains enabled state`() {
        val enabledEvent = AnalyticsEvent.DebugModeToggled(enabled = true)
        val disabledEvent = AnalyticsEvent.DebugModeToggled(enabled = false)

        assertTrue(enabledEvent.enabled)
        assertFalse(disabledEvent.enabled)
    }

    @Test
    fun `teleportUsed event contains coordinates`() {
        val event = AnalyticsEvent.TeleportUsed(
            latitude = 30.65,
            longitude = 32.05
        )

        assertEquals("teleport_used", event.name)
        assertEquals(30.65, event.latitude, 0.001)
        assertEquals(32.05, event.longitude, 0.001)
    }

    @Test
    fun `permissionDenied event contains permission name`() {
        val event = AnalyticsEvent.PermissionDenied(permission = "ACCESS_FINE_LOCATION")

        assertEquals("permission_denied", event.name)
        assertEquals("ACCESS_FINE_LOCATION", event.permission)
    }

    @Test
    fun `locationError event contains error message`() {
        val event = AnalyticsEvent.LocationError(message = "GPS not available")

        assertEquals("location_error", event.name)
        assertEquals("GPS not available", event.message)
    }

    @Test
    fun `debugAnalyticsTracker does not throw`() {
        val tracker = DebugAnalyticsTracker()

        // Should not throw
        tracker.trackEvent(AnalyticsEvent.MapScreenViewed)
        tracker.setUserId("user123")
        tracker.setUserProperty("key", "value")
    }

    @Test
    fun `productionAnalyticsTracker does not throw`() {
        val tracker = ProductionAnalyticsTracker()

        // Should not throw even without Firebase
        tracker.trackEvent(AnalyticsEvent.MapScreenViewed)
        tracker.setUserId("user123")
        tracker.setUserProperty("key", "value")
    }
}

