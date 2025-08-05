package com.example.wanderbee.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_data")
data class CityDataEntity(
    @PrimaryKey
    val cityKey: String, // "cityName_countryName"
    val cityName: String,
    val countryName: String,
    val currency: String,
    val timezone: String,
    val language: String,
    val tags: String, // JSON array as string
    val description: String,
    val highlights: String,
    val createdAt: Long = System.currentTimeMillis()
) 