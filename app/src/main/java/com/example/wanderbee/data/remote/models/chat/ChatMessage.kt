package com.example.wanderbee.data.remote.models.chat

/**
 * Matches the backend ChatMessage MongoDB document.
 * senderId / recipientId are the user's email (same value the gateway
 * extracts from the JWT and forwards as X-User-Id).
 */
data class ChatMessage(
    val id: String? = null,
    val content: String = "",
    val senderId: String = "",
    val recipientId: String? = null,
    val roomId: String = "",
    val timestamp: String? = null,   // ISO-8601 LocalDateTime from the backend
    val type: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT, IMAGE, SYSTEM, DELETE
}