package com.example.wanderbee.utils

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    companion object {
        private const val TAG = "NotificationManager"
        private const val USERS_COLLECTION = "users"
        private const val FCM_TOKENS_COLLECTION = "fcm_tokens"
    }
    
    /**
     * Get the current FCM token and store it in Firestore
     */
    suspend fun getAndStoreFCMToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM Token: $token")
            
            // Store token in Firestore
            token?.let { storeTokenInFirestore(it) }
            
            token
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token", e)
            null
        }
    }
    
    /**
     * Store FCM token in Firestore for the current user
     */
    private suspend fun storeTokenInFirestore(token: String) {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userTokenData = hashMapOf(
                    "userId" to currentUser.uid,
                    "email" to currentUser.email,
                    "token" to token,
                    "timestamp" to System.currentTimeMillis(),
                    "deviceInfo" to getDeviceInfo()
                )
                
                // Store in users collection
                firestore.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .collection("tokens")
                    .document("fcm_token")
                    .set(userTokenData)
                    .await()
                
                // Also store in a separate collection for easy querying
                firestore.collection(FCM_TOKENS_COLLECTION)
                    .document(currentUser.uid)
                    .set(userTokenData)
                    .await()
                
                Log.d(TAG, "FCM token stored successfully for user: ${currentUser.uid}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error storing FCM token", e)
        }
    }
    
    /**
     * Delete FCM token when user logs out
     */
    suspend fun deleteFCMToken() {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Delete from users collection
                firestore.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .collection("tokens")
                    .document("fcm_token")
                    .delete()
                    .await()
                
                // Delete from FCM tokens collection
                firestore.collection(FCM_TOKENS_COLLECTION)
                    .document(currentUser.uid)
                    .delete()
                    .await()
                
                Log.d(TAG, "FCM token deleted successfully for user: ${currentUser.uid}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting FCM token", e)
        }
    }
    
    /**
     * Subscribe to a topic for broadcast notifications
     */
    suspend fun subscribeToTopic(topic: String) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            Log.d(TAG, "Subscribed to topic: $topic")
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to topic: $topic", e)
        }
    }
    
    /**
     * Unsubscribe from a topic
     */
    suspend fun unsubscribeFromTopic(topic: String) {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            Log.d(TAG, "Unsubscribed from topic: $topic")
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing from topic: $topic", e)
        }
    }
    
    /**
     * Get device information for token storage
     */
    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "manufacturer" to android.os.Build.MANUFACTURER,
            "model" to android.os.Build.MODEL,
            "androidVersion" to android.os.Build.VERSION.RELEASE,
            "sdkVersion" to android.os.Build.VERSION.SDK_INT.toString()
        )
    }
    
    /**
     * Send a local notification (for testing purposes)
     */
    fun sendLocalNotification(title: String, body: String) {
        // This would typically be handled by the WanderBeeMessagingService
        // But you can also trigger local notifications from here if needed
        Log.d(TAG, "Local notification: $title - $body")
    }

    /**
     * Test FCM setup by sending a test notification
     */
    suspend fun testNotificationSetup() {
        try {
            val token = getAndStoreFCMToken()
            if (token != null) {
                Log.d(TAG, "FCM setup successful. Token: $token")
                // You can also send a test notification to this token via your backend
            } else {
                Log.e(TAG, "Failed to get FCM token")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error testing FCM setup", e)
        }
    }

    /**
     * Get current FCM token for debugging
     */
    suspend fun getCurrentToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current token", e)
            null
        }
    }
} 