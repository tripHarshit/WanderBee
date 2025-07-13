package com.example.wanderbee.data.repository

import android.util.Log
import com.example.wanderbee.data.remote.models.chat.ChatMessage
import com.example.wanderbee.data.remote.models.chat.ChatPreview
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


interface ChatRepository {

    suspend fun joinDestinationChat(destinationId: String, destinationName: String)

    suspend fun removeExpiredParticipants(destinationId: String)

    suspend fun sendGroupMessage(destinationId: String, message: ChatMessage)

    fun listenToGroupMessages(destinationId: String, onMessages: (List<ChatMessage>) -> Unit): ListenerRegistration

    suspend fun getOrCreatePrivateChat(userId1: String, userId2: String): String

    suspend fun sendPrivateMessage(chatId: String, message: ChatMessage)

    fun listenToPrivateMessages(chatId: String, onMessages: (List<ChatMessage>) -> Unit): ListenerRegistration

    suspend fun getGroupChatPreviews(userId: String): List<ChatPreview>

    suspend fun getPrivateChatPreviews(userId: String): List<ChatPreview>

    suspend fun getUserDetails(userId: String): com.example.wanderbee.data.remote.models.chat.ChatUser
}

class DefaultChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ChatRepository {

    override suspend fun joinDestinationChat(destinationId: String, destinationName: String) {
        val userId = auth.currentUser?.uid ?: return
        val chatRoomRef = firestore.collection("chatRooms").document(destinationId)

        firestore.runTransaction { transaction ->

            val snapshot = transaction.get(chatRoomRef)

            val participants = (snapshot.get("participants") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()

            // Get joinDates as map string to Firestore Timestamp
            val joinDatesMap = snapshot.get("participantJoinDates") as? Map<String, Any> ?: emptyMap()
            val joinDates = mutableMapOf<String, com.google.firebase.Timestamp>()

            joinDatesMap.forEach { (key, value) ->
                when (value) {
                    is com.google.firebase.Timestamp -> joinDates[key] = value
                    is java.util.Date -> joinDates[key] = com.google.firebase.Timestamp(value)
                    is Long -> joinDates[key] = com.google.firebase.Timestamp(java.util.Date(value))
                    else -> { /* ignore */ }
                }
            }

            if (!participants.contains(userId)) {
                participants.add(userId)
                joinDates[userId] = com.google.firebase.Timestamp.now()
            }

            val data = mapOf(
                "id" to destinationId,
                "destinationName" to destinationName,
                "participants" to participants,
                "participantJoinDates" to joinDates,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            if (!snapshot.exists()) {
                transaction.set(chatRoomRef, data)
                Log.d("ChatRepository", "Created new chat room for $destinationId with participants: $participants")
            } else {
                transaction.update(chatRoomRef, mapOf(
                    "participants" to participants,
                    "participantJoinDates" to joinDates
                ))
                Log.d("ChatRepository", "Updated chat room $destinationId participants: $participants")
            }
        }.await()
    }



    override suspend fun removeExpiredParticipants(destinationId: String) {
        val chatRoomRef = firestore.collection("chatRooms").document(destinationId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(chatRoomRef)

            val joinDatesMap = snapshot.get("participantJoinDates") as? Map<String, Any>
            val joinDates = mutableMapOf<String, com.google.firebase.Timestamp>()

            // Convert to Firebase Timestamp consistently
            joinDatesMap?.forEach { (key, value) ->
                when (value) {
                    is com.google.firebase.Timestamp -> joinDates[key] = value
                    is java.util.Date -> joinDates[key] = com.google.firebase.Timestamp(value)
                    is Long -> joinDates[key] = com.google.firebase.Timestamp(java.util.Date(value))
                }
            }

            val now = com.google.firebase.Timestamp.now() // Use Firebase Timestamp
            val sixMonthsInMillis = 183L * 24 * 60 * 60 * 1000

            val toRemove = joinDates.filter { (_, timestamp) ->
                (now.toDate().time - timestamp.toDate().time) > sixMonthsInMillis
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

    override suspend fun getGroupChatPreviews(userId: String): List<ChatPreview> {
        val chatRooms = firestore.collection("chatRooms")
            .whereArrayContains("participants", userId)
            .get()
            .await()

        return chatRooms.documents.mapNotNull { doc ->
            val destinationName = doc.getString("destinationName") ?: return@mapNotNull null
            val destinationId = doc.getString("id") ?: doc.id

            // Get the latest message from the subcollection
            val latestMessageQuery = firestore.collection("chatRooms")
                .document(destinationId)
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val latest = latestMessageQuery.documents.firstOrNull()?.toObject(ChatMessage::class.java)
            ChatPreview(
                chatId = destinationId,
                isGroup = true,
                name = destinationName,
                lastMessage = latest?.text ?: "Start chatting!",
                lastMessageTime = latest?.timestamp?.toDate()?.time ?: 0L,
                destination = destinationName
            )
        }
    }

    override suspend fun getPrivateChatPreviews(userId: String): List<ChatPreview> {
        val chats = firestore.collection("privateChats")
            .whereArrayContains("users", userId)
            .get()
            .await()

        return chats.documents.mapNotNull { doc ->
            val chatId = doc.getString("id") ?: doc.id
            val users = doc.get("users") as? List<*> ?: return@mapNotNull null
            val otherUserId = users.filterIsInstance<String>().firstOrNull { it != userId } ?: return@mapNotNull null

            // Get the other user's display name (optional: you could cache this)
            val userDoc = firestore.collection("users").document(otherUserId).get().await()
            val otherUserName = userDoc.getString("name") ?: "User"

            val latestMessageQuery = firestore.collection("privateChats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val latest = latestMessageQuery.documents.firstOrNull()?.toObject(ChatMessage::class.java)

            ChatPreview(
                chatId = chatId,
                isGroup = false,
                name = otherUserName,
                lastMessage = latest?.text ?: "Start chatting!",
                lastMessageTime = latest?.timestamp?.toDate()?.time ?: 0L
            )
        }
    }

    override suspend fun getUserDetails(userId: String): com.example.wanderbee.data.remote.models.chat.ChatUser {
        val userDoc = firestore.collection("users").document(userId).get().await()
        val name = userDoc.getString("displayName") ?: userDoc.getString("name") ?: "User"
        val photoUrl = userDoc.getString("photoUrl")
        return com.example.wanderbee.data.remote.models.chat.ChatUser(id = userId, name = name, photoUrl = photoUrl)
    }


}
