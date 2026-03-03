package com.example.wanderbee.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cultural_tips")
data class CulturalTipsEntity(
    @PrimaryKey val cityName: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
)