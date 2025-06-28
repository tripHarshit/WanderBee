package com.example.wanderbee.data.repository

import com.example.wanderbee.data.remote.models.chat.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


interface ChatRepository {
    /*
     * Join or create a destination chat room when itinerary is saved.
     * Adds the current user to the chat room's participants list and records their join date.
     */
    suspend fun joinDestinationChat(destinationId: String, destinationName: String)

    /*
     * Remove users who joined a group chat more than 2 years ago.
     * Cleans up expired participants from the chat room.
     */
    suspend fun removeExpiredParticipants(destinationId: String)

    /*
     * Send a message to a group chat.
     * Adds a new message document to the group's messages subcollection.
     */
    suspend fun sendGroupMessage(destinationId: String, message: ChatMessage)

    /*
     * Listen for group chat messages in real-time.
     * Invokes the callback with the latest list of messages whenever there is a change.
     * Returns a ListenerRegistration that can be used to remove the listener.
     */
    fun listenToGroupMessages(destinationId: String, onMessages: (List<ChatMessage>) -> Unit): ListenerRegistration

    /*
     * Create or get a private chat between two users.
     * Returns the chat ID for the private chat, creating it if necessary.
     */
    suspend fun getOrCreatePrivateChat(userId1: String, userId2: String): String

    /*
     * Send a message to a private chat.
     * Adds a new message document to the private chat's messages subcollection.
     */
    suspend fun sendPrivateMessage(chatId: String, message: ChatMessage)

    /*
     * Listen for private chat messages in real-time.
     * Invokes the callback with the latest list of messages whenever there is a change.
     * Returns a ListenerRegistration that can be used to remove the listener.
     */
    fun listenToPrivateMessages(chatId: String, onMessages: (List<ChatMessage>) -> Unit): ListenerRegistration
}

class DefaultChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ChatRepository {

    override suspend fun joinDestinationChat(destinationId: String, destinationName: String) {
        val userId = auth.currentUser?.uid ?: return
        val chatRoomRef = firestore.collection("chatRooms").document(destinationId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(chatRoomRef)
            val participants = (snapshot.get("participants") as? List<*>)
                ?.filterIsInstance<String>()
                ?.toMutableList() ?: mutableListOf()

            // Handle participantJoinDates properly
            val joinDatesMap = snapshot.get("participantJoinDates") as? Map<String, Any>
            val joinDates = mutableMapOf<String, Timestamp>()

            // Convert existing join dates
            joinDatesMap?.forEach { (key, value) ->
                when (value) {
                    is Timestamp -> joinDates[key] = value
                    is com.google.firebase.Timestamp -> joinDates[key] = value
                    is java.util.Date -> joinDates[key] = Timestamp(value)
                    is Long -> joinDates[key] = Timestamp(java.util.Date(value))
                }
            }

            if (!participants.contains(userId)) {
                participants.add(userId)
                joinDates[userId] = Timestamp.now()
            }

            if (!snapshot.exists()) {
                // Create new document
                transaction.set(chatRoomRef, mapOf(
                    "id" to destinationId,
                    "destinationName" to destinationName,
                    "participants" to participants,
                    "participantJoinDates" to joinDates,
                    "createdAt" to Timestamp.now()
                ))
            } else {
                transaction.update(chatRoomRef, mapOf(
                    "participants" to participants,
                    "participantJoinDates" to joinDates
                ))
            }
        }.await()
    }

    override suspend fun removeExpiredParticipants(destinationId: String) {
        val chatRoomRef = firestore.collection("chatRooms").document(destinationId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(chatRoomRef)

            // Handle participantJoinDates properly
            val joinDatesMap = snapshot.get("participantJoinDates") as? Map<String, Any>
            val joinDates = mutableMapOf<String, Timestamp>()

            // Convert existing join dates
            joinDatesMap?.forEach { (key, value) ->
                when (value) {
                    is Timestamp -> joinDates[key] = value
                    is com.google.firebase.Timestamp -> joinDates[key] = value
                    is java.util.Date -> joinDates[key] = Timestamp(value)
                    is Long -> joinDates[key] = Timestamp(java.util.Date(value))
                }
            }

            val now = Timestamp.now()
            val sixMonths = 183 * 24 * 60 * 60 * 1000
            val toRemove = joinDates.filter {
                now.toDate().time - it.value.toDate().time > sixMonths
            }.keys

            if (toRemove.isNotEmpty()) {
                val participants = (snapshot.get("participants") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toMutableList() ?: mutableListOf()

                toRemove.forEach { userId ->
                    participants.remove(userId)
                    joinDates.remove(userId)
                }

                transaction.update(chatRoomRef, mapOf(
                    "participants" to participants,
                    "participantJoinDates" to joinDates
                ))
            }
        }.await()
    }

    override suspend fun sendGroupMessage(destinationId: String, message: ChatMessage) {
        firestore.collection("chatRooms")
            .document(destinationId)
            .collection("messages")
            .add(message)
            .await()
    }

    override fun listenToGroupMessages(
        destinationId: String,
        onMessages: (List<ChatMessage>) -> Unit
    ): ListenerRegistration {
        return firestore.collection("chatRooms")
            .document(destinationId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                val messages = snapshot?.documents?.mapNotNull {
                    it.toObject(ChatMessage::class.java)
                } ?: emptyList()
                onMessages(messages)
            }
    }

    override suspend fun getOrCreatePrivateChat(userId1: String, userId2: String): String {
        val chatId = if (userId1 < userId2) "${userId1}_$userId2" else "${userId2}_$userId1"
        val chatRef = firestore.collection("privateChats").document(chatId)
        val doc = chatRef.get().await()

        if (!doc.exists()) {
            chatRef.set(mapOf(
                "id" to chatId,
                "users" to listOf(userId1, userId2),
                "createdAt" to Timestamp.now()
            )).await()
        }
        return chatId
    }

    override suspend fun sendPrivateMessage(chatId: String, message: ChatMessage) {
        firestore.collection("privateChats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .await()
    }

    override fun listenToPrivateMessages(
        chatId: String,
        onMessages: (List<ChatMessage>) -> Unit
    ): ListenerRegistration {
        return firestore.collection("privateChats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                val messages = snapshot?.documents?.mapNotNull {
                    it.toObject(ChatMessage::class.java)
                } ?: emptyList()
                onMessages(messages)
            }
    }
}
