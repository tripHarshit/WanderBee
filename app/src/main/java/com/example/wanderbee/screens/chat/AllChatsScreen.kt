package com.example.wanderbee.screens.chat

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.wanderbee.utils.AllChatsScreenTopBar
import com.example.wanderbee.utils.BottomNavigationBar
import com.example.wanderbee.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wanderbee.screens.profile.ProfileViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateMapOf

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

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    // State for profile/city images per chatId
    val profilePicUrls = remember { mutableStateMapOf<String, String>() }
    val loadingStates = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        chatViewModel.loadAllChats()
    }

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
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No chats found.", color = MaterialTheme.colorScheme.onBackground)
                        }
                    } else {
                        filteredChatPreviews.forEach { preview ->
                            val chatId = preview.chatId
                            val isGroup = preview.isGroup
                            val destination = preview.destination ?: ""
                            val name = preview.name
                            val lastMessage = preview.lastMessage
                            val lastMessageTime = preview.lastMessageTime

                            val profilePicUrl = profilePicUrls[chatId]
                            val isLoading = loadingStates[chatId] == true
                            var loadError by remember(chatId) { mutableStateOf(false) }

                            LaunchedEffect(chatId, isGroup, destination, name) {
                                if (profilePicUrl == null && !isLoading) {
                                    loadingStates[chatId] = true
                                    loadError = false
                                    if (isGroup) {
                                        val cityQuery = destination.split("_").firstOrNull() ?: destination
                                        profileViewModel.loadCityCoverImage(cityQuery) { url ->
                                            if (url != null) profilePicUrls[chatId] = url
                                            else loadError = true
                                            loadingStates[chatId] = false
                                        }
                                    } else {
                                        val currentUserId = chatViewModel.auth.currentUser?.uid
                                        val otherUserId = getOtherUserIdFromChatId(chatId, currentUserId)
                                        if (otherUserId != null) {
                                            coroutineScope.launch {
                                                val url = profileViewModel.getUserProfilePictureUrl(otherUserId)
                                                if (url != null) profilePicUrls[chatId] = url
                                                else loadError = true
                                                loadingStates[chatId] = false
                                            }
                                        } else {
                                            loadError = true
                                            loadingStates[chatId] = false
                                        }
                                    }
                                }
                            }

                            ChatScreenCard(
                                dest = destination,
                                name = name,
                                message = lastMessage,
                                lastMessageTime = lastMessageTime,
                                profilePicUrl = profilePicUrl ?: "",
                                isLoading = isLoading,
                                loadError = loadError,
                                onCardClick = {
                                    if (isGroup) {
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
            ),
            shape = RoundedCornerShape(12.dp)
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
    profilePicUrl: String,
    isLoading: Boolean = false,
    loadError: Boolean = false,
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
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(32.dp).align(Alignment.Center)
                            )
                        }
                        loadError || profilePicUrl.isEmpty() -> {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Default Profile",
                                tint = MaterialTheme.colorScheme.background,
                                modifier = Modifier.size(40.dp).align(Alignment.Center)
                            )
                        }
                        else -> {
                            AsyncImage(
                                model = profilePicUrl,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                onError = { /* Show fallback icon above */ },
                                onLoading = { /* Optionally show a loading indicator */ }
                            )
                        }
                    }
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

// Helper to extract the other user's id from a chatId (assuming chatId encodes both user ids, or you have a way to get it)
fun getOtherUserIdFromChatId(chatId: String, currentUserId: String?): String? {
    // If chatId is a concatenation of user ids, split and return the one that's not currentUserId
    // Otherwise, you may need to fetch from Firestore or ChatPreview
    // Placeholder logic:
    val ids = chatId.split("_")
    return ids.firstOrNull { it != currentUserId }
}

