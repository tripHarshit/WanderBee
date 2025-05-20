package com.example.wanderbee.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_descriptions")
data class CityDescriptionEntity(
    @PrimaryKey val cityName: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
