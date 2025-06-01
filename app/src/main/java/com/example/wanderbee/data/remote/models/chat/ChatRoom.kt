package com.example.wanderbee.data.remote.models.chat

data class ChatRoom(
    val id: String = "",
    val destinationName: String = "",
    val participants: List<String> = emptyList(),
    val participantJoinDates: Map<String, com.google.firebase.Timestamp> = emptyMap(),
    val createdAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)
