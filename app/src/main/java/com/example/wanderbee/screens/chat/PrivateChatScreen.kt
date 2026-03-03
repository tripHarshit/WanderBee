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
import com.example.wanderbee.utils.MessageBubble
import com.example.wanderbee.utils.MessageInput
import com.example.wanderbee.utils.InnerChatScreenTopBar

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
    val focusManager = LocalFocusManager.current
    val currentUserId = chatViewModel.getCurrentUserId()

    // Load message history for this room
    LaunchedEffect(chatId) {
        chatViewModel.listenToPrivateMessagesByRoomId(chatId)
    }

    // Derive the other user's ID from the messages (the senderId that isn't us)
    val otherUserId by remember(messages, currentUserId) {
        derivedStateOf {
            messages.firstOrNull { it.senderId != currentUserId }?.senderId
        }
    }

    Scaffold(
        topBar = {
            InnerChatScreenTopBar(
                navController = navController,
                heading = otherUserId ?: "Private Chat"
            )
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
                        itemsIndexed(messages.reversed()) { _, msg ->
                            val isCurrentUser = msg.senderId == currentUserId
                            MessageBubble(
                                message = msg,
                                isCurrentUser = isCurrentUser
                            )
                        }
                    }
                }
            }

            // Message input
            MessageInput(
                value = input,
                onValueChange = { input = it },
                onSend = {
                    val recipient = otherUserId
                    if (recipient != null) {
                        isSending = true
                        chatViewModel.sendPrivateMessage(chatId, recipient, input)
                        input = ""
                        isSending = false
                        focusManager.clearFocus()
                    }
                },
                isSending = isSending,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )
        }
    }
}