package com.compose.geoquest.analytics

/**
 * Analytics abstraction layer
 * Demonstrates clean architecture - easy to swap implementations
 * (Firebase, Amplitude, Mixpanel, etc.)
 */
interface AnalyticsTracker {
    fun trackEvent(event: AnalyticsEvent)
    fun setUserProperty(key: String, value: String)
    fun setUserId(userId: String?)
}

/**
 * Sealed class for type-safe analytics events
 * Shows Kotlin sealed class pattern for exhaustive event handling
 */
sealed class AnalyticsEvent(val name: String) {

    // Game Events
    data class TreasureSelected(val treasureId: String, val treasureName: String) :
        AnalyticsEvent("treasure_selected")

    data class TreasureCollected(
        val treasureId: String,
        val treasureName: String,
        val pointsEarned: Int,
        val distanceWalked: Float,
        val timeToCollectMs: Long
    ) : AnalyticsEvent("treasure_collected")

    data class AchievementUnlocked(val achievementId: String, val achievementName: String) :
        AnalyticsEvent("achievement_unlocked")

    // Navigation Events
    data object MapScreenViewed : AnalyticsEvent("map_screen_viewed")
    data object BackpackScreenViewed : AnalyticsEvent("backpack_screen_viewed")
    data object AchievementsScreenViewed : AnalyticsEvent("achievements_screen_viewed")
    data object SettingsScreenViewed : AnalyticsEvent("settings_screen_viewed")

    // Feature Events
    data class DebugModeToggled(val enabled: Boolean) : AnalyticsEvent("debug_mode_toggled")
    data class TeleportUsed(val latitude: Double, val longitude: Double) : AnalyticsEvent("teleport_used")

    // Error Events
    data class LocationError(val message: String) : AnalyticsEvent("location_error")
    data class PermissionDenied(val permission: String) : AnalyticsEvent("permission_denied")
}

/**
 * Debug implementation - logs to console
 * Use in development builds
 */
class DebugAnalyticsTracker : AnalyticsTracker {

    override fun trackEvent(event: AnalyticsEvent) {
        val params = when (event) {
            is AnalyticsEvent.TreasureSelected ->
                "treasureId=${event.treasureId}, name=${event.treasureName}"
            is AnalyticsEvent.TreasureCollected ->
                "treasureId=${event.treasureId}, points=${event.pointsEarned}, time=${event.timeToCollectMs}ms"
            is AnalyticsEvent.AchievementUnlocked ->
                "achievementId=${event.achievementId}, name=${event.achievementName}"
            is AnalyticsEvent.DebugModeToggled ->
                "enabled=${event.enabled}"
            is AnalyticsEvent.TeleportUsed ->
                "lat=${event.latitude}, lng=${event.longitude}"
            is AnalyticsEvent.LocationError ->
                "message=${event.message}"
            is AnalyticsEvent.PermissionDenied ->
                "permission=${event.permission}"
            else -> ""
        }
        println("📊 Analytics: ${event.name} | $params")
    }

    override fun setUserProperty(key: String, value: String) {
        println("📊 Analytics Property: $key = $value")
    }

    override fun setUserId(userId: String?) {
        println("📊 Analytics UserId: $userId")
    }
}

/**
 * Production implementation - would integrate with Firebase/Amplitude/etc.
 * Placeholder for now - demonstrates the pattern
 */
class ProductionAnalyticsTracker : AnalyticsTracker {

    override fun trackEvent(event: AnalyticsEvent) {
        // TODO: Integrate with Firebase Analytics
        // Firebase.analytics.logEvent(event.name) { ... }
    }

    override fun setUserProperty(key: String, value: String) {
        // TODO: Firebase.analytics.setUserProperty(key, value)
    }

    override fun setUserId(userId: String?) {
        // TODO: Firebase.analytics.setUserId(userId)
    }
}

