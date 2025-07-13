package com.example.wanderbee.screens.chat

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wanderbee.utils.AllChatsScreenTopBar
import com.example.wanderbee.utils.BottomNavigationBar
import com.example.wanderbee.R

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AllChatsScreen(
    navController: NavController,
    chatViewModel: ChatViewModel
) {
    var selectedTab by remember { mutableStateOf("Chat") }
    val chatPreviews by chatViewModel.chatPreviews.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        chatViewModel.loadAllChats()
    }

    // Filter chat previews based on search query
    val filteredChatPreviews = remember(chatPreviews, searchQuery) {
        if (searchQuery.isEmpty()) {
            chatPreviews
        } else {
            chatPreviews.filter { preview ->
                preview.name.contains(searchQuery, ignoreCase = true) ||
                preview.destination?.contains(searchQuery, ignoreCase = true) == true ||
                preview.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = { AllChatsScreenTopBar(navController = navController) },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedTab,
                onItemSelected = { tab -> selectedTab = tab },
                navController = navController
            )
        }
    ) {
        Surface(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onClearSearch = { 
                        searchQuery = ""
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.padding(16.dp)
                )

                // Chat List
                val scrollState = rememberScrollState()
                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                    if (filteredChatPreviews.isEmpty()) {
                        if (searchQuery.isEmpty()) {
                            // No chats available
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No chats available",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    fontFamily = FontFamily(Font(R.font.istokweb_regular))
                                )
                            }
                        } else {
                            // No search results
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No chats found for \"$searchQuery\"",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    fontFamily = FontFamily(Font(R.font.istokweb_regular))
                                )
                            }
                        }
                    } else {
                        filteredChatPreviews.forEach { preview ->
                            ChatScreenCard(
                                dest = preview.destination ?: "",
                                name = preview.name,
                                message = preview.lastMessage,
                                onCardClick = {
                                    if (preview.isGroup) {
                                        navController.navigate("GroupChat/${preview.chatId}/${preview.name}")
                                    } else {
                                        navController.navigate("PrivateChat/${preview.chatId}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Search chats...",
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
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
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


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ChatScreenCard(
    dest: String,
    name: String,
    message: String,
    lastMessageTime: Long = 0L,
    onCardClick: () -> Unit = {}
) {
    val timeAgo = remember(lastMessageTime) {
        if (lastMessageTime == 0L) "" else getRelativeTime(lastMessageTime)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(2.dp)
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start
        ) {

            // Avatar
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(shape = CircleShape)
                        .background(color = MaterialTheme.colorScheme.onBackground)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Chat content
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = name,
                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = timeAgo,
                        fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = message,
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    maxLines = 1,
                    fontSize = 16.sp,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location Symbol",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = dest,
                        color = MaterialTheme.colorScheme.secondary,
                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        else -> "$days day${if (days > 1) "s" else ""} ago"
    }
}

