package com.example.wanderbee.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.wanderbee.MainActivity
import com.example.wanderbee.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class WanderBeeMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "WanderBeeFCM"
        private const val CHANNEL_ID = "wanderbee_notifications"
        private const val CHANNEL_NAME = "WanderBee Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications from WanderBee travel app"
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // TODO: Send this token to your server to associate it with the user
        sendRegistrationToServer(token)
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message received successfully!")
        
        // Check if message contains a data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }
        
        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            Log.d(TAG, "Message Notification Title: ${it.title}")
            sendNotification(it.title, it.body)
        }
        
        // Handle data-only messages
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "WanderBee"
            val body = remoteMessage.data["body"] ?: "You have a new notification"
            val type = remoteMessage.data["type"] ?: "general"
            
            Log.d(TAG, "Processing data message - Title: $title, Body: $body, Type: $type")
            sendNotification(title, body, type)
        }
    }
    
    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement token registration with your backend
        Log.d(TAG, "Sending FCM token to server: $token")
        
        // Example: You can store the token in Firestore for the current user
        // This would typically be done in your AuthViewModel or a dedicated service
    }
    
    private fun sendNotification(title: String?, body: String?, type: String = "general") {
        Log.d(TAG, "Creating notification - Title: $title, Body: $body, Type: $type")
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_type", type)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title ?: "WanderBee")
            .setContentText(body ?: "You have a new notification")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
        
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "Notification sent with ID: $notificationId")
    }
} 