package com.compose.geoquest.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.compose.geoquest.data.model.ProximityLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages haptic feedback for proximity-based interactions
 */
@Singleton
class HapticFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private var isEnabled: Boolean = true
    private var lastProximityLevel: ProximityLevel? = null

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            vibrator.cancel()
        }
    }

    /**
     * Provides haptic feedback based on proximity level
     */
    fun vibrateForProximity(level: ProximityLevel) {
        if (!isEnabled) return
        if (level == lastProximityLevel) return
        lastProximityLevel = level

        val pattern = when (level) {
            ProximityLevel.BURNING -> longArrayOf(0, 100, 50, 100, 50, 100)
            ProximityLevel.HOT -> longArrayOf(0, 150, 100, 150)
            ProximityLevel.WARM -> longArrayOf(0, 200)
            ProximityLevel.COOL -> longArrayOf(0, 100)
            ProximityLevel.COLD -> longArrayOf(0, 50)
            ProximityLevel.FREEZING -> return
        }

        vibrate(pattern)
    }

    fun vibrateSuccess() {
        if (!isEnabled) return
        vibrate(longArrayOf(0, 100, 100, 100, 100, 300))
    }

    fun vibrateAchievement() {
        if (!isEnabled) return
        vibrate(longArrayOf(0, 50, 50, 50, 50, 50, 50, 200))
    }

    private fun vibrate(pattern: LongArray) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    fun cancel() {
        vibrator.cancel()
        lastProximityLevel = null
    }
}

