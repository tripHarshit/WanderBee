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
import com.example.wanderbee.utils.PrivateChatDialog
import com.example.wanderbee.utils.InnerChatScreenTopBar
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ChatScreen(
    navController: NavController,
    destinationId: String,
    destinationName: String,
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val messages by chatViewModel.groupMessages.collectAsState()
    var input by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // Simulated user cache (replace with real user fetching if avatars are added)
    val userCache = remember { mutableStateMapOf<String, ChatUser>() }
    val currentUserId = chatViewModel.getCurrentUserId()

    LaunchedEffect(destinationId) {
        chatViewModel.listenToGroupMessages(destinationId)
    }

    Scaffold(
        topBar = {
            InnerChatScreenTopBar(navController = navController, heading = destinationName)
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
                        itemsIndexed(messages.reversed()) { idx, msg ->
                            val isCurrentUser = msg.senderId == currentUserId
                            val sender = userCache[msg.senderId] ?: ChatUser(
                                id = msg.senderId,
                                name = msg.senderName
                            )
                            MessageBubble(
                                message = msg,
                                isCurrentUser = isCurrentUser,
                                sender = sender,
                                onMessageClick = if (!isCurrentUser) {
                                    { showDialog = Pair(msg.senderId, msg.senderName) }
                                } else null
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
                    chatViewModel.sendGroupMessage(destinationId, input)
                    input = ""
                    isSending = false
                    focusManager.clearFocus()
                },
                isSending = isSending,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )
        }
    }

    // Private chat dialog
    showDialog?.let { (userId, userName) ->
        PrivateChatDialog(
            username = userName,
            onConfirm = {
                coroutineScope.launch {
                    val chatId = chatViewModel.startOrGetPrivateChat(userId)
                    chatId?.let {
                        showDialog = null
                        navController.navigate("PrivateChat/$it")
                    }
                }
            },
            onDismiss = { showDialog = null }
        )
    }
}
