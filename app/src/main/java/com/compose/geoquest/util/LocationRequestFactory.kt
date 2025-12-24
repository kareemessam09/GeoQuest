package com.compose.geoquest.util

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority


object LocationRequestFactory {

    fun createForegroundRequest(highAccuracyMode: Boolean): LocationRequest {
        val priority = if (highAccuracyMode) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val updateInterval = if (highAccuracyMode) 5000L else 10000L

        return LocationRequest.Builder(priority, updateInterval)
            .setMinUpdateDistanceMeters(5f)
            .setWaitForAccurateLocation(highAccuracyMode)
            .build()
    }
}

