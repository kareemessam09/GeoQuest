package com.compose.geoquest.ui.game

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.geoquest.data.model.Achievement
import com.compose.geoquest.data.model.GameState
import com.compose.geoquest.data.model.Treasure
import com.compose.geoquest.data.model.toProximityLevel
import com.compose.geoquest.data.preferences.UserPreferences
import com.compose.geoquest.data.repository.AchievementRepository
import com.compose.geoquest.data.repository.InventoryRepository
import com.compose.geoquest.data.repository.LocationRepository
import com.compose.geoquest.data.repository.TreasureSpawner
import com.compose.geoquest.receiver.GeofenceEvent
import com.compose.geoquest.receiver.GeofenceEventBus
import com.compose.geoquest.util.GeofenceManager
import com.compose.geoquest.util.HapticFeedbackManager
import com.compose.geoquest.util.ImportResult
import com.compose.geoquest.util.ProximityNotificationManager
import com.compose.geoquest.util.ShareManager
import com.compose.geoquest.util.SharedTreasure
import com.compose.geoquest.util.SoundManager
import com.compose.geoquest.widget.TreasureDistanceWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


data class GameUiState(
    val gameState: GameState = GameState(),
    val showTreasureDialog: Boolean = false,
    val collectedTreasure: Treasure? = null,
    val unlockedAchievement: Achievement? = null,
    val showShareDialog: Boolean = false,
    val showImportDialog: Boolean = false,
    val importResult: ImportResult? = null,
    val importSuccessCount: Int? = null
)

@HiltViewModel
class GameViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationRepository: LocationRepository,
    private val treasureSpawner: TreasureSpawner,
    private val inventoryRepository: InventoryRepository,
    private val achievementRepository: AchievementRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val notificationManager: ProximityNotificationManager,
    private val soundManager: SoundManager,
    private val userPreferences: UserPreferences,
    private val geofenceManager: GeofenceManager,
    private val shareManager: ShareManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    val gameState: StateFlow<GameState> = _uiState
        .map { it.gameState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameState())

    val showTreasureDialog: StateFlow<Boolean> = _uiState
        .map { it.showTreasureDialog }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val collectedTreasure: StateFlow<Treasure?> = _uiState
        .map { it.collectedTreasure }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val unlockedAchievement: StateFlow<Achievement?> = _uiState
        .map { it.unlockedAchievement }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val showShareDialog: StateFlow<Boolean> = _uiState
        .map { it.showShareDialog }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showImportDialog: StateFlow<Boolean> = _uiState
        .map { it.showImportDialog }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val importResult: StateFlow<ImportResult?> = _uiState
        .map { it.importResult }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val importSuccessCount: StateFlow<Int?> = _uiState
        .map { it.importSuccessCount }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Track for speed runner achievement
    private var treasureSelectedTime: Long = 0L

    private var hasSpawnedInitialTreasures = false
    private var registeredGeofenceIds = mutableSetOf<String>()

    private var lastLocation: Location? = null

    // Geofence event listener
    private val geofenceListener: (GeofenceEvent) -> Unit = { event ->
        handleGeofenceEvent(event)
    }

    init {
        initializeStats()
        observeTreasures()
        observePreferences()
        setupGeofenceListener()
    }

    override fun onCleared() {
        super.onCleared()
        GeofenceEventBus.removeListener(geofenceListener)
    }

    private fun setupGeofenceListener() {
        GeofenceEventBus.addListener(geofenceListener)
    }

    private fun handleGeofenceEvent(event: GeofenceEvent) {
        when (event) {
            is GeofenceEvent.Entered -> {
                // User entered a treasure zone - update UI
                val treasureIds = event.treasureIds
                _uiState.update { state ->
                    state.copy(
                        gameState = state.gameState.copy(
                            nearbyTreasureIds = state.gameState.nearbyTreasureIds + treasureIds
                        )
                    )
                }
                hapticFeedbackManager.vibrateForProximity(com.compose.geoquest.data.model.ProximityLevel.WARM)
                soundManager.playTreasureNearby()
            }
            is GeofenceEvent.Exited -> {
                // User exited a treasure zone
                val treasureIds = event.treasureIds
                _uiState.update { state ->
                    state.copy(
                        gameState = state.gameState.copy(
                            nearbyTreasureIds = state.gameState.nearbyTreasureIds - treasureIds.toSet()
                        )
                    )
                }
            }
        }
    }

    private fun initializeStats() {
        viewModelScope.launch(Dispatchers.IO) {
            achievementRepository.initializeStats()
        }
    }


    private fun observePreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.hapticEnabled.collect { enabled ->
                hapticFeedbackManager.setEnabled(enabled)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.notificationEnabled.collect { enabled ->
                notificationManager.setEnabled(enabled)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.soundEnabled.collect { enabled ->
                soundManager.setEnabled(enabled)
            }
        }
    }


    private fun observeTreasures() {
        viewModelScope.launch {
            treasureSpawner.getAvailableTreasures()
                .flowOn(Dispatchers.IO)
                .collect { treasures ->
                    _uiState.update { state ->
                        state.copy(
                            gameState = state.gameState.copy(
                                treasures = treasures,
                                isLoading = false
                            )
                        )
                    }

                    val newTreasures = treasures.filter { it.id !in registeredGeofenceIds }
                    if (newTreasures.isNotEmpty()) {
                        registerGeofencesForTreasures(newTreasures)
                    }
                }
        }
    }

    private fun registerGeofencesForTreasures(treasures: List<Treasure>) {
        geofenceManager.addGeofences(
            treasures = treasures,
            onSuccess = {
                registeredGeofenceIds.addAll(treasures.map { it.id })
            },
            onFailure = { }
        )
    }


    private suspend fun spawnTreasuresIfNeeded(userLat: Double, userLng: Double) {
        if (!hasSpawnedInitialTreasures) {
            hasSpawnedInitialTreasures = true
            withContext(Dispatchers.IO) {
                treasureSpawner.ensureTreasuresSpawned(userLat, userLng)
            }
        }
    }


    fun respawnTreasures() {
        val state = _uiState.value.gameState
        val lat = state.userLatitude ?: return
        val lng = state.userLongitude ?: return

        viewModelScope.launch(Dispatchers.IO) {
            geofenceManager.removeAllGeofences()
            registeredGeofenceIds.clear()
            treasureSpawner.respawnTreasures(lat, lng)
        }
    }


    fun startLocationUpdates() {
        viewModelScope.launch {
            locationRepository.getLocationUpdates()
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _uiState.update { state ->
                        state.copy(
                            gameState = state.gameState.copy(error = e.message)
                        )
                    }
                }
                .collect { location ->
                    // Spawn treasures around user on first location update
                    spawnTreasuresIfNeeded(location.latitude, location.longitude)
                    updateUserLocation(location)
                }
        }
    }

    private var lastNotifiedTreasureId: String? = null

    private fun updateUserLocation(location: Location) {
        // distance walked
        lastLocation?.let { prevLocation ->
            val distanceWalked = prevLocation.distanceTo(location)
            // Only record if moved more than 1 meter
            if (distanceWalked > 1f && distanceWalked < 100f) {
                viewModelScope.launch(Dispatchers.IO) {
                    achievementRepository.recordDistanceWalked(distanceWalked)
                }
            }
        }
        lastLocation = location

        _uiState.update { state ->
            val currentGameState = state.gameState
            val selectedTreasure = currentGameState.selectedTreasure
            val distance = selectedTreasure?.let {
                location.distanceTo(it.toLocation())
            }

            if (selectedTreasure != null && distance != null) {
                val proximityLevel = distance.toProximityLevel()
                hapticFeedbackManager.vibrateForProximity(proximityLevel)

                // Update widget with new distance
                TreasureDistanceWidget.updateAllWidgets(context)

                if (distance <= GameState.NOTIFICATION_RADIUS_METERS &&
                    lastNotifiedTreasureId != selectedTreasure.id) {
                    lastNotifiedTreasureId = selectedTreasure.id
                    notificationManager.notifyGeofenceEntered(
                        treasureId = selectedTreasure.id,
                        treasureName = selectedTreasure.name
                    )
                }

                if (distance <= GameState.COLLECT_RADIUS_METERS) {
                    notificationManager.notifyTreasureVeryClose(
                        treasureId = selectedTreasure.id,
                        treasureName = selectedTreasure.name
                    )
                }
            }

            state.copy(
                gameState = currentGameState.copy(
                    userLatitude = location.latitude,
                    userLongitude = location.longitude,
                    distanceToTarget = distance
                )
            )
        }
    }


    fun selectTreasure(treasure: Treasure) {
        treasureSelectedTime = System.currentTimeMillis()

        // Update widget with selected treasure
        TreasureDistanceWidget.setSelectedTreasure(context, treasure.id)

        _uiState.update { state ->
            val currentGameState = state.gameState
            val userLocation = if (currentGameState.userLatitude != null && currentGameState.userLongitude != null) {
                Location("user").apply {
                    latitude = currentGameState.userLatitude
                    longitude = currentGameState.userLongitude
                }
            } else null

            val distance = userLocation?.distanceTo(treasure.toLocation())

            state.copy(
                gameState = currentGameState.copy(
                    selectedTreasure = treasure,
                    distanceToTarget = distance
                )
            )
        }
    }


    fun clearSelection() {
        lastNotifiedTreasureId = null
        hapticFeedbackManager.cancel()
        notificationManager.cancelGeofenceNotification()

        // Clear widget selection
        TreasureDistanceWidget.setSelectedTreasure(context, null)

        _uiState.update { state ->
            state.copy(
                gameState = state.gameState.copy(
                    selectedTreasure = null,
                    distanceToTarget = null
                )
            )
        }
    }


    fun collectTreasure() {
        val currentGameState = _uiState.value.gameState
        val treasure = currentGameState.selectedTreasure ?: return
        val userLat = currentGameState.userLatitude ?: return
        val userLng = currentGameState.userLongitude ?: return

        // Use geofence-based check (100m radius)
        if (!currentGameState.canCollectSelected) return

        val collectionTimeMs = System.currentTimeMillis() - treasureSelectedTime

        viewModelScope.launch(Dispatchers.IO) {
            treasureSpawner.markTreasureCollected(treasure.id)

            val success = inventoryRepository.collectTreasure(treasure, userLat, userLng)
            if (success) {
                geofenceManager.removeGeofence(treasure.id)
                registeredGeofenceIds.remove(treasure.id)

                // Switch to Main for UI updates
                withContext(Dispatchers.Main) {
                    // Success feedback
                    hapticFeedbackManager.vibrateSuccess()
                    soundManager.playTreasureFound()
                    notificationManager.cancelGeofenceNotification()

                    // Clear widget selection since treasure is collected
                    TreasureDistanceWidget.setSelectedTreasure(context, null)

                    _uiState.update { state ->
                        state.copy(
                            gameState = state.gameState.copy(
                                selectedTreasure = null,
                                distanceToTarget = null
                            ),
                            collectedTreasure = treasure,
                            showTreasureDialog = true
                        )
                    }
                }

                // Record stats for achievements (back on IO)
                achievementRepository.recordTreasureCollected(
                    points = treasure.reward.value,
                    collectionTimeMs = collectionTimeMs
                )

                val newAchievements = achievementRepository.checkAndUnlockAchievements()
                if (newAchievements.isNotEmpty()) {
                    val achievement = newAchievements.first()
                    withContext(Dispatchers.Main) {
                        soundManager.playAchievementUnlocked()
                        hapticFeedbackManager.vibrateAchievement()
                        notificationManager.notifyAchievementUnlocked(
                            achievementTitle = achievement.title,
                            achievementDescription = achievement.description
                        )
                        _uiState.update { state ->
                            state.copy(unlockedAchievement = achievement)
                        }
                    }
                }
            }
        }
    }

    fun dismissAchievementNotification() {
        _uiState.update { it.copy(unlockedAchievement = null) }
    }

    fun dismissTreasureDialog() {
        _uiState.update { it.copy(showTreasureDialog = false, collectedTreasure = null) }
    }

    // ==================== Treasure Sharing ====================

    fun showShareTreasuresDialog() {
        _uiState.update { it.copy(showShareDialog = true) }
    }

    fun dismissShareDialog() {
        _uiState.update { it.copy(showShareDialog = false) }
    }

    fun shareTreasures(treasures: List<Treasure>, senderName: String) {
        shareManager.shareTreasureLocations(treasures, senderName)
        _uiState.update { it.copy(showShareDialog = false) }
    }

    fun showImportTreasuresDialog() {
        _uiState.update { it.copy(showImportDialog = true, importResult = null) }
    }

    fun dismissImportDialog() {
        _uiState.update { it.copy(showImportDialog = false, importResult = null) }
    }

    fun parseShareCode(code: String) {
        val result = shareManager.parseSharedTreasures(code)
        _uiState.update { it.copy(importResult = result) }
    }

    fun confirmImportTreasures(sharedTreasures: List<SharedTreasure>) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = treasureSpawner.importSharedTreasures(sharedTreasures)

            withContext(Dispatchers.Main) {
                _uiState.update { state ->
                    state.copy(
                        showImportDialog = false,
                        importResult = null,
                        importSuccessCount = count
                    )
                }
                // Play success sound
                soundManager.playTreasureFound()
                hapticFeedbackManager.vibrateSuccess()
            }
        }
    }

    fun dismissImportSuccessDialog() {
        _uiState.update { it.copy(importSuccessCount = null) }
    }
}

