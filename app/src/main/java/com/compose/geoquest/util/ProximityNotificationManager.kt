package com.compose.geoquest.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.compose.geoquest.MainActivity
import com.compose.geoquest.data.model.ProximityLevel
import com.compose.geoquest.data.model.Treasure
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages notifications for treasure proximity and achievements
 */
@Singleton
class ProximityNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID_PROXIMITY = "proximity_channel"
        const val CHANNEL_ID_ACHIEVEMENT = "achievement_channel"
        const val NOTIFICATION_ID_PROXIMITY = 1001
        const val NOTIFICATION_ID_ACHIEVEMENT = 1002
    }

    private var lastNotifiedLevel: ProximityLevel? = null
    private var isEnabled: Boolean = true

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Proximity channel
        val proximityChannel = NotificationChannel(
            CHANNEL_ID_PROXIMITY,
            "Treasure Proximity",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when you're near a treasure"
            enableVibration(true)
        }

        // Achievement channel
        val achievementChannel = NotificationChannel(
            CHANNEL_ID_ACHIEVEMENT,
            "Achievements",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Achievement unlock notifications"
            enableVibration(true)
        }

        notificationManager.createNotificationChannel(proximityChannel)
        notificationManager.createNotificationChannel(achievementChannel)
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            cancelAll()
        }
    }

    /**
     * Show notification when proximity level changes significantly
     */
    fun notifyProximityChange(
        treasure: Treasure,
        proximityLevel: ProximityLevel,
        distanceMeters: Float
    ) {
        if (!isEnabled) return

        // Only notify on significant proximity changes
        if (proximityLevel == lastNotifiedLevel) return

        // Only show notifications for HOT and BURNING levels
        if (proximityLevel != ProximityLevel.HOT && proximityLevel != ProximityLevel.BURNING) {
            // Cancel existing notification if moving away
            if (lastNotifiedLevel == ProximityLevel.HOT || lastNotifiedLevel == ProximityLevel.BURNING) {
                cancelProximityNotification()
            }
            lastNotifiedLevel = proximityLevel
            return
        }

        lastNotifiedLevel = proximityLevel

        val (title, message) = when (proximityLevel) {
            ProximityLevel.BURNING -> Pair(
                "🔥 You're HERE!",
                "\"${treasure.name}\" is within reach! Open the app to collect it!"
            )
            ProximityLevel.HOT -> Pair(
                "🔥 Getting Hot!",
                "\"${treasure.name}\" is only ${distanceMeters.toInt()}m away!"
            )
            else -> return
        }

        showNotification(
            channelId = CHANNEL_ID_PROXIMITY,
            notificationId = NOTIFICATION_ID_PROXIMITY,
            title = title,
            message = message,
            ongoing = proximityLevel == ProximityLevel.BURNING
        )
    }

    /**
     * Show achievement unlocked notification
     */
    fun notifyAchievementUnlocked(achievementTitle: String, achievementDescription: String) {
        if (!isEnabled) return

        showNotification(
            channelId = CHANNEL_ID_ACHIEVEMENT,
            notificationId = NOTIFICATION_ID_ACHIEVEMENT,
            title = "🏆 Achievement Unlocked!",
            message = "$achievementTitle - $achievementDescription",
            ongoing = false
        )
    }

    private fun showNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        ongoing: Boolean = false
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(!ongoing)
            .setOngoing(ongoing)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission not granted - ignore
        }
    }

    fun cancelProximityNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_PROXIMITY)
        lastNotifiedLevel = null
    }

    fun cancelAll() {
        NotificationManagerCompat.from(context).cancelAll()
        lastNotifiedLevel = null
    }
}

