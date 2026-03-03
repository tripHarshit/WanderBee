package com.example.wanderbee.data.remote.models.itinerary

data class ItineraryResponse(
    val days: List<ItineraryDay>,
    val accommodation: Accommodation,
    val localDishes: List<String>
)

data class ItineraryDay(
    val dayNumber: Int,
    val date: String,
    val totalCost: String,
    val timeSlots: List<TimeSlot>
)

data class TimeSlot(
    val time: String,
    val activity: String,
    val location: String,
    val cost: String,
    val transportation: String,
    val dining: String
)

data class Accommodation(
    val budget: String,
    val recommendation: String
)

