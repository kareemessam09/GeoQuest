package com.compose.geoquest.ui.game

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
import com.compose.geoquest.util.ProximityNotificationManager
import com.compose.geoquest.util.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val treasureSpawner: TreasureSpawner,
    private val inventoryRepository: InventoryRepository,
    private val achievementRepository: AchievementRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val notificationManager: ProximityNotificationManager,
    private val soundManager: SoundManager,
    private val userPreferences: UserPreferences,
    private val geofenceManager: GeofenceManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()


    private val _showTreasureDialog = MutableStateFlow(false)
    val showTreasureDialog: StateFlow<Boolean> = _showTreasureDialog.asStateFlow()

    private val _collectedTreasure = MutableStateFlow<Treasure?>(null)
    val collectedTreasure: StateFlow<Treasure?> = _collectedTreasure.asStateFlow()

    private val _unlockedAchievement = MutableStateFlow<Achievement?>(null)
    val unlockedAchievement: StateFlow<Achievement?> = _unlockedAchievement.asStateFlow()

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
                _gameState.update { state ->
                    state.copy(nearbyTreasureIds = state.nearbyTreasureIds + treasureIds)
                }
                hapticFeedbackManager.vibrateForProximity(com.compose.geoquest.data.model.ProximityLevel.WARM)
                soundManager.playTreasureNearby()
            }
            is GeofenceEvent.Exited -> {
                // User exited a treasure zone
                val treasureIds = event.treasureIds
                _gameState.update { state ->
                    state.copy(nearbyTreasureIds = state.nearbyTreasureIds - treasureIds.toSet())
                }
            }
        }
    }

    private fun initializeStats() {
        viewModelScope.launch {
            achievementRepository.initializeStats()
        }
    }


    private fun observePreferences() {
        viewModelScope.launch {
            userPreferences.hapticEnabled.collect { enabled ->
                hapticFeedbackManager.setEnabled(enabled)
            }
        }
        viewModelScope.launch {
            userPreferences.notificationEnabled.collect { enabled ->
                notificationManager.setEnabled(enabled)
            }
        }
        viewModelScope.launch {
            userPreferences.soundEnabled.collect { enabled ->
                soundManager.setEnabled(enabled)
            }
        }
    }


    private fun observeTreasures() {
        viewModelScope.launch {
            treasureSpawner.getAvailableTreasures()
                .collect { treasures ->
                    _gameState.update { it.copy(
                        treasures = treasures,
                        isLoading = false
                    )}

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
            treasureSpawner.ensureTreasuresSpawned(userLat, userLng)
        }
    }


    fun respawnTreasures() {
        val state = _gameState.value
        val lat = state.userLatitude ?: return
        val lng = state.userLongitude ?: return

        viewModelScope.launch {
            geofenceManager.removeAllGeofences()
            registeredGeofenceIds.clear()
            treasureSpawner.respawnTreasures(lat, lng)
        }
    }


    fun startLocationUpdates() {
        viewModelScope.launch {
            locationRepository.getLocationUpdates()
                .catch { e ->
                    _gameState.update { it.copy(error = e.message) }
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
                viewModelScope.launch {
                    achievementRepository.recordDistanceWalked(distanceWalked)
                }
            }
        }
        lastLocation = location

        _gameState.update { state ->
            val selectedTreasure = state.selectedTreasure
            val distance = selectedTreasure?.let {
                location.distanceTo(it.toLocation())
            }

            if (selectedTreasure != null && distance != null) {
                val proximityLevel = distance.toProximityLevel()
                hapticFeedbackManager.vibrateForProximity(proximityLevel)

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
                userLatitude = location.latitude,
                userLongitude = location.longitude,
                distanceToTarget = distance
            )
        }
    }


    fun selectTreasure(treasure: Treasure) {
        treasureSelectedTime = System.currentTimeMillis()
        _gameState.update { state ->
            val userLocation = if (state.userLatitude != null && state.userLongitude != null) {
                Location("user").apply {
                    latitude = state.userLatitude
                    longitude = state.userLongitude
                }
            } else null

            val distance = userLocation?.distanceTo(treasure.toLocation())

            state.copy(
                selectedTreasure = treasure,
                distanceToTarget = distance
            )
        }
    }


    fun clearSelection() {
        lastNotifiedTreasureId = null
        hapticFeedbackManager.cancel()
        notificationManager.cancelGeofenceNotification()
        _gameState.update { it.copy(
            selectedTreasure = null,
            distanceToTarget = null
        )}
    }


    fun collectTreasure() {
        val state = _gameState.value
        val treasure = state.selectedTreasure ?: return
        val userLat = state.userLatitude ?: return
        val userLng = state.userLongitude ?: return

        // Use geofence-based check (100m radius)
        if (!state.canCollectSelected) return

        val collectionTimeMs = System.currentTimeMillis() - treasureSelectedTime

        viewModelScope.launch {
            treasureSpawner.markTreasureCollected(treasure.id)

            val success = inventoryRepository.collectTreasure(treasure, userLat, userLng)
            if (success) {
                geofenceManager.removeGeofence(treasure.id)
                registeredGeofenceIds.remove(treasure.id)

                // Success feedback
                hapticFeedbackManager.vibrateSuccess()
                soundManager.playTreasureFound()
                notificationManager.cancelGeofenceNotification()

                _collectedTreasure.value = treasure
                _showTreasureDialog.value = true

                _gameState.update {
                    it.copy(
                        selectedTreasure = null,
                        distanceToTarget = null
                    )
                }

                // Record stats for achievements
                achievementRepository.recordTreasureCollected(
                    points = treasure.reward.value,
                    collectionTimeMs = collectionTimeMs
                )

                val newAchievements = achievementRepository.checkAndUnlockAchievements()
                if (newAchievements.isNotEmpty()) {
                    val achievement = newAchievements.first()
                    soundManager.playAchievementUnlocked()
                    _unlockedAchievement.value = achievement

                    hapticFeedbackManager.vibrateAchievement()
                    notificationManager.notifyAchievementUnlocked(
                        achievementTitle = achievement.title,
                        achievementDescription = achievement.description
                    )
                }
            }
        }
    }

    fun dismissAchievementNotification() {
        _unlockedAchievement.value = null
    }

    fun dismissTreasureDialog() {
        _showTreasureDialog.value = false
        _collectedTreasure.value = null
    }
}

