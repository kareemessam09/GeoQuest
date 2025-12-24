package com.compose.geoquest.ui.game

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.compose.geoquest.data.model.GameState
import com.compose.geoquest.data.model.ProximityLevel
import com.compose.geoquest.data.model.toProximityLevel
import com.compose.geoquest.receiver.GpsStatusReceiver
import com.compose.geoquest.ui.components.AchievementNotification
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onNavigateToBackpack: () -> Unit,
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val gameState by viewModel.gameState.collectAsState()
    val isDebugMode by viewModel.isDebugMode.collectAsState()
    val showTreasureDialog by viewModel.showTreasureDialog.collectAsState()
    val collectedTreasure by viewModel.collectedTreasure.collectAsState()
    val unlockedAchievement by viewModel.unlockedAchievement.collectAsState()

    // GPS status from BroadcastReceiver (real-time updates)
    val isGpsEnabled by GpsStatusReceiver.isGpsEnabled.collectAsState()
    var showGpsDialog by remember { mutableStateOf(false) }

    // Initial GPS check and register for updates
    LaunchedEffect(Unit) {
        GpsStatusReceiver.checkGpsStatus(context)
    }

    // Show dialog when GPS is disabled (reacts to broadcast)
    LaunchedEffect(isGpsEnabled) {
        if (!isGpsEnabled) {
            showGpsDialog = true
        }
    }

    // GPS Enable Dialog
    if (showGpsDialog && !isGpsEnabled) {
        AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            title = { Text("📍 GPS Required") },
            text = {
                Text("Please enable GPS/Location services to hunt for treasures. Without GPS, the app cannot detect your location.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showGpsDialog = false
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGpsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = context.packageName
            // Optimize tile loading to reduce image decoding spam
            tileDownloadThreads = 2
            tileFileSystemThreads = 4
            tileDownloadMaxQueueSize = 8
            tileFileSystemMaxQueueSize = 16
        }
        viewModel.startLocationUpdates()
    }

    // Create and remember the MapView
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(17.0)
            // Optimize rendering
            isTilesScaledToDpi = true
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
        }
    }

    // My location overlay
    val myLocationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            enableFollowLocation()
        }
    }

    // Add overlays
    LaunchedEffect(mapView) {
        mapView.overlays.add(myLocationOverlay)
    }

    // Update map when user location changes
    LaunchedEffect(gameState.userLatitude, gameState.userLongitude) {
        if (gameState.userLatitude != null && gameState.userLongitude != null) {
            val userPoint = GeoPoint(gameState.userLatitude!!, gameState.userLongitude!!)
            mapView.controller.animateTo(userPoint)
        }
    }

    // Update treasure markers
    LaunchedEffect(gameState.treasures, gameState.selectedTreasure) {
        // Remove old treasure markers (keep my location overlay)
        mapView.overlays.removeAll { it is Marker }

        // Add treasure markers
        gameState.treasures.forEach { treasure ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(treasure.latitude, treasure.longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = treasure.name
                snippet = "Tap to navigate"

                // Create custom sized icon
                val isSelected = gameState.selectedTreasure?.id == treasure.id
                val iconSize = if (isSelected) 48 else 36 // Size in dp
                val emoji = if (isSelected) "📍" else "💰"

                // Create text bitmap with custom size
                val paint = android.graphics.Paint().apply {
                    textSize = iconSize * context.resources.displayMetrics.density
                    isAntiAlias = true
                }
                val textWidth = paint.measureText(emoji).toInt().coerceAtLeast(1)
                val textHeight = (paint.descent() - paint.ascent()).toInt().coerceAtLeast(1)

                val bitmap = android.graphics.Bitmap.createBitmap(
                    textWidth,
                    textHeight,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                canvas.drawText(emoji, 0f, -paint.ascent(), paint)

                icon = android.graphics.drawable.BitmapDrawable(context.resources, bitmap)

                setOnMarkerClickListener { _, _ ->
                    viewModel.selectTreasure(treasure)
                    true
                }
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    // Handle debug mode map clicks
    DisposableEffect(isDebugMode) {
        if (isDebugMode) {
            val tapOverlay = object : org.osmdroid.views.overlay.Overlay() {
                override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
                    if (e != null && mapView != null) {
                        val projection = mapView.projection
                        val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                        viewModel.teleportTo(geoPoint.latitude, geoPoint.longitude)
                        return true
                    }
                    return false
                }
            }
            mapView.overlays.add(0, tapOverlay)
            onDispose {
                mapView.overlays.remove(tapOverlay)
            }
        } else {
            onDispose { }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            myLocationOverlay.disableMyLocation()
            mapView.onDetach()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // OSMDroid MapView
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // Top bar with debug toggle and backpack
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Debug mode toggle
            IconButton(
                onClick = { viewModel.toggleDebugMode() },
                modifier = Modifier
                    .background(
                        if (isDebugMode) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.BugReport,
                    contentDescription = "Debug Mode",
                    tint = if (isDebugMode) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurface
                )
            }

            // Right side buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Achievements button
                IconButton(
                    onClick = onNavigateToAchievements,
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = "Achievements"
                    )
                }

                // Backpack button
                IconButton(
                    onClick = onNavigateToBackpack,
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
                ) {
                    Icon(
                        Icons.Default.Backpack,
                        contentDescription = "Backpack"
                    )
                }

                // Settings button
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        }

        // My Location FAB
        FloatingActionButton(
            onClick = {
                gameState.userLatitude?.let { lat ->
                    gameState.userLongitude?.let { lng ->
                        mapView.controller.animateTo(GeoPoint(lat, lng))
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(end = 16.dp, bottom = 120.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "My Location")
        }

        // Debug mode indicator
        if (isDebugMode) {
            Text(
                text = "DEBUG MODE - Tap map to teleport",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(top = 60.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Distance indicator and unlock button
        AnimatedVisibility(
            visible = gameState.selectedTreasure != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(WindowInsets.navigationBars.asPaddingValues())
        ) {
            DistanceCard(
                gameState = gameState,
                onCollect = { viewModel.collectTreasure() },
                onDismiss = { viewModel.clearSelection() }
            )
        }

        // Treasure collected dialog
        if (showTreasureDialog && collectedTreasure != null) {
            TreasureCollectedDialog(
                treasure = collectedTreasure!!,
                onDismiss = { viewModel.dismissTreasureDialog() }
            )
        }

        // Achievement notification (shows at top)
        AchievementNotification(
            achievement = unlockedAchievement,
            onDismiss = { viewModel.dismissAchievementNotification() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(top = 70.dp)
        )
    }
}

@Composable
fun DistanceCard(
    gameState: GameState,
    onCollect: () -> Unit,
    onDismiss: () -> Unit
) {
    val distance = gameState.distanceToTarget ?: 0f
    val proximityLevel = distance.toProximityLevel()

    val backgroundColor = when (proximityLevel) {
        ProximityLevel.BURNING -> Color(0xFF4CAF50)  // Green
        ProximityLevel.HOT -> Color(0xFFF44336)       // Red
        ProximityLevel.WARM -> Color(0xFFFF9800)      // Orange
        ProximityLevel.COOL -> Color(0xFFFFEB3B)      // Yellow
        ProximityLevel.COLD -> Color(0xFF2196F3)      // Blue
        ProximityLevel.FREEZING -> Color(0xFF9E9E9E)  // Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = gameState.selectedTreasure?.name ?: "Unknown Treasure",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        distance < 1000 -> "${distance.toInt()}m"
                        else -> String.format(java.util.Locale.US, "%.1fkm", distance / 1000)
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = when (proximityLevel) {
                        ProximityLevel.BURNING -> "🔥 YOU'RE HERE!"
                        ProximityLevel.HOT -> "🔥 Very Hot!"
                        ProximityLevel.WARM -> "☀️ Warm"
                        ProximityLevel.COOL -> "❄️ Cool"
                        ProximityLevel.COLD -> "🥶 Cold"
                        ProximityLevel.FREEZING -> "❄️❄️ Freezing"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.3f)
                    )
                ) {
                    Text("Cancel", color = Color.White)
                }

                Button(
                    onClick = onCollect,
                    enabled = gameState.isInRange,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = if (gameState.isInRange) "🎁 Open Chest!" else "Get Closer",
                        color = if (gameState.isInRange) backgroundColor else Color.White
                    )
                }
            }
        }
    }
}

