package com.example.wanderbee.data.repository

import android.util.Log
import com.example.wanderbee.data.remote.StompClient
import com.example.wanderbee.data.remote.apiService.ChatApiService
import com.example.wanderbee.data.remote.models.chat.*
import com.example.wanderbee.utils.AppPreferences
import com.example.wanderbee.utils.Resource
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Chat repository interface.
 *
 * Every method talks to the backend chat-service via REST ([ChatApiService]).
 * All list/object responses are wrapped in [Resource] so ViewModels can
 * distinguish network errors, server errors, and success without scattered
 * try/catch blocks.
 */
interface ChatRepository {

    suspend fun getRooms(): Resource<List<ChatRoom>>
    suspend fun createGroupRoom(name: String, participantIds: List<String>): Resource<ChatRoom>
    suspend fun createPrivateRoom(otherUserId: String): Resource<ChatRoom>
    suspend fun getMessages(roomId: String): Resource<List<ChatMessage>>
    suspend fun sendMessage(request: SendMessageRequest): Resource<ChatMessage>
    suspend fun deleteMessage(messageId: String, roomId: String): Resource<Unit>
    suspend fun leaveRoom(roomId: String): Resource<ChatRoom>
    suspend fun getCurrentUserId(): String?
    suspend fun getChatPreviews(): Resource<List<ChatPreview>>
}


class DefaultChatRepository @Inject constructor(
    private val chatApiService: ChatApiService,
    private val appPreferences: AppPreferences
) : ChatRepository {

    companion object {
        private const val TAG = "ChatRepository"
    }

    // ── Rooms ──────────────────────────────────────────────────────────────

    override suspend fun getRooms(): Resource<List<ChatRoom>> = safeCall(TAG, "getRooms") {
        val r = chatApiService.getRooms()
        if (r.isSuccessful) Resource.Success(r.body() ?: emptyList())
        else Resource.httpError(r.code())
    }

    override suspend fun createGroupRoom(
        name: String,
        participantIds: List<String>
    ): Resource<ChatRoom> = safeCall(TAG, "createGroupRoom") {
        val request = CreateRoomRequest(name = name, isGroup = true, participantIds = participantIds)
        val r = chatApiService.createGroupRoom(request)
        if (r.isSuccessful) Resource.Success(r.body()!!)
        else Resource.httpError(r.code())
    }

    override suspend fun createPrivateRoom(otherUserId: String): Resource<ChatRoom> =
        safeCall(TAG, "createPrivateRoom") {
            val r = chatApiService.createPrivateRoom(otherUserId)
            if (r.isSuccessful) Resource.Success(r.body()!!)
            else Resource.httpError(r.code())
        }

    // ── Messages ───────────────────────────────────────────────────────────

    override suspend fun getMessages(roomId: String): Resource<List<ChatMessage>> =
        safeCall(TAG, "getMessages") {
            val r = chatApiService.getMessages(roomId)
            if (r.isSuccessful) Resource.Success(r.body() ?: emptyList())
            else Resource.httpError(r.code())
        }

    override suspend fun sendMessage(request: SendMessageRequest): Resource<ChatMessage> =
        safeCall(TAG, "sendMessage") {
            val r = chatApiService.sendMessage(request)
            if (r.isSuccessful) Resource.Success(r.body()!!)
            else Resource.httpError(r.code())
        }

    override suspend fun deleteMessage(messageId: String, roomId: String): Resource<Unit> =
        safeCall(TAG, "deleteMessage") {
            val userId = getCurrentUserId() ?: return@safeCall Resource.Error("Not authenticated")
            val request = DeleteMessageRequest(messageId, roomId, userId)
            val r = chatApiService.deleteMessage(request)
            if (r.isSuccessful) Resource.Success(Unit)
            else Resource.httpError(r.code())
        }

    override suspend fun leaveRoom(roomId: String): Resource<ChatRoom> =
        safeCall(TAG, "leaveRoom") {
            val r = chatApiService.leaveRoom(roomId)
            if (r.isSuccessful) Resource.Success(r.body()!!)
            else Resource.httpError(r.code())
        }

    // ── User ───────────────────────────────────────────────────────────────

    override suspend fun getCurrentUserId(): String? = appPreferences.userEmail.first()

    // ── Chat Previews ──────────────────────────────────────────────────────

    override suspend fun getChatPreviews(): Resource<List<ChatPreview>> {
        return when (val roomsResult = getRooms()) {
            is Resource.Success -> {
                val previews = roomsResult.data.map { room ->
                    ChatPreview(
                        chatId = room.id,
                        isGroup = room.isGroup,
                        name = room.name,
                        lastMessage = room.lastMessage?.content ?: "Start chatting!",
                        lastMessageTime = room.lastMessage?.timestamp?.let {
                            parseTimestamp(it)
                        } ?: 0L,
                        destination = if (room.isGroup) room.name else null
                    )
                }.sortedByDescending { it.lastMessageTime }
                Resource.Success(previews)
            }
            is Resource.Error -> roomsResult
            is Resource.Loading -> Resource.loading()
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun parseTimestamp(iso: String): Long = try {
        java.time.LocalDateTime.parse(iso)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (_: Exception) { 0L }
}

/**
 * Central error-handling helper used by both [DefaultChatRepository] and
 * [DestinationRepository].  Catches [IOException] (no network) and
 * [HttpException] (non-2xx Retrofit throws) in addition to any other runtime
 * exception.
 */
internal inline fun <T> safeCall(
    tag: String,
    operation: String,
    block: () -> Resource<T>
): Resource<T> = try {
    block()
} catch (e: IOException) {
    Log.e(tag, "$operation – network error", e)
    Resource.Error("No internet connection. Please check your network.")
} catch (e: HttpException) {
    Log.e(tag, "$operation – HTTP ${e.code()}", e)
    Resource.httpError(e.code())
} catch (e: Exception) {
    Log.e(tag, "$operation – unexpected error", e)
    Resource.Error("Unexpected error: ${e.message ?: "unknown"}")
}

