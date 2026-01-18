package com.jurysim.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jurysim.data.model.Message

@Composable
fun ChatBubble(message: Message) {
    val isUser = message.isUser
    val speaker = message.speaker ?: "Unknown"
    
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

        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
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
