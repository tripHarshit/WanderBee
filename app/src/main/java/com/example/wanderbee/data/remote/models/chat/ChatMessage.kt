package com.example.wanderbee.data.remote.models.chat

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val timestamp: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)