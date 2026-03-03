package com.example.wanderbee.di

import android.content.Context
import androidx.room.Room
import com.example.wanderbee.data.local.AppDatabase
import com.example.wanderbee.data.local.dao.CityDescriptionDao
import com.example.wanderbee.data.local.dao.CulturalTipsDao
import com.example.wanderbee.data.local.dao.ProfileDao
import com.example.wanderbee.data.local.dao.SavedDestinationDao
import com.example.wanderbee.data.local.dao.CityDataDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "wanderbee_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideCityDescriptionDao(database: AppDatabase): CityDescriptionDao {
        return database.cityDescriptionDao()
    }

    @Provides
    fun provideCulturalTipsDao(database: AppDatabase): CulturalTipsDao {
        return database.culturalTipsDao()
    }

    @Provides
    fun provideSavedDestinationDao(database: AppDatabase): SavedDestinationDao {
        return database.savedDestinationDao()
    }

    @Provides
    fun provideProfileDao(database: AppDatabase): ProfileDao {
        return database.profileDao()
    }

    @Provides
    fun provideCityDataDao(database: AppDatabase): CityDataDao {
        return database.cityDataDao()
    }
}
