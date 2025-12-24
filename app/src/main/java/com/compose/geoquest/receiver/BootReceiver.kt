package com.compose.geoquest.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.compose.geoquest.data.repository.TreasureSpawner
import com.compose.geoquest.util.GeofenceManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootReceiverEntryPoint {
        fun geofenceManager(): GeofenceManager
        fun treasureSpawner(): TreasureSpawner
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.d(TAG, "Device booted - re-registering geofences")

            scope.launch {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        BootReceiverEntryPoint::class.java
                    )

                    val treasureSpawner = entryPoint.treasureSpawner()
                    val geofenceManager = entryPoint.geofenceManager()

                    val treasures = treasureSpawner.getAvailableTreasures().first()
                    if (treasures.isNotEmpty()) {
                        Log.d(TAG, "Re-registering ${treasures.size} geofences after boot")
                        geofenceManager.addGeofences(
                            treasures = treasures,
                            onSuccess = {
                                Log.d(TAG, "Successfully re-registered geofences after boot")
                            },
                            onFailure = { e ->
                                Log.e(TAG, "Failed to re-register geofences after boot: ${e.message}")
                            }
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error re-registering geofences after boot", e)
                }
            }
        }
    }
}

