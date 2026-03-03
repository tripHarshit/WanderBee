package com.example.wanderbee.data.remote.models.chat

data class ChatPreview(
    val chatId: String,
    val isGroup: Boolean,
    val name: String,               // Group name or other user's name
    val lastMessage: String,
    val lastMessageTime: Long,     // Timestamp for sorting
    val destination: String? = null // Only for group chats
)

