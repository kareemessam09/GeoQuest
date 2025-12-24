package com.compose.geoquest.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.compose.geoquest.data.model.Treasure
import com.compose.geoquest.receiver.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages geofences for treasure locations.
 * Uses Android's Geofencing API for battery-efficient location monitoring.
 */
@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geofencingClient: GeofencingClient
) {
    companion object {
        private const val TAG = "GeofenceManager"
        private const val PREFS_NAME = "geofence_treasures"

        // Geofence radius in meters - minimum reliable radius is ~100m
        const val GEOFENCE_RADIUS_METERS = 100f

        // How long the geofence should remain active (never expires)
        const val GEOFENCE_EXPIRATION_MS = Geofence.NEVER_EXPIRE

        // Loitering delay for DWELL transition (30 seconds)
        const val LOITERING_DELAY_MS = 30000

        // Responsiveness - how quickly geofence triggers (0 = best, uses more battery)
        const val RESPONSIVENESS_MS = 5000

        /**
         * Get treasure name by ID from SharedPreferences
         */
        fun getTreasureName(context: Context, treasureId: String): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(treasureId, null) ?: "Treasure"
        }
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT
        }
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    /**
     * Register geofences for a list of treasures
     */
    @SuppressLint("MissingPermission")
    fun addGeofences(treasures: List<Treasure>, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        Log.d(TAG, "addGeofences called with ${treasures.size} treasures")

        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasBackground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true

        // Must have at least fine location permission
        if (!hasFine) {
            Log.w(TAG, "Missing fine location permission - cannot register geofences")
            onFailure(SecurityException("Missing fine location permission"))
            return
        }

        // Warn if no background permission (geofences will only work in foreground)
        if (!hasBackground) {
            Log.w(TAG, "Missing background location permission - geofences will only trigger in foreground")
        }

        if (treasures.isEmpty()) {
            Log.d(TAG, "No treasures to add geofences for")
            return
        }

        // Store treasure names for later retrieval by BroadcastReceiver
        saveTreasureNames(treasures)

        Log.d(TAG, "Creating geofences for treasures:")
        treasures.forEach { treasure ->
            Log.d(TAG, "  - ${treasure.name} at (${treasure.latitude}, ${treasure.longitude})")
        }

        val geofences = treasures.map { treasure ->
            createGeofence(treasure)
        }

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL)
            .addGeofences(geofences)
            .build()

        try {
            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully added ${treasures.size} geofences")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to add geofences", e)
                    onFailure(e)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception adding geofences", e)
            onFailure(e)
        }
    }


    /**
     * Remove geofence for a specific treasure (e.g., when collected)
     */
    fun removeGeofence(treasureId: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        // Remove from SharedPreferences
        prefs.edit().remove(treasureId).apply()

        geofencingClient.removeGeofences(listOf(treasureId))
            .addOnSuccessListener {
                Log.d(TAG, "Successfully removed geofence for: $treasureId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove geofence for: $treasureId", e)
                onFailure(e)
            }
    }

    /**
     * Remove all registered geofences
     */
    fun removeAllGeofences(onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        // Clear all treasure names from SharedPreferences
        prefs.edit().clear().apply()

        geofencingClient.removeGeofences(pendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully removed all geofences")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove all geofences", e)
                onFailure(e)
            }
    }

    private fun saveTreasureNames(treasures: List<Treasure>) {
        prefs.edit().apply {
            treasures.forEach { treasure ->
                putString(treasure.id, treasure.name)
            }
            apply()
        }
    }

    private fun createGeofence(treasure: Treasure): Geofence {
        return Geofence.Builder()
            .setRequestId(treasure.id)
            .setCircularRegion(
                treasure.latitude,
                treasure.longitude,
                GEOFENCE_RADIUS_METERS
            )
            .setExpirationDuration(GEOFENCE_EXPIRATION_MS)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                Geofence.GEOFENCE_TRANSITION_EXIT or
                Geofence.GEOFENCE_TRANSITION_DWELL
            )
            .setLoiteringDelay(LOITERING_DELAY_MS)
            .setNotificationResponsiveness(RESPONSIVENESS_MS)
            .build()
    }
}

