package com.example.wanderbee.data.remote.models.AI

// models/AiRequest.kt
data class AiMessage(
    val role: String,
    val content: String
)

data class AiRequest(
    val model: String,
    val messages: List<AiMessage>,
    val temperature: Double
)

data class ItineraryItem(
    val time: String,
    val title: String,
    val subtitle: String? = null
)