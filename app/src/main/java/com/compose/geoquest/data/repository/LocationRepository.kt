package com.compose.geoquest.data.repository

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that provides location updates using FusedLocationProviderClient
 * Emits location updates as a Flow for reactive consumption
 */
@Singleton
class LocationRepository @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) {

    // For debug mode: allows setting a fake location
    private val _debugLocation = MutableStateFlow<Location?>(null)
    val debugLocation: StateFlow<Location?> = _debugLocation.asStateFlow()

    private var isDebugMode = false

    fun setDebugMode(enabled: Boolean) {
        isDebugMode = enabled
        if (!enabled) {
            _debugLocation.value = null
        }
    }

    /**
     * Set a fake location for debug/demo purposes
     * Allows "teleporting" to any location on the map
     */
    fun setDebugLocation(latitude: Double, longitude: Double) {
        if (isDebugMode) {
            _debugLocation.value = Location("debug").apply {
                this.latitude = latitude
                this.longitude = longitude
                this.accuracy = 5f
            }
        }
    }

    /**
     * Get location updates as a Flow
     * Updates every 5 seconds or when user moves 5 meters
     *
     * @return Flow of Location updates
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        // Location request configuration
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // 5 seconds interval
        ).apply {
            setMinUpdateDistanceMeters(5f) // Update if moved 5 meters
            setWaitForAccurateLocation(true)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // If in debug mode and we have a debug location, use that instead
                    val locationToEmit = if (isDebugMode && _debugLocation.value != null) {
                        _debugLocation.value!!
                    } else {
                        location
                    }
                    trySend(locationToEmit)
                }
            }
        }

        // Start location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // Also emit debug location changes when in debug mode
        val debugJob = CoroutineScope(coroutineContext).launch {
            _debugLocation.collect { debugLoc ->
                if (isDebugMode && debugLoc != null) {
                    trySend(debugLoc)
                }
            }
        }

        // Cleanup when flow collection stops
        awaitClose {
            debugJob.cancel()
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * Get the last known location (one-shot)
     * Useful for initial location before updates start
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): Location? {
        if (isDebugMode && _debugLocation.value != null) {
            return _debugLocation.value
        }

        return try {
            val task = fusedLocationClient.lastLocation
            suspendCancellableCoroutine { continuation ->
                task.addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                task.addOnFailureListener {
                    continuation.resume(null)
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}

