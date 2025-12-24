package com.compose.geoquest.ui.game

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.geoquest.BuildConfig
import com.compose.geoquest.data.model.Achievement
import com.compose.geoquest.data.model.GameState
import com.compose.geoquest.data.model.Treasure
import com.compose.geoquest.data.model.toProximityLevel
import com.compose.geoquest.data.preferences.UserPreferences
import com.compose.geoquest.data.repository.AchievementRepository
import com.compose.geoquest.data.repository.InventoryRepository
import com.compose.geoquest.data.repository.LocationRepository
import com.compose.geoquest.data.repository.TreasureSpawner
import com.compose.geoquest.service.TreasureHuntService
import com.compose.geoquest.util.HapticFeedbackManager
import com.compose.geoquest.util.ProximityNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val locationRepository: LocationRepository,
    private val treasureSpawner: TreasureSpawner,
    private val inventoryRepository: InventoryRepository,
    private val achievementRepository: AchievementRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val notificationManager: ProximityNotificationManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _isDebugMode = MutableStateFlow(BuildConfig.DEBUG_MODE)
    val isDebugMode: StateFlow<Boolean> = _isDebugMode.asStateFlow()

    private val _showTreasureDialog = MutableStateFlow(false)
    val showTreasureDialog: StateFlow<Boolean> = _showTreasureDialog.asStateFlow()

    private val _collectedTreasure = MutableStateFlow<Treasure?>(null)
    val collectedTreasure: StateFlow<Treasure?> = _collectedTreasure.asStateFlow()

    // Achievement notification
    private val _unlockedAchievement = MutableStateFlow<Achievement?>(null)
    val unlockedAchievement: StateFlow<Achievement?> = _unlockedAchievement.asStateFlow()

    // Track when treasure was selected for speed runner achievement
    private var treasureSelectedTime: Long = 0L

    // Track if initial spawn has been done
    private var hasSpawnedInitialTreasures = false

    // Track last location for distance calculation
    private var lastLocation: Location? = null

    init {
        initializeStats()
        observeTreasures()
        observePreferences()
        if (_isDebugMode.value) {
            locationRepository.setDebugMode(true)
        }
    }

    private fun initializeStats() {
        viewModelScope.launch {
            achievementRepository.initializeStats()
        }
    }

    /**
     * Observe user preferences and update managers accordingly
     */
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
    }

    /**
     * Observe treasures from the spawner
     */
    private fun observeTreasures() {
        viewModelScope.launch {
            treasureSpawner.getAvailableTreasures()
                .collect { treasures ->
                    _gameState.update { it.copy(
                        treasures = treasures,
                        isLoading = false
                    )}
                }
        }
    }

    /**
     * Spawn treasures around user's location (called on first location update)
     */
    private suspend fun spawnTreasuresIfNeeded(userLat: Double, userLng: Double) {
        if (!hasSpawnedInitialTreasures) {
            hasSpawnedInitialTreasures = true
            treasureSpawner.ensureTreasuresSpawned(userLat, userLng)
        }
    }

    /**
     * Force respawn treasures around current location
     */
    fun respawnTreasures() {
        val state = _gameState.value
        val lat = state.userLatitude ?: return
        val lng = state.userLongitude ?: return

        viewModelScope.launch {
            treasureSpawner.respawnTreasures(lat, lng)
        }
    }

    /**
     * Start listening to location updates
     * Should be called when location permission is granted
     */
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

    private fun updateUserLocation(location: Location) {
        // Calculate distance walked since last update
        lastLocation?.let { prevLocation ->
            val distanceWalked = prevLocation.distanceTo(location)
            // Only record if moved more than 1 meter (filter out GPS noise)
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
            val isInRange = distance != null && distance <= GameState.UNLOCK_RADIUS_METERS

            // Trigger haptic feedback and notifications based on proximity
            if (selectedTreasure != null && distance != null) {
                val proximityLevel = distance.toProximityLevel()

                // Haptic feedback
                hapticFeedbackManager.vibrateForProximity(proximityLevel)

                // Notification when getting close
                notificationManager.notifyProximityChange(
                    treasure = selectedTreasure,
                    proximityLevel = proximityLevel,
                    distanceMeters = distance
                )
            }

            state.copy(
                userLatitude = location.latitude,
                userLongitude = location.longitude,
                distanceToTarget = distance,
                isInRange = isInRange
            )
        }
    }

    /**
     * Select a treasure to navigate to
     */
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
            val isInRange = distance != null && distance <= GameState.UNLOCK_RADIUS_METERS

            state.copy(
                selectedTreasure = treasure,
                distanceToTarget = distance,
                isInRange = isInRange
            )
        }

        // Start background tracking service
        TreasureHuntService.startTracking(
            context = context,
            treasureId = treasure.id,
            treasureName = treasure.name,
            treasureLat = treasure.latitude,
            treasureLng = treasure.longitude
        )
    }

    /**
     * Clear the selected treasure
     */
    fun clearSelection() {
        // Stop background tracking
        TreasureHuntService.stopTracking(context)

        notificationManager.cancelProximityNotification()
        hapticFeedbackManager.cancel()
        _gameState.update { it.copy(
            selectedTreasure = null,
            distanceToTarget = null,
            isInRange = false
        )}
    }

    /**
     * Attempt to collect the treasure
     * Only succeeds if user is within range
     */
    fun collectTreasure() {
        val state = _gameState.value
        val treasure = state.selectedTreasure ?: return
        val userLat = state.userLatitude ?: return
        val userLng = state.userLongitude ?: return

        if (!state.isInRange) return

        val collectionTimeMs = System.currentTimeMillis() - treasureSelectedTime

        viewModelScope.launch {
            // Mark as collected in spawner
            treasureSpawner.markTreasureCollected(treasure.id)

            // Save to inventory
            val success = inventoryRepository.collectTreasure(treasure, userLat, userLng)
            if (success) {
                // Stop background tracking
                TreasureHuntService.stopTracking(context)

                // Success feedback
                hapticFeedbackManager.vibrateSuccess()
                notificationManager.cancelProximityNotification()

                _collectedTreasure.value = treasure
                _showTreasureDialog.value = true

                // Clear selection (treasures list updates automatically via Flow)
                _gameState.update {
                    it.copy(
                        selectedTreasure = null,
                        distanceToTarget = null,
                        isInRange = false
                    )
                }

                // Record stats for achievements
                achievementRepository.recordTreasureCollected(
                    points = treasure.reward.value,
                    collectionTimeMs = collectionTimeMs
                )

                // Check for newly unlocked achievements
                val newAchievements = achievementRepository.checkAndUnlockAchievements()
                if (newAchievements.isNotEmpty()) {
                    val achievement = newAchievements.first()
                    // Show the first unlocked achievement
                    _unlockedAchievement.value = achievement

                    // Achievement haptic and notification
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

    // ===== DEBUG MODE FUNCTIONS =====

    fun toggleDebugMode() {
        _isDebugMode.update { !it }
        locationRepository.setDebugMode(_isDebugMode.value)
    }

    /**
     * Teleport to a location (debug mode only)
     * Used for testing without physically moving
     */
    fun teleportTo(latitude: Double, longitude: Double) {
        if (_isDebugMode.value) {
            locationRepository.setDebugLocation(latitude, longitude)
        }
    }
}

