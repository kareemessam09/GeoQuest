package com.compose.geoquest.ui.game

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Screen that handles location and notification permission requests
 * Shows rationale and handles different permission states
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    var showBackgroundPermission by remember { mutableStateOf(false) }

    // Build permission list - location required, notification optional but recommended
    val permissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    // Background location permission (must be requested separately on Android 10+)
    val backgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else null

    // Check if fine location is granted (we need precise location)
    val hasFineLocation = permissionState.permissions
        .find { it.permission == Manifest.permission.ACCESS_FINE_LOCATION }
        ?.status?.isGranted == true

    val hasBackgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        backgroundLocationPermission?.status?.isGranted == true
    } else true

    LaunchedEffect(hasFineLocation, hasBackgroundLocation) {
        if (hasFineLocation) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundLocation) {
                showBackgroundPermission = true
            } else {
                onPermissionGranted()
            }
        }
    }

    // Background permission screen
    if (showBackgroundPermission && !hasBackgroundLocation) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔔",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Background Location",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "To notify you when you're near a treasure even when the app is closed, " +
                       "we need \"Allow all the time\" location permission.\n\n" +
                       "Tap the button below, then select \"Allow all the time\".",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // Open app settings for background location
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            ) {
                Text("Open Settings")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onPermissionGranted() }
            ) {
                Text("Skip (No background tracking)")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🗺️",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (permissionState.shouldShowRationale) {
                "GeoQuest needs precise location access to detect when you're near treasures. " +
                "Notifications let us alert you when you're close to a treasure!"
            } else {
                "To hunt for treasures, we need:\n\n" +
                "Location - To detect nearby treasures\n" +
                "Notifications - To alert you when close"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { permissionState.launchMultiplePermissionRequest() }
        ) {
            Text("Grant Permissions")
        }

        // Show warning if only coarse location was granted
        val hasCoarseOnly = permissionState.permissions
            .find { it.permission == Manifest.permission.ACCESS_COARSE_LOCATION }
            ?.status?.isGranted == true && !hasFineLocation

        if (hasCoarseOnly) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "⚠️ You granted approximate location only. " +
                       "Precise location is needed for the 15-meter treasure detection. " +
                       "Please enable precise location in Settings.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

