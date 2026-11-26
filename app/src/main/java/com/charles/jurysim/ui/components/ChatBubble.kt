package com.charles.jurysim.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.charles.jurysim.data.model.Message
import com.charles.jurysim.ui.adaptive.AppWindowSize
import com.charles.jurysim.ui.adaptive.LocalAppWindowSize

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: Message,
    showPlayButton: Boolean = false,
    onPlayClick: ((Message) -> Unit)? = null,
    onReportClick: ((Message) -> Unit)? = null
) {
    val isUser = message.isUser
    val speaker = message.speaker ?: "Unknown"
    // Long-press menu state — only offered for AI-generated messages when
    // the caller wires a report handler (Google Play AI-content policy:
    // users must be able to flag offensive AI-generated content in-app).
    var reportMenuOpen by remember { mutableStateOf(false) }
    val canReport = !isUser && onReportClick != null
    val windowSize = LocalAppWindowSize.current
    val bubbleMaxWidth = when (windowSize) {
        AppWindowSize.Compact -> 320.dp
        AppWindowSize.Medium -> 420.dp
        AppWindowSize.Expanded -> 520.dp
    }
    
    // Determine colors based on speaker
    val bubbleColor = when {
        isUser -> MaterialTheme.colorScheme.primary
        speaker.contains("Judge", ignoreCase = true) -> Color(0xFF455A64) // Blue Grey
        speaker.contains("Prosecutor", ignoreCase = true) -> Color(0xFFD32F2F) // Red
        speaker.contains("Defense", ignoreCase = true) -> Color(0xFF1976D2) // Blue
        speaker.contains("Witness", ignoreCase = true) -> Color(0xFF7B1FA2) // Purple
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when {
        isUser || speaker.contains("Judge") || speaker.contains("Prosecutor") || 
        speaker.contains("Defense") || speaker.contains("Witness") -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            SpeakerAvatar(speaker, bubbleColor)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box {
        Column(
            modifier = Modifier
                .widthIn(max = bubbleMaxWidth)
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .then(
                    if (canReport) {
                        Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = { reportMenuOpen = true }
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(12.dp)
        ) {
            if (!isUser) {
                Text(
                    text = speaker,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
            if (!isUser && showPlayButton && onPlayClick != null) {
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = { onPlayClick(message) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play AI voice",
                        tint = contentColor
                    )
                }
            }
        }
        if (canReport) {
            DropdownMenu(
                expanded = reportMenuOpen,
                onDismissRequest = { reportMenuOpen = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Report this content") },
                    onClick = {
                        reportMenuOpen = false
                        onReportClick?.invoke(message)
                    }
                )
            }
        }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            SpeakerAvatar("You", bubbleColor)
        }
    }
}

@Composable
fun SpeakerAvatar(name: String, color: Color) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.8f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
