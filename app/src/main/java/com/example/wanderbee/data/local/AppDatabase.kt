package com.example.wanderbee.data.local

import android.view.View.X
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wanderbee.data.local.dao.CityDescriptionDao
import com.example.wanderbee.data.local.dao.CulturalTipsDao
import com.example.wanderbee.data.local.entity.CityDescriptionEntity
import com.example.wanderbee.data.local.entity.CulturalTipsEntity

@Database(entities = [CityDescriptionEntity::class, CulturalTipsEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDescriptionDao(): CityDescriptionDao
    abstract fun culturalTipsDao(): CulturalTipsDao
}