package com.compose.geoquest.data.repository

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.compose.geoquest.data.preferences.UserPreferences
import com.compose.geoquest.util.LocationRequestFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LocationRepository @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val userPreferences: UserPreferences
) {


    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        val highAccuracyMode = userPreferences.highAccuracyMode.first()

        val locationRequest = LocationRequestFactory.createForegroundRequest(highAccuracyMode)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }


    // just gets the last known location, doesn't request a new one
    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): Location? {

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

