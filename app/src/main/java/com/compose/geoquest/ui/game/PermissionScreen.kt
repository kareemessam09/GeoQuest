package com.compose.geoquest.ui.game

import android.Manifest
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.compose.geoquest.util.PermissionUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

private enum class PermissionPhase {
    LOCATION,
    BACKGROUND,
    COMPLETED
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current

    var permissionPhase by remember { mutableStateOf(PermissionPhase.LOCATION) }

    val permissions = PermissionUtils.getRequiredPermissions()
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    val backgroundLocationPermission = if (PermissionUtils.requiresBackgroundLocationPermission) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else null

    val hasFineLocation = permissionState.permissions
        .find { it.permission == Manifest.permission.ACCESS_FINE_LOCATION }
        ?.status?.isGranted == true

    val hasBackgroundLocation = if (PermissionUtils.requiresBackgroundLocationPermission) {
        backgroundLocationPermission?.status?.isGranted == true
    } else true

    LaunchedEffect(hasFineLocation, hasBackgroundLocation, permissionPhase) {
        when (permissionPhase) {
            PermissionPhase.LOCATION -> {
                if (hasFineLocation) {
                    if (PermissionUtils.requiresBackgroundLocationPermission && !hasBackgroundLocation) {
                        permissionPhase = PermissionPhase.BACKGROUND
                    } else {
                        onPermissionGranted()
                    }
                }
            }
            PermissionPhase.BACKGROUND -> {
                if (hasBackgroundLocation) {
                    onPermissionGranted()
                }
            }
            PermissionPhase.COMPLETED -> {
                onPermissionGranted()
            }
        }
    }

    when (permissionPhase) {
        PermissionPhase.BACKGROUND -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Background location permission required. To notify you when near a treasure even when the app is closed, we need Allow all the time location permission."
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🔔",
                    style = MaterialTheme.typography.displayLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Background Location Required",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "To notify you when you're near a treasure even when the app is closed, " +
                           "we need \"Allow all the time\" location permission.\n\n" +
                           "Steps:\n" +
                           "1. Tap \"Open App Settings\"\n" +
                           "2. Tap \"Permissions\"\n" +
                           "3. Tap \"Location\"\n" +
                           "4. Select \"Allow all the time\"",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open App Settings")
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { permissionPhase = PermissionPhase.COMPLETED }
                ) {
                    Text("Skip (No background notifications)")
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Location permission required. GeoQuest needs your location to show nearby treasures and detect when you find them."
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🗺️",
                    style = MaterialTheme.typography.displayLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Location Permission Required",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (permissionState.shouldShowRationale) {
                        "GeoQuest needs precise location access to detect when you're near treasures. " +
                        "Please grant location permission to play."
                    } else {
                        "GeoQuest is a GPS-based treasure hunting game. " +
                        "We need your location to show nearby treasures and detect when you find them."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (permissionState.shouldShowRationale) {
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Open Settings")
                    }
                } else {
                    Button(
                        onClick = { permissionState.launchMultiplePermissionRequest() }
                    ) {
                        Text("Grant Location Permission")
                    }
                }
            }
        }
    }
}
