package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.data.remote.models.chat.*
import retrofit2.Response
import retrofit2.http.*


interface ChatApiService {

    // ── Rooms ──────────────────────────────────────────────────────────────

    /** Get all chat rooms the current user participates in. */
    @GET("api/v1/chat/rooms")
    suspend fun getRooms(): Response<List<ChatRoom>>

    /** Create (or get) a private 1-on-1 room. */
    @POST("api/v1/chat/rooms/private")
    suspend fun createPrivateRoom(
        @Query("userId2") otherUserId: String
    ): Response<ChatRoom>

    /** Create a new group room. */
    @POST("api/v1/chat/rooms/group")
    suspend fun createGroupRoom(
        @Body request: CreateRoomRequest
    ): Response<ChatRoom>

    /** Delete a room entirely. */
    @DELETE("api/v1/chat/rooms/{roomId}")
    suspend fun deleteRoom(@Path("roomId") roomId: String): Response<Unit>

    /** Leave a room. */
    @DELETE("api/v1/chat/rooms/{roomId}/leave")
    suspend fun leaveRoom(@Path("roomId") roomId: String): Response<ChatRoom>

    // ── Messages ───────────────────────────────────────────────────────────

    /** Fetch all messages for a room (history). */
    @GET("api/v1/chat/rooms/{roomId}/messages")
    suspend fun getMessages(
        @Path("roomId") roomId: String
    ): Response<List<ChatMessage>>

    /** Send a message via REST (alternative to STOMP). */
    @POST("api/v1/chat/messages/send")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<ChatMessage>

    /** Delete a message via REST. */
    @POST("api/v1/chat/messages/delete")
    suspend fun deleteMessage(
        @Body request: DeleteMessageRequest
    ): Response<Unit>
}
