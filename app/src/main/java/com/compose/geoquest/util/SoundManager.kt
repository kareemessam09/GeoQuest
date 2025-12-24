package com.compose.geoquest.util

import android.content.Context
import android.media.RingtoneManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SoundManager"
    }

    private var isEnabled: Boolean = true

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun playTreasureFound() {
        if (!isEnabled) return
        playSystemSound()
    }

    fun playTreasureNearby() {
        if (!isEnabled) return
        playSystemSound()
    }

    fun playAchievementUnlocked() {
        if (!isEnabled) return
        playSystemSound()
    }

    private fun playSystemSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone?.play()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound", e)
        }
    }
}
