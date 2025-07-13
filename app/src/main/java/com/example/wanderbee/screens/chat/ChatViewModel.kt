package com.example.wanderbee.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.remote.models.chat.ChatMessage
import com.example.wanderbee.data.remote.models.chat.ChatPreview
import com.example.wanderbee.data.repository.DefaultChatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val defaultChatRepository: DefaultChatRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _groupMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val groupMessages: StateFlow<List<ChatMessage>> = _groupMessages

    private val _privateMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val privateMessages: StateFlow<List<ChatMessage>> = _privateMessages

    private val _chatPreviews = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chatPreviews: StateFlow<List<ChatPreview>> = _chatPreviews

    fun loadAllChats() {
        val currentUserId = auth.currentUser?.uid ?: return
        Log.d("ChatViewModel", "Current User ID: $currentUserId")

        viewModelScope.launch {
            val groupChats = defaultChatRepository.getGroupChatPreviews(currentUserId)
            Log.d("ChatViewModel", "Group Chats: $groupChats")
            val privateChats = defaultChatRepository.getPrivateChatPreviews(currentUserId)

            _chatPreviews.value = (groupChats + privateChats)
                .sortedByDescending { it.lastMessageTime }
        }
    }


    // Join group chat and remove expired participants
    fun joinGroupChat(destinationId: String, destinationName: String) {
        viewModelScope.launch {
            try {
                defaultChatRepository.joinDestinationChat(destinationId, destinationName)
                delay(1000)
                defaultChatRepository.removeExpiredParticipants(destinationId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error joining chat: ${e.message}", e)
            }
        }
    }


    // Send message to group
    fun sendGroupMessage(destinationId: String, text: String) {
        val user = auth.currentUser ?: return
        val currentUserId = user.uid

        viewModelScope.launch {
            try {
                val currentUserName = defaultChatRepository.getUserDetails(currentUserId).name
                val message = com.example.wanderbee.data.remote.models.chat.ChatMessage(
                    text = text,
                    senderId = currentUserId,
                    senderName = currentUserName
                )

                defaultChatRepository.sendGroupMessage(destinationId, message)

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error in sendGroupMessage: ${e.message}", e)
            }
        }
    }


    // Listen to group messages
    fun listenToGroupMessages(destinationId: String) {
        defaultChatRepository.listenToGroupMessages(destinationId) {
            _groupMessages.value = it
        }
    }

    // Start or get private chat and send message
    fun sendPrivateMessage(otherUserId: String, text: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val chatId = defaultChatRepository.getOrCreatePrivateChat(user.uid, otherUserId)
            val message = ChatMessage(
                text = text,
                senderId = user.uid,
                senderName = defaultChatRepository.getUserDetails(otherUserId).name
            )
            defaultChatRepository.sendPrivateMessage(chatId, message)
        }
    }

    // Listen to private messages
    fun listenToPrivateMessages(otherUserId: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val chatId = defaultChatRepository.getOrCreatePrivateChat(user.uid, otherUserId)
            defaultChatRepository.listenToPrivateMessages(chatId) {
                _privateMessages.value = it
            }
        }
    }

    // Listen to private messages by chatId (for PrivateChatScreen)
    fun listenToPrivateMessagesByChatId(chatId: String) {
        defaultChatRepository.listenToPrivateMessages(chatId) {
            _privateMessages.value = it
        }
    }

    // Send private message by chatId (for PrivateChatScreen)
    fun sendPrivateMessageByChatId(chatId: String, text: String) {
        val user = auth.currentUser ?: return
        val currentUserId = user.uid

        viewModelScope.launch {
            try {
                val currentUserName = defaultChatRepository.getUserDetails(currentUserId).name
               val message = com.example.wanderbee.data.remote.models.chat.ChatMessage(
                    text = text,
                    senderId = currentUserId,
                    senderName = currentUserName
                )

                defaultChatRepository.sendPrivateMessage(chatId, message)

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error in sendPrivateMessageByChatId: ${e.message}", e)
            }
        }
    }


    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Add this suspend function for starting or getting a private chat
    suspend fun startOrGetPrivateChat(otherUserId: String): String? {
        val user = auth.currentUser ?: return null
        return defaultChatRepository.getOrCreatePrivateChat(user.uid, otherUserId)
    }
}
