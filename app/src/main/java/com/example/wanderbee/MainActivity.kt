package com.example.wanderbee

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wanderbee.navigation.WanderBeeNavigation
import com.example.wanderbee.ui.theme.WanderBeeTheme
import com.example.wanderbee.utils.NotificationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var notificationManager: NotificationManager
    
    companion object {
        private const val TAG = "MainActivity"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check notification permissions
        checkNotificationPermissions()
        
        // Test FCM setup
        testFCMSetup()
        
        setContent {
            WanderBeeTheme {
                WanderBeeNavigation()
            }
        }
    }
    
    private fun checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Notification permission not granted, requesting...")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            } else {
                Log.d(TAG, "Notification permission already granted")
            }
        } else {
            Log.d(TAG, "Android version < 13, notification permission not required")
        }
    }
    
    private fun testFCMSetup() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get current token
                val token = Firebase.messaging.token.await()
                Log.d(TAG, "Current FCM Token: $token")
                
                // Test notification manager
                notificationManager.testNotificationSetup()
                
                // Check if user is logged in and store token
                if (FirebaseAuth.getInstance().currentUser != null) {
                    notificationManager.getAndStoreFCMToken()
                    Log.d(TAG, "User is logged in, token stored")
                } else {
                    Log.d(TAG, "User is not logged in")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error testing FCM setup", e)
            }
        }
    }
}
