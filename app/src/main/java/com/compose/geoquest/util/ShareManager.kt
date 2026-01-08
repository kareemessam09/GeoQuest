package com.compose.geoquest.util

import android.content.Context
import android.content.Intent
import android.util.Base64
import com.compose.geoquest.data.model.Achievement
import com.compose.geoquest.data.model.Treasure
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton


data class SharedTreasure(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val sharedBy: String = "A friend"
)


sealed class ImportResult {
    data class Success(val treasures: List<SharedTreasure>) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

@Singleton
class ShareManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val SHARE_PREFIX = "GEOQUEST:"
        private const val VERSION = 1
    }


    fun shareAchievement(achievement: Achievement) {
        val shareText = buildAchievementShareText(achievement)
        shareText(shareText, "Share Achievement")
    }

    private fun buildAchievementShareText(achievement: Achievement): String {
        return """
            |${achievement.iconEmoji} Achievement Unlocked in GeoQuest! ${achievement.iconEmoji}
            |
            |🏆 ${achievement.title}
            |📜 ${achievement.description}
            |
            |Can you beat my score? Download GeoQuest and start your treasure hunting adventure! 🗺️💎
            |
            |#GeoQuest #TreasureHunting #Achievement
        """.trimMargin()
    }

    fun shareTreasureFound(treasureName: String, rewardValue: Int) {
        val shareText = """
            |💎 Treasure Found in GeoQuest! 💎
            |
            |I just discovered "$treasureName" and earned $rewardValue points!
            |
            |Join the adventure! 🗺️
            |
            |#GeoQuest #TreasureHunting
        """.trimMargin()
        shareText(shareText, "Share Treasure")
    }


    fun shareTreasureLocation(treasure: Treasure, senderName: String = "A friend") {
        shareTreasureLocations(listOf(treasure), senderName)
    }


    fun shareTreasureLocations(treasures: List<Treasure>, senderName: String = "A friend") {
        val encodedData = encodeTreasures(treasures, senderName)

        val shareText = """
            |🗺️ GeoQuest Treasure Locations! 🗺️
            |
            |$senderName shared ${treasures.size} treasure location${if (treasures.size > 1) "s" else ""} with you!
            |
            |📍 Treasures:
            |${treasures.joinToString("\n") { "• ${it.name}" }}
            |
            |To import: Copy the code below and paste it in GeoQuest app!
            |
            |$encodedData
            |
            |#GeoQuest #TreasureHunting
        """.trimMargin()

        shareText(shareText, "Share Treasure Locations")
    }


    private fun encodeTreasures(treasures: List<Treasure>, senderName: String): String {
        val json = JSONObject().apply {
            put("v", VERSION)
            put("from", senderName)
            put("treasures", JSONArray().apply {
                treasures.forEach { treasure ->
                    put(JSONObject().apply {
                        put("n", treasure.name)
                        put("lat", treasure.latitude)
                        put("lng", treasure.longitude)
                    })
                }
            })
        }

        val jsonString = json.toString()
        val encoded = Base64.encodeToString(jsonString.toByteArray(), Base64.NO_WRAP)
        return "$SHARE_PREFIX$encoded"
    }


    fun parseSharedTreasures(data: String): ImportResult {
        return try {
            val cleanData = data.trim()

            // Check if it contains our share prefix
            val encodedPart = if (cleanData.contains(SHARE_PREFIX)) {
                cleanData.substringAfter(SHARE_PREFIX).substringBefore("\n").trim()
            } else if (cleanData.startsWith("GEOQUEST:")) {
                cleanData.substringAfter("GEOQUEST:").trim()
            } else {
                return ImportResult.Error("Invalid share code. Make sure you copied the entire code.")
            }

            val decoded = String(Base64.decode(encodedPart, Base64.NO_WRAP))
            val json = JSONObject(decoded)

            val version = json.optInt("v", 1)
            val senderName = json.optString("from", "A friend")
            val treasuresArray = json.getJSONArray("treasures")

            val treasures = mutableListOf<SharedTreasure>()
            for (i in 0 until treasuresArray.length()) {
                val treasureJson = treasuresArray.getJSONObject(i)
                treasures.add(
                    SharedTreasure(
                        name = treasureJson.getString("n"),
                        latitude = treasureJson.getDouble("lat"),
                        longitude = treasureJson.getDouble("lng"),
                        sharedBy = senderName
                    )
                )
            }

            if (treasures.isEmpty()) {
                ImportResult.Error("No treasures found in the shared data.")
            } else {
                ImportResult.Success(treasures)
            }
        } catch (e: Exception) {
            ImportResult.Error("Failed to parse share code: ${e.message}")
        }
    }


    fun containsShareData(text: String): Boolean {
        return text.contains(SHARE_PREFIX)
    }


    private fun shareText(text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(shareIntent)
    }
}

