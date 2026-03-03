package com.example.wanderbee.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.remote.StompClient
import com.example.wanderbee.data.remote.models.chat.ChatMessage
import com.example.wanderbee.data.remote.models.chat.ChatPreview
import com.example.wanderbee.data.remote.models.chat.ChatRoom
import com.example.wanderbee.data.remote.models.chat.SendMessageRequest
import com.example.wanderbee.data.repository.ChatRepository
import com.example.wanderbee.utils.AppPreferences
import com.example.wanderbee.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val stompClient: StompClient,
    private val appPreferences: AppPreferences
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val _groupMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val groupMessages: StateFlow<List<ChatMessage>> = _groupMessages

    private val _privateMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val privateMessages: StateFlow<List<ChatMessage>> = _privateMessages

    private val _chatPreviews = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chatPreviews: StateFlow<List<ChatPreview>> = _chatPreviews

    /** Human-readable error set when any repository call returns [Resource.Error]. */
    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError

    /** True while history is loading from the REST endpoint. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var currentUserId: String? = null

    // Track active WebSocket subscriptions so we can clean up
    private var currentGroupSubId: String? = null
    private var currentPrivateSubId: String? = null

    init {
        viewModelScope.launch {
            currentUserId = appPreferences.userEmail.first()
            // Connect the STOMP client with the user's email
            currentUserId?.let { stompClient.connect(it) }

            // Subscribe to the private message queue for this user
            currentUserId?.let { userId ->
                currentPrivateSubId = stompClient.subscribe("/user/queue/messages") { message ->
                    Log.d(TAG, "Private message received: ${message.content}")
                    val current = _privateMessages.value.toMutableList()
                    current.add(message)
                    _privateMessages.value = current
                }
            }
        }
    }

    // ── Chat Previews ──────────────────────────────────────────────────────

    fun loadAllChats() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = chatRepository.getChatPreviews()) {
                is Resource.Success -> {
                    _chatPreviews.value = result.data
                    _chatError.value = null
                }
                is Resource.Error -> {
                    _chatError.value = result.message
                    Log.e(TAG, "loadAllChats error: ${result.message} (HTTP ${result.statusCode})")
                }
                is Resource.Loading -> Unit
            }
            _isLoading.value = false
        }
    }

    // ── Group Chat ─────────────────────────────────────────────────────────

    /**
     * Join a destination group chat.
     * Creates the room on the backend (idempotent) and adds the current user.
     */
    fun joinGroupChat(destinationId: String, destinationName: String) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch
                chatRepository.createGroupRoom(
                    name = destinationName,
                    participantIds = listOf(userId)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error joining chat: ${e.message}", e)
            }
        }
    }

    /**
     * Load message history and subscribe to real-time updates for a group room.
     */
    fun listenToGroupMessages(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // Load history via REST, unwrap Resource
            when (val result = chatRepository.getMessages(roomId)) {
                is Resource.Success -> {
                    _groupMessages.value = result.data
                    _chatError.value = null
                }
                is Resource.Error -> {
                    _chatError.value = result.message
                    Log.e(TAG, "listenToGroupMessages history error: ${result.message} (HTTP ${result.statusCode})")
                }
                is Resource.Loading -> Unit
            }
            _isLoading.value = false

            // Unsubscribe from old room if any
            currentGroupSubId?.let { stompClient.unsubscribe(it) }

            // Subscribe to real-time updates via STOMP
            currentGroupSubId = stompClient.subscribe("/topic/room/$roomId") { message ->
                Log.d(TAG, "Group message received: ${message.content}")
                _groupMessages.value = _groupMessages.value + message
            }
        }
    }

    /** Send a message to a group room. */
    fun sendGroupMessage(roomId: String, text: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            try {
                val request = SendMessageRequest(
                    roomId = roomId,
                    senderId = userId,
                    content = text
                )
                // Send via STOMP for real-time delivery
                stompClient.send("/app/chat.send", request)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending group message: ${e.message}", e)
            }
        }
    }

    // ── Private Chat ───────────────────────────────────────────────────────

    /**
     * Start or retrieve a private chat with another user.
     * Returns the room ID, or null on failure.
     */
    suspend fun startOrGetPrivateChat(otherUserId: String): String? {
        return try {
            when (val result = chatRepository.createPrivateRoom(otherUserId)) {
                is Resource.Success -> result.data.id
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating private chat: ${e.message}", e)
            null
        }
    }

    /** Load history and listen for private messages on a specific room. */
    fun listenToPrivateMessagesByRoomId(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = chatRepository.getMessages(roomId)) {
                is Resource.Success -> {
                    _privateMessages.value = result.data
                    _chatError.value = null
                }
                is Resource.Error -> {
                    _chatError.value = result.message
                    Log.e(TAG, "listenToPrivateMessagesByRoomId error: ${result.message}")
                }
                is Resource.Loading -> Unit
            }
            _isLoading.value = false
        }
    }

    /** Send a private message to a room. */
    fun sendPrivateMessage(roomId: String, recipientId: String, text: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            try {
                val request = SendMessageRequest(
                    roomId = roomId,
                    senderId = userId,
                    recipientId = recipientId,
                    content = text
                )
                stompClient.send("/app/chat.send", request)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending private message: ${e.message}", e)
            }
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    fun getCurrentUserId(): String? = currentUserId

    override fun onCleared() {
        super.onCleared()
        currentGroupSubId?.let { stompClient.unsubscribe(it) }
        // Don't disconnect the STOMP client here — it's shared and lives in the DI graph.
    }
}