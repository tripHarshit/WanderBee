package com.example.wanderbee.data.remote.models.chat

/**
 * Matches the backend ChatRoom MongoDB document.
 */
data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val isGroup: Boolean = false,
    val participantIds: List<String> = emptyList(),
    val lastMessage: ChatMessage? = null
)