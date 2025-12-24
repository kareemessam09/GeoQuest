package com.compose.geoquest.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * BroadcastReceiver that listens for GPS/Location provider changes
 * Works even when app is in background
 */
class GpsStatusReceiver : BroadcastReceiver() {

    companion object {
        private val _isGpsEnabled = MutableStateFlow(true)
        val isGpsEnabled: StateFlow<Boolean> = _isGpsEnabled.asStateFlow()

        /**
         * Check current GPS status
         */
        fun checkGpsStatus(context: Context): Boolean {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            _isGpsEnabled.value = enabled
            return enabled
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            _isGpsEnabled.value = isGpsOn
        }
    }
}

