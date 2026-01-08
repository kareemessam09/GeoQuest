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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.compose.geoquest.data.model.GameState
import com.compose.geoquest.data.model.ProximityLevel
import com.compose.geoquest.data.model.toProximityLevel
import com.compose.geoquest.receiver.GpsStatusReceiver
import com.compose.geoquest.service.GeofenceMonitorService
import com.compose.geoquest.ui.components.AchievementNotification
import com.compose.geoquest.ui.components.ImportSuccessDialog
import com.compose.geoquest.ui.components.ImportTreasuresDialog
import com.compose.geoquest.ui.components.ShareTreasuresDialog
import com.compose.geoquest.ui.theme.CommonGray
import com.compose.geoquest.ui.theme.InfoBlue
import com.compose.geoquest.ui.theme.ProximityCool
import com.compose.geoquest.ui.theme.ProximityHot
import com.compose.geoquest.ui.theme.ProximityWarm
import com.compose.geoquest.ui.theme.SuccessGreen
import com.compose.geoquest.util.ShareManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable

@Composable
fun MapScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onNavigateToBackpack: () -> Unit,
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val gameState by viewModel.gameState.collectAsState()
    val showTreasureDialog by viewModel.showTreasureDialog.collectAsState()
    val collectedTreasure by viewModel.collectedTreasure.collectAsState()
    val unlockedAchievement by viewModel.unlockedAchievement.collectAsState()

    // Treasure sharing state
    val showShareDialog by viewModel.showShareDialog.collectAsState()
    val showImportDialog by viewModel.showImportDialog.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val importSuccessCount by viewModel.importSuccessCount.collectAsState()

    // ShareManager for sharing achievements
    val shareManager = remember { ShareManager(context) }

    val isGpsEnabled by GpsStatusReceiver.isGpsEnabled.collectAsState()
    var showGpsDialog by remember { mutableStateOf(false) }

    var hasInitiallyCentered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        GpsStatusReceiver.checkGpsStatus(context)
    }

    LaunchedEffect(isGpsEnabled) {
        if (!isGpsEnabled) {
            showGpsDialog = true
        }
    }

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

        // Start background geofence service
        GeofenceMonitorService.start(context)
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(17.0)
            isTilesScaledToDpi = true
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
        }
    }

    val myLocationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
        }
    }

    LaunchedEffect(mapView) {
        mapView.overlays.add(myLocationOverlay)
    }

    LaunchedEffect(gameState.userLatitude, gameState.userLongitude) {
        if (gameState.userLatitude != null && gameState.userLongitude != null) {
            if (!hasInitiallyCentered) {
                val userPoint = GeoPoint(gameState.userLatitude!!, gameState.userLongitude!!)
                mapView.controller.animateTo(userPoint)
                hasInitiallyCentered = true
            }
        }
    }

    LaunchedEffect(gameState.treasures, gameState.selectedTreasure) {
        mapView.overlays.removeAll { it is Marker }

        gameState.treasures.forEach { treasure ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(treasure.latitude, treasure.longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = treasure.name
                snippet = "Tap to navigate"

                val isSelected = gameState.selectedTreasure?.id == treasure.id
                val iconSize = if (isSelected) 48 else 36
                val emoji = if (isSelected) "📍" else "💰"

                val paint = android.graphics.Paint().apply {
                    textSize = iconSize * context.resources.displayMetrics.density
                    isAntiAlias = true
                }
                val textWidth = paint.measureText(emoji).toInt().coerceAtLeast(1)
                val textHeight = (paint.descent() - paint.ascent()).toInt().coerceAtLeast(1)

                val bitmap = createBitmap(textWidth, textHeight)
                val canvas = android.graphics.Canvas(bitmap)
                canvas.drawText(emoji, 0f, -paint.ascent(), paint)

                icon = bitmap.toDrawable(context.resources)

                setOnMarkerClickListener { _, _ ->
                    viewModel.selectTreasure(treasure)
                    true
                }
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.End
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Share treasures button
                IconButton(
                    onClick = { viewModel.showShareTreasuresDialog() },
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share Treasures"
                    )
                }

                // Import treasures button
                IconButton(
                    onClick = { viewModel.showImportTreasuresDialog() },
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Import Treasures"
                    )
                }

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

        // Respawn Treasures FAB
        FloatingActionButton(
            onClick = { viewModel.respawnTreasures() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(end = 16.dp, bottom = 190.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Respawn Treasures",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
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
                .padding(top = 70.dp),
            onShare = { achievement -> shareManager.shareAchievement(achievement) }
        )

        // Share treasures dialog
        if (showShareDialog) {
            ShareTreasuresDialog(
                treasures = gameState.treasures.filter { !it.isCollected },
                onShare = { treasures, senderName ->
                    viewModel.shareTreasures(treasures, senderName)
                },
                onDismiss = { viewModel.dismissShareDialog() }
            )
        }

        // Import treasures dialog
        if (showImportDialog) {
            ImportTreasuresDialog(
                onImport = { code -> viewModel.parseShareCode(code) },
                onDismiss = { viewModel.dismissImportDialog() },
                importResult = importResult,
                onConfirmImport = { sharedTreasures ->
                    viewModel.confirmImportTreasures(sharedTreasures)
                }
            )
        }

        // Import success dialog
        importSuccessCount?.let { count ->
            ImportSuccessDialog(
                count = count,
                onDismiss = { viewModel.dismissImportSuccessDialog() }
            )
        }
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
        ProximityLevel.BURNING -> SuccessGreen        // Green - Can collect!
        ProximityLevel.HOT -> ProximityHot            // Red
        ProximityLevel.WARM -> ProximityWarm          // Orange
        ProximityLevel.COOL -> ProximityCool          // Yellow
        ProximityLevel.COLD -> InfoBlue               // Blue
        ProximityLevel.FREEZING -> CommonGray         // Gray
    }

    // Text color that contrasts with background
    val textOnBackground = when (proximityLevel) {
        ProximityLevel.COOL -> Color.Black  // Yellow needs dark text
        else -> Color.White
    }

    // Accessibility: Build descriptive state for screen readers
    val distanceText = when {
        distance < 1000 -> "${distance.toInt()} meters"
        else -> String.format(java.util.Locale.US, "%.1f kilometers", distance / 1000)
    }
    val proximityDescription = when (proximityLevel) {
        ProximityLevel.BURNING -> "You're here! Ready to collect"
        ProximityLevel.HOT -> "Very hot, very close to treasure"
        ProximityLevel.WARM -> "Warm, getting closer"
        ProximityLevel.COOL -> "Cool, moderate distance"
        ProximityLevel.COLD -> "Cold, far from treasure"
        ProximityLevel.FREEZING -> "Freezing, very far from treasure"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Treasure navigation card. ${gameState.selectedTreasure?.name ?: "Unknown Treasure"}. Distance: $distanceText. Status: $proximityDescription"
            },
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
                color = textOnBackground,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics {
                    stateDescription = "Distance: $distanceText, $proximityDescription"
                }
            ) {
                Text(
                    text = when {
                        distance < 1000 -> "${distance.toInt()}m"
                        else -> String.format(java.util.Locale.US, "%.1fkm", distance / 1000)
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = textOnBackground
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
                    color = textOnBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.semantics {
                        contentDescription = "Cancel treasure navigation"
                        role = Role.Button
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                ) {
                    Text("Cancel", color = textOnBackground)
                }

                val collectButtonDescription = when {
                    gameState.canCollectSelected -> "Open treasure chest button, ready to collect"
                    gameState.isNearby -> "Getting close, need to be within 15 meters to collect"
                    else -> "Get closer button, need to be within 15 meters to collect"
                }

                Button(
                    onClick = onCollect,
                    enabled = gameState.canCollectSelected,
                    modifier = Modifier.semantics {
                        contentDescription = collectButtonDescription
                        role = Role.Button
                        if (!gameState.canCollectSelected) {
                            stateDescription = "Disabled, move closer to the treasure"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                ) {
                    val buttonText = when {
                        gameState.canCollectSelected -> "🎁 Open Chest!"
                        gameState.isNearby -> "Getting close... (< 15m to collect)"
                        else -> "Get Closer (< 15m)"
                    }
                    Text(
                        text = buttonText,
                        color = if (gameState.canCollectSelected) backgroundColor else textOnBackground
                    )
                }
            }
        }
    }
}

