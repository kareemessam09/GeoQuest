package com.compose.geoquest.di

import android.content.Context
import androidx.room.Room
import com.compose.geoquest.BuildConfig
import com.compose.geoquest.analytics.AnalyticsTracker
import com.compose.geoquest.analytics.DebugAnalyticsTracker
import com.compose.geoquest.analytics.ProductionAnalyticsTracker
import com.compose.geoquest.data.local.AchievementDao
import com.compose.geoquest.data.local.GeoQuestDatabase
import com.compose.geoquest.data.local.InventoryDao
import com.compose.geoquest.data.local.SpawnedTreasureDao
import com.compose.geoquest.data.local.UserStatsDao
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides FusedLocationProviderClient for GPS location access
     */
    @Provides
    @Singleton
    fun provideFusedLocationClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * Provides Room Database instance
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): GeoQuestDatabase {
        return Room.databaseBuilder(
            context,
            GeoQuestDatabase::class.java,
            "geoquest_database"
        )
        .fallbackToDestructiveMigration() // For development - use migrations in production
        .build()
    }

    /**
     * Provides InventoryDao from the database
     */
    @Provides
    @Singleton
    fun provideInventoryDao(database: GeoQuestDatabase): InventoryDao {
        return database.inventoryDao()
    }

    /**
     * Provides AchievementDao from the database
     */
    @Provides
    @Singleton
    fun provideAchievementDao(database: GeoQuestDatabase): AchievementDao {
        return database.achievementDao()
    }

    /**
     * Provides UserStatsDao from the database
     */
    @Provides
    @Singleton
    fun provideUserStatsDao(database: GeoQuestDatabase): UserStatsDao {
        return database.userStatsDao()
    }

    /**
     * Provides SpawnedTreasureDao from the database
     */
    @Provides
    @Singleton
    fun provideSpawnedTreasureDao(database: GeoQuestDatabase): SpawnedTreasureDao {
        return database.spawnedTreasureDao()
    }

    /**
     * Provides Analytics tracker - debug in dev, production otherwise
     */
    @Provides
    @Singleton
    fun provideAnalyticsTracker(): AnalyticsTracker {
        return if (BuildConfig.DEBUG) {
            DebugAnalyticsTracker()
        } else {
            ProductionAnalyticsTracker()
        }
    }
}

