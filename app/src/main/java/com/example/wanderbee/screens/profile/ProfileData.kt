package com.example.wanderbee.screens.profile

data class ProfileData(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val profilePictureUrl: String = "",
    
    // Travel Preferences
    val travelStyle: String = "", // e.g., "Adventure", "Relaxation", "Cultural", "Budget"
    val favoriteDestinations: String = "", // Comma-separated list
    val travelCompanions: String = "", // e.g., "Solo", "Family", "Friends", "Partner"
    val budgetRange: String = "", // e.g., "Budget", "Mid-range", "Luxury"
    
    // Additional Travel Info
    val preferredClimate: String = "", // e.g., "Tropical", "Temperate", "Cold"
    val travelFrequency: String = "", // e.g., "Monthly", "Quarterly", "Yearly"
    val languages: String = "", // Comma-separated list
    val dietaryRestrictions: String = "", // e.g., "Vegetarian", "Halal", "None"
    
    // Timestamps
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) 