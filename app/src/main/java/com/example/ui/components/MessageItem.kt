package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
import com.example.ui.theme.AstraCyanLight
import com.example.ui.theme.AstraIndigoLight
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageItem(
    message: MessageEntity,
    isSpeakingThisMessage: Boolean,
    onSpeakToggle: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUser = message.sender == "USER"
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            AstraAvatar(size = 32.dp, modifier = Modifier.padding(top = 2.dp, end = 8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 330.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Sender name & time header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = if (isUser) "You" else "Astra",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) MaterialTheme.colorScheme.primary else AstraCyanLight
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                )
            }

            // Bubble body
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                },
                border = if (!isUser) {
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                } else null,
                tonalElevation = 2.dp,
                modifier = Modifier.testTag(if (isUser) "user_message_bubble" else "astra_message_bubble")
            ) {
                Box(modifier = Modifier.padding(14.dp)) {
                    if (isUser) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = 22.sp
                            )
                        )
                    } else {
                        FormattedAstraText(
                            text = message.content,
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Astra Action Bar (Read aloud, Copy, Bookmark, Share)
            if (!isUser && message.status == "SUCCESS") {
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp, start = 4.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // TTS Read aloud button
                    IconButton(
                        onClick = onSpeakToggle,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("tts_button")
                    ) {
                        Icon(
                            imageVector = if (isSpeakingThisMessage) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isSpeakingThisMessage) "Stop listening" else "Read aloud",
                            tint = if (isSpeakingThisMessage) AstraCyanLight else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Copy button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Astra Response", message.content)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Astra answer copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Bookmark button
                    IconButton(
                        onClick = onBookmarkToggle,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (message.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (message.isBookmarked) "Saved in bookmarks" else "Bookmark",
                            tint = if (message.isBookmarked) AstraIndigoLight else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "✨ Astra AI Assistant:\n\n${message.content}")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share with...")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Retry if error
            if (message.status == "ERROR" && onRetry != null) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onRetry,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
