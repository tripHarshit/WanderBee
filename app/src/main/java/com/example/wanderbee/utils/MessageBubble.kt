package com.example.wanderbee.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wanderbee.data.remote.models.chat.ChatMessage
import com.example.wanderbee.data.remote.models.chat.ChatUser
import com.example.wanderbee.screens.chat.getRelativeTime

@Composable
fun MessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean,
    sender: ChatUser?,
    onMessageClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val alignment = if (isCurrentUser) Arrangement.End else Arrangement.Start
    val shape = if (isCurrentUser) RoundedCornerShape(16.dp, 0.dp, 16.dp, 16.dp) else RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = alignment,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isCurrentUser) {
            // Avatar
            if (sender?.photoUrl != null) {
                AsyncImage(
                    model = sender.photoUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onBackground)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            if (!isCurrentUser) {
                Text(
                    text = sender?.name ?: message.senderName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .background(bubbleColor, shape)
                    .clickable(enabled = onMessageClick != null) { onMessageClick?.invoke() }
                    .padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = getRelativeTime(message.timestamp.toDate().time),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp, end = 2.dp)
            )
        }
        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
    }
} 