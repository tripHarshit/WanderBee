package com.example.wanderbee.data.remote.models.chat

/** Request body for POST /api/v1/chat/messages/send and STOMP /app/chat.send */
data class SendMessageRequest(
    val roomId: String,
    val senderId: String,
    val recipientId: String? = null,
    val content: String,
    val type: MessageType = MessageType.TEXT
)

/** Request body for POST /api/v1/chat/messages/delete and STOMP /app/chat.delete */
data class DeleteMessageRequest(
    val messageId: String,
    val roomId: String,
    val requesterId: String
)

/** Request body for POST /api/v1/chat/rooms/group */
data class CreateRoomRequest(
    val name: String,
    val isGroup: Boolean = true,
    val participantIds: List<String>
)
