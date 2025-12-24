package com.compose.geoquest.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.compose.geoquest.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ProximityNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID_ACHIEVEMENT = "achievement_channel"
        const val CHANNEL_ID_GEOFENCE = "geofence_channel"
        const val NOTIFICATION_ID_ACHIEVEMENT = 1002
        const val NOTIFICATION_ID_GEOFENCE = 1003
    }

    private var isEnabled: Boolean = true

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Achievement channel
        val achievementChannel = NotificationChannel(
            CHANNEL_ID_ACHIEVEMENT,
            "Achievements",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Achievement unlock notifications"
            enableVibration(true)
        }

        val geofenceChannel = NotificationChannel(
            CHANNEL_ID_GEOFENCE,
            "Nearby Treasures",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when you enter a treasure zone (within 100m)"
            enableVibration(true)
        }

        notificationManager.createNotificationChannel(achievementChannel)
        notificationManager.createNotificationChannel(geofenceChannel)
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            cancelAll()
        }
    }

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


    fun notifyGeofenceEntered(treasureId: String, treasureName: String) {
        if (!isEnabled) return

        showNotification(
            channelId = CHANNEL_ID_GEOFENCE,
            notificationId = NOTIFICATION_ID_GEOFENCE,
            title = "📍 Treasure Nearby!",
            message = "You're within 100m of \"$treasureName\"! Open the app to hunt it down!",
            ongoing = false
        )
    }


    fun notifyTreasureVeryClose(treasureId: String, treasureName: String) {
        if (!isEnabled) return

        showNotification(
            channelId = CHANNEL_ID_GEOFENCE,
            notificationId = NOTIFICATION_ID_GEOFENCE,
            title = "🔥 Treasure Very Close!",
            message = "\"$treasureName\" is right here! Open the app NOW to collect it!",
            ongoing = true
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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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

    fun cancelGeofenceNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_GEOFENCE)
    }

    fun cancelAll() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}

