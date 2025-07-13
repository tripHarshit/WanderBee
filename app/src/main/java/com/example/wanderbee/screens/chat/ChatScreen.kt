package com.example.wanderbee.screens.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wanderbee.data.remote.models.chat.ChatUser
import com.example.wanderbee.utils.MessageBubble
import com.example.wanderbee.utils.MessageInput
import com.example.wanderbee.utils.PrivateChatDialog
import com.example.wanderbee.utils.InnerChatScreenTopBar
import com.example.wanderbee.R
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
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // Simulated user cache (replace with real user fetching if avatars are added)
    val userCache = remember { mutableStateMapOf<String, ChatUser>() }
    val currentUserId = chatViewModel.getCurrentUserId()

    // Filter messages based on search query
    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isEmpty()) {
            messages
        } else {
            messages.filter { message ->
                message.text.contains(searchQuery, ignoreCase = true) ||
                message.senderName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(destinationId) {
        chatViewModel.listenToGroupMessages(destinationId)
    }

    Scaffold(
        topBar = {
            if (showSearch) {
                SearchTopBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onClearSearch = { 
                        searchQuery = ""
                        showSearch = false
                        focusManager.clearFocus()
                    },
                    onBackClick = { showSearch = false }
                )
            } else {
                InnerChatScreenTopBar(
                    navController = navController, 
                    heading = destinationName,
                    onSearchClick = { showSearch = true }
                )
            }
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
                if (filteredMessages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (searchQuery.isEmpty()) "No messages yet. Start the conversation!" 
                            else "No messages found for \"$searchQuery\"",
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
                        itemsIndexed(filteredMessages.reversed()) { idx, msg ->
                            val isCurrentUser = msg.senderId == currentUserId
                            val sender = userCache[msg.senderId] ?: ChatUser(
                                id = msg.senderId,
                                name = msg.senderName
                            )
                            MessageBubble(
                                message = msg,
                                isCurrentUser = isCurrentUser,
                                sender = sender,
                                onMessageClick = { message ->
                                    if (!isCurrentUser) {
                                        showDialog = Pair(message.senderId, message.senderName)
                                    }
                                }
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

@Composable
fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onBackClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search messages...",
                        fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 16.sp
                )
            )
        }
    }
}
