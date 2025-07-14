package com.example.wanderbee.data.local

import android.view.View.X
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wanderbee.data.local.dao.CityDescriptionDao
import com.example.wanderbee.data.local.dao.CulturalTipsDao
import com.example.wanderbee.data.local.dao.ProfileDao
import com.example.wanderbee.data.local.dao.SavedDestinationDao
import com.example.wanderbee.data.local.entity.CityDescriptionEntity
import com.example.wanderbee.data.local.entity.CulturalTipsEntity
import com.example.wanderbee.data.local.entity.ProfileEntity
import com.example.wanderbee.data.local.entity.SavedDestinationEntity

@Database(
    entities = [
        CityDescriptionEntity::class, 
        CulturalTipsEntity::class, 
        SavedDestinationEntity::class,
        ProfileEntity::class
    ], 
    version = 3, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDescriptionDao(): CityDescriptionDao
    abstract fun culturalTipsDao(): CulturalTipsDao
    abstract fun savedDestinationDao(): SavedDestinationDao
    abstract fun profileDao(): ProfileDao
}