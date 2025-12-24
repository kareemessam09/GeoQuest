package com.compose.geoquest.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.compose.geoquest.MainActivity
import com.compose.geoquest.data.model.ProximityLevel
import com.compose.geoquest.data.model.RewardType
import com.compose.geoquest.data.model.Treasure
import com.compose.geoquest.data.model.TreasureReward
import com.compose.geoquest.data.model.toProximityLevel
import com.compose.geoquest.util.HapticFeedbackManager
import com.compose.geoquest.util.ProximityNotificationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground Service that tracks location in background
 * and sends notifications when user is near a treasure
 */
@AndroidEntryPoint
class TreasureHuntService : Service() {

    companion object {
        const val CHANNEL_ID = "treasure_hunt_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "START_TRACKING"
        const val ACTION_STOP = "STOP_TRACKING"
        const val EXTRA_TREASURE_ID = "treasure_id"
        const val EXTRA_TREASURE_NAME = "treasure_name"
        const val EXTRA_TREASURE_LAT = "treasure_lat"
        const val EXTRA_TREASURE_LNG = "treasure_lng"

        private var isRunning = false

        fun isServiceRunning() = isRunning

        fun startTracking(
            context: Context,
            treasureId: String,
            treasureName: String,
            treasureLat: Double,
            treasureLng: Double
        ) {
            val intent = Intent(context, TreasureHuntService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TREASURE_ID, treasureId)
                putExtra(EXTRA_TREASURE_NAME, treasureName)
                putExtra(EXTRA_TREASURE_LAT, treasureLat)
                putExtra(EXTRA_TREASURE_LNG, treasureLng)
            }
            context.startForegroundService(intent)
        }

        fun stopTracking(context: Context) {
            val intent = Intent(context, TreasureHuntService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    @Inject
    lateinit var notificationManager: ProximityNotificationManager

    @Inject
    lateinit var hapticFeedbackManager: HapticFeedbackManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var targetTreasureId: String? = null
    private var targetTreasureName: String? = null
    private var targetLat: Double = 0.0
    private var targetLng: Double = 0.0
    private var lastProximityLevel: ProximityLevel? = null

    // Cached treasure object
    private var cachedTreasure: Treasure? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                targetTreasureId = intent.getStringExtra(EXTRA_TREASURE_ID)
                targetTreasureName = intent.getStringExtra(EXTRA_TREASURE_NAME)
                targetLat = intent.getDoubleExtra(EXTRA_TREASURE_LAT, 0.0)
                targetLng = intent.getDoubleExtra(EXTRA_TREASURE_LNG, 0.0)

                // Cache treasure object to avoid recreating on each location update
                cachedTreasure = Treasure(
                    id = targetTreasureId ?: "",
                    name = targetTreasureName ?: "Treasure",
                    latitude = targetLat,
                    longitude = targetLng,
                    reward = TreasureReward(type = RewardType.GOLD, name = "", value = 0)
                )

                startForeground(NOTIFICATION_ID, createNotification("Hunting for $targetTreasureName..."))
                startLocationTracking()
                isRunning = true
            }
            ACTION_STOP -> {
                stopLocationTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                isRunning = false
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
        isRunning = false
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Treasure Hunt Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when actively hunting for a treasure"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createNotification(content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, TreasureHuntService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🗺️ GeoQuest Active")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()
    }

    private fun updateNotification(content: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification(content))
    }

    @Suppress("MissingPermission")
    private fun startLocationTracking() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 seconds in background (less battery drain)
        ).apply {
            setMinUpdateDistanceMeters(10f) // Update every 10 meters
            setWaitForAccurateLocation(true)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    processLocation(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationTracking() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun processLocation(location: Location) {
        val targetLocation = Location("target").apply {
            latitude = targetLat
            longitude = targetLng
        }
        val distance = location.distanceTo(targetLocation)
        val proximityLevel = distance.toProximityLevel()

        // Update notification with distance
        updateNotification("$targetTreasureName: ${distance.toInt()}m away")

        // Only notify on proximity level change for HOT or BURNING
        if (proximityLevel != lastProximityLevel) {
            lastProximityLevel = proximityLevel

            if (proximityLevel == ProximityLevel.HOT || proximityLevel == ProximityLevel.BURNING) {
                cachedTreasure?.let { treasure ->
                    notificationManager.notifyProximityChange(
                        treasure = treasure,
                        proximityLevel = proximityLevel,
                        distanceMeters = distance
                    )
                    hapticFeedbackManager.vibrateForProximity(proximityLevel)
                }
            }
        }
    }
}

