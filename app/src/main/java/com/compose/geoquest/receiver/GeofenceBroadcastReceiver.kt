package com.compose.geoquest.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.compose.geoquest.util.GeofenceManager
import com.compose.geoquest.util.ProximityNotificationManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceReceiver"
        const val ACTION_GEOFENCE_EVENT = "com.compose.geoquest.ACTION_GEOFENCE_EVENT"
    }

    @Inject
    lateinit var notificationManager: ProximityNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called with action: ${intent.action}")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null - intent action: ${intent.action}")
            return
        }

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofence error: $errorMessage (code: ${geofencingEvent.errorCode})")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.d(TAG, "Geofence transition type: $geofenceTransition")

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                handleGeofenceEnter(context, geofencingEvent)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                handleGeofenceExit(geofencingEvent)
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                handleGeofenceDwell(context, geofencingEvent)
            }
            else -> {
                Log.w(TAG, "Unknown geofence transition: $geofenceTransition")
            }
        }
    }

    private fun handleGeofenceEnter(context: Context, event: GeofencingEvent) {
        val triggeringGeofences = event.triggeringGeofences ?: return

        for (geofence in triggeringGeofences) {
            val treasureId = geofence.requestId
            val treasureName = GeofenceManager.getTreasureName(context, treasureId)
            Log.d(TAG, "Entered geofence for treasure: $treasureName ($treasureId)")

            // Send notification that user is near a treasure
            notificationManager.notifyGeofenceEntered(
                treasureId = treasureId,
                treasureName = treasureName
            )
        }

        // Broadcast to update UI if app is open
        GeofenceEventBus.postEvent(GeofenceEvent.Entered(triggeringGeofences.map { it.requestId }))
    }

    private fun handleGeofenceExit(event: GeofencingEvent) {
        val triggeringGeofences = event.triggeringGeofences ?: return

        for (geofence in triggeringGeofences) {
            val treasureId = geofence.requestId
            Log.d(TAG, "Exited geofence for treasure: $treasureId")
        }

        // Broadcast to update UI
        GeofenceEventBus.postEvent(GeofenceEvent.Exited(triggeringGeofences.map { it.requestId }))
    }

    private fun handleGeofenceDwell(context: Context, event: GeofencingEvent) {
        val triggeringGeofences = event.triggeringGeofences ?: return

        for (geofence in triggeringGeofences) {
            val treasureId = geofence.requestId
            val treasureName = GeofenceManager.getTreasureName(context, treasureId)
            Log.d(TAG, "Dwelling in geofence for treasure: $treasureName ($treasureId)")

            // User has been near the treasure for a while - stronger notification
            notificationManager.notifyTreasureVeryClose(
                treasureId = treasureId,
                treasureName = treasureName
            )
        }
    }
}


object GeofenceEventBus {
    private val listeners = mutableListOf<(GeofenceEvent) -> Unit>()

    fun addListener(listener: (GeofenceEvent) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (GeofenceEvent) -> Unit) {
        listeners.remove(listener)
    }

    fun postEvent(event: GeofenceEvent) {
        listeners.forEach { it(event) }
    }
}

sealed class GeofenceEvent {
    data class Entered(val treasureIds: List<String>) : GeofenceEvent()
    data class Exited(val treasureIds: List<String>) : GeofenceEvent()
}

