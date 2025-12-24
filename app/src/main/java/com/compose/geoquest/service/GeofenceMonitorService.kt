package com.compose.geoquest.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.compose.geoquest.MainActivity
import com.compose.geoquest.data.repository.TreasureSpawner
import com.compose.geoquest.util.GeofenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class GeofenceMonitorService : Service() {

    companion object {
        private const val TAG = "GeofenceMonitorService"
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "geofence_monitor_channel"

        fun start(context: Context) {
            val intent = Intent(context, GeofenceMonitorService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, GeofenceMonitorService::class.java)
            context.stopService(intent)
        }
    }

    @Inject
    lateinit var geofenceManager: GeofenceManager

    @Inject
    lateinit var treasureSpawner: TreasureSpawner

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Register geofences when service starts
        registerGeofences()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        serviceScope.cancel()
    }

    private fun registerGeofences() {
        serviceScope.launch {
            try {
                val treasures = treasureSpawner.getAvailableTreasures().first()
                if (treasures.isNotEmpty()) {
                    Log.d(TAG, "Registering geofences for ${treasures.size} treasures")
                    geofenceManager.addGeofences(
                        treasures = treasures,
                        onSuccess = {
                            Log.d(TAG, "Successfully registered ${treasures.size} geofences from service")
                        },
                        onFailure = { e ->
                            Log.e(TAG, "Failed to register geofences from service: ${e.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error registering geofences", e)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Treasure Hunting Active",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when GeoQuest is monitoring for nearby treasures"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🗺️ Treasure Hunting Active")
            .setContentText("You'll be notified when near a treasure")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}

