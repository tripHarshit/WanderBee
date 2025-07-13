package com.example.wanderbee.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_destinations")
data class SavedDestinationEntity(
    @PrimaryKey val destinationId: String, // Format: "city_dest"
    val city: String,
    val destination: String,
    val userId: String,
    val savedAt: Long = System.currentTimeMillis(),
    val isSaved: Boolean = true
) 