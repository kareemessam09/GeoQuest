package com.compose.geoquest.di

import android.content.Context
import androidx.room.Room
import com.compose.geoquest.data.local.AchievementDao
import com.compose.geoquest.data.local.GeoQuestDatabase
import com.compose.geoquest.data.local.InventoryDao
import com.compose.geoquest.data.local.SpawnedTreasureDao
import com.compose.geoquest.data.local.UserStatsDao
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
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


    @Provides
    @Singleton
    fun provideFusedLocationClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }


    @Provides
    @Singleton
    fun provideGeofencingClient(
        @ApplicationContext context: Context
    ): GeofencingClient {
        return LocationServices.getGeofencingClient(context)
    }


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
        .fallbackToDestructiveMigration()
        .build()
    }


    @Provides
    @Singleton
    fun provideInventoryDao(database: GeoQuestDatabase): InventoryDao {
        return database.inventoryDao()
    }


    @Provides
    @Singleton
    fun provideAchievementDao(database: GeoQuestDatabase): AchievementDao {
        return database.achievementDao()
    }


    @Provides
    @Singleton
    fun provideUserStatsDao(database: GeoQuestDatabase): UserStatsDao {
        return database.userStatsDao()
    }


    @Provides
    @Singleton
    fun provideSpawnedTreasureDao(database: GeoQuestDatabase): SpawnedTreasureDao {
        return database.spawnedTreasureDao()
    }


}

