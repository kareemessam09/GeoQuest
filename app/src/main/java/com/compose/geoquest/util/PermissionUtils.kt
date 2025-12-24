package com.compose.geoquest.util

import android.Manifest
import android.os.Build


object PermissionUtils {

    fun getRequiredPermissions(): List<String> = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }


    val backgroundLocationPermission: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        } else null


    val requiresBackgroundLocationPermission: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}

