package com.compose.geoquest.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.widget.RemoteViews
import com.compose.geoquest.MainActivity
import com.compose.geoquest.R
import com.compose.geoquest.data.local.GeoQuestDatabase
import com.compose.geoquest.data.local.SpawnedTreasureEntity
import com.compose.geoquest.data.model.ProximityLevel
import com.compose.geoquest.data.model.toProximityLevel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume


class TreasureDistanceWidget : AppWidgetProvider() {

    companion object {
        private const val PREFS_NAME = "treasure_widget_prefs"
        private const val KEY_SELECTED_TREASURE_ID = "selected_treasure_id"
        private const val KEY_UNITS = "distance_units"


        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, TreasureDistanceWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = widgetManager.getAppWidgetIds(
                ComponentName(context, TreasureDistanceWidget::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            context.sendBroadcast(intent)
        }


        fun setSelectedTreasure(context: Context, treasureId: String?) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SELECTED_TREASURE_ID, treasureId)
                .apply()
            updateAllWidgets(context)
        }


        fun setUnits(context: Context, units: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_UNITS, units)
                .apply()
            updateAllWidgets(context)
        }

        fun getSelectedTreasureId(context: Context): String? {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_SELECTED_TREASURE_ID, null)
        }

        private fun getUnits(context: Context): String {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_UNITS, "metric") ?: "metric"
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is removed
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        scope.launch {
            val views = RemoteViews(context.packageName, R.layout.widget_treasure_distance)

            // Set up click to open app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            val selectedTreasureId = getSelectedTreasureId(context)
            val units = getUnits(context)

            if (selectedTreasureId == null) {
                views.setTextViewText(R.id.widget_treasure_name, "No treasure selected")
                views.setTextViewText(R.id.widget_distance, "--")
                views.setTextViewText(R.id.widget_proximity, "Tap to open GeoQuest")
            } else {
                // Get treasure from database
                val treasure = getTreasureFromDatabase(context, selectedTreasureId)

                if (treasure == null || treasure.isCollected) {
                    views.setTextViewText(R.id.widget_treasure_name, "Treasure unavailable")
                    views.setTextViewText(R.id.widget_distance, "--")
                    views.setTextViewText(R.id.widget_proximity, "Select a new treasure")
                } else {
                    views.setTextViewText(R.id.widget_treasure_name, treasure.name)

                    // Get current location and calculate distance
                    val location = getCurrentLocation(context)

                    if (location != null) {
                        val treasureLocation = Location("treasure").apply {
                            latitude = treasure.latitude
                            longitude = treasure.longitude
                        }
                        val distance = location.distanceTo(treasureLocation)

                        val formattedDistance = formatDistance(distance, units)
                        val proximityLevel = distance.toProximityLevel()

                        views.setTextViewText(R.id.widget_distance, formattedDistance)
                        views.setTextViewText(R.id.widget_proximity, getProximityText(proximityLevel))
                        views.setInt(R.id.widget_distance, "setTextColor", getProximityColor(proximityLevel))
                    } else {
                        views.setTextViewText(R.id.widget_distance, "--")
                        views.setTextViewText(R.id.widget_proximity, "Location unavailable")
                    }
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(context: Context): Location? {
        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            suspendCancellableCoroutine { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        continuation.resume(location)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getTreasureFromDatabase(context: Context, treasureId: String): SpawnedTreasureEntity? {
        return try {
            val database = androidx.room.Room.databaseBuilder(
                context.applicationContext,
                GeoQuestDatabase::class.java,
                "geoquest_database"
            ).build()

            database.spawnedTreasureDao().getTreasureById(treasureId)
        } catch (_: Exception) {
            null
        }
    }

    private fun formatDistance(distanceMeters: Float, units: String): String {
        return if (units == "imperial") {
            val feet = distanceMeters * 3.28084f
            if (feet < 5280) {
                "${feet.toInt()} ft"
            } else {
                val miles = feet / 5280
                String.format(Locale.getDefault(), "%.2f mi", miles)
            }
        } else {
            if (distanceMeters < 1000) {
                "${distanceMeters.toInt()} m"
            } else {
                val km = distanceMeters / 1000
                String.format(Locale.getDefault(), "%.2f km", km)
            }
        }
    }

    private fun getProximityText(level: ProximityLevel): String {
        return when (level) {
            ProximityLevel.FREEZING -> "❄️ Very far away"
            ProximityLevel.COLD -> "🌨️ Getting closer"
            ProximityLevel.COOL -> "🌤️ You're on track"
            ProximityLevel.WARM -> "🔥 Getting warm!"
            ProximityLevel.HOT -> "🔥🔥 Very hot!"
            ProximityLevel.BURNING -> "🎯 You're there!"
        }
    }

    private fun getProximityColor(level: ProximityLevel): Int {
        return when (level) {
            ProximityLevel.FREEZING -> 0xFF87CEEB.toInt()
            ProximityLevel.COLD -> 0xFF4FC3F7.toInt()
            ProximityLevel.COOL -> 0xFFFFF176.toInt()
            ProximityLevel.WARM -> 0xFFFFB74D.toInt()
            ProximityLevel.HOT -> 0xFFFF7043.toInt()
            ProximityLevel.BURNING -> 0xFFFF5252.toInt()
        }
    }
}

