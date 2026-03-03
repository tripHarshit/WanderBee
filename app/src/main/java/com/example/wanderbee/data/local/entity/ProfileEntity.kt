package com.example.wanderbee.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val profilePictureUrl: String = "",
    
    // Travel Preferences
    val travelStyle: String = "",
    val favoriteDestinations: String = "",
    val travelCompanions: String = "",
    val budgetRange: String = "",
    
    // Additional Travel Info
    val preferredClimate: String = "",
    val travelFrequency: String = "",
    val languages: String = "",
    val dietaryRestrictions: String = "",
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 