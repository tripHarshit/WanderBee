package com.example.wanderbee.screens.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wanderbee.data.remote.models.chat.ChatUser
import com.example.wanderbee.utils.MessageBubble
import com.example.wanderbee.utils.MessageInput
import com.example.wanderbee.utils.InnerChatScreenTopBar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PrivateChatScreen(
    navController: NavController,
    chatId: String,
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val messages by chatViewModel.privateMessages.collectAsState()
    var input by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = chatViewModel.getCurrentUserId()
    val userCache = remember { mutableStateMapOf<String, ChatUser>() }
    var otherUserName by remember { mutableStateOf<String?>(null) }

    // Listen to private messages
    LaunchedEffect(chatId) {
        chatViewModel.listenToPrivateMessagesByChatId(chatId)
    }

    // Fetch the other user's name for the heading
    LaunchedEffect(chatId) {
        val currentUserId = chatViewModel.getCurrentUserId()
        if (currentUserId != null) {
            val firestore = FirebaseFirestore.getInstance()
            val chatDoc = firestore.collection("privateChats").document(chatId).get().await()
            val users = chatDoc.get("users") as? List<*> ?: emptyList<String>()
            val otherUserId = users.filterIsInstance<String>().firstOrNull { it != currentUserId }
            if (otherUserId != null) {
                val userDoc = firestore.collection("users").document(otherUserId).get().await()
                otherUserName = userDoc.getString("displayName") ?: userDoc.getString("name") ?: "User"
            }
        }
    }

    Scaffold(
        topBar = {
            InnerChatScreenTopBar(navController = navController, heading = otherUserName ?: "Private Chat")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Messages area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No messages yet. Start the conversation!",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true,
                        contentPadding = PaddingValues(
                            horizontal = 8.dp,
                            vertical = 8.dp
                        )
                    ) {
                        itemsIndexed(messages.reversed()) { idx, msg ->
                            val isCurrentUser = msg.senderId == currentUserId
                            val sender = userCache[msg.senderId] ?: ChatUser(
                                id = msg.senderId,
                                name = msg.senderName
                            )
                            MessageBubble(
                                message = msg,
                                isCurrentUser = isCurrentUser,
                                sender = sender
                            )
                        }
                    }
                }

                if (isRefreshing) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Message input at the bottom of content area
            MessageInput(
                value = input,
                onValueChange = { input = it },
                onSend = {
                    isSending = true
                    chatViewModel.sendPrivateMessageByChatId(chatId, input)
                    input = ""
                    isSending = false
                    focusManager.clearFocus()
                },
                isSending = isSending,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )
        }
    }
}
