package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.ui.components.AstraAvatar
import com.example.ui.components.ChatInputBar
import com.example.ui.components.MessageItem
import com.example.ui.theme.AstraCyanLight
import com.example.ui.theme.AstraIndigoLight
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AstraViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Stateful ChatScreen that collects state from [AstraViewModel]
 * and handles history bottom-sheet interactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: AstraViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val speakingMessageId by viewModel.speakingMessageId.collectAsState()
    val currentConv by viewModel.currentConversation.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val quickSuggestions by viewModel.quickSuggestions.collectAsState()
    val isHistoryOpen by viewModel.isHistoryDrawerOpen.collectAsState()

    // BackHandler closes history drawer if open
    BackHandler(enabled = isHistoryOpen) {
        viewModel.setHistoryDrawerOpen(false)
    }

    Box(modifier = modifier.fillMaxSize()) {
        ChatScreenContent(
            messages = messages,
            isGenerating = isGenerating,
            speakingMessageId = speakingMessageId,
            conversationTitle = currentConv?.title ?: "Astra Assistant",
            quickSuggestions = if (messages.size <= 2) quickSuggestions else emptyList(),
            onSendMessage = { prompt -> viewModel.sendMessage(prompt) },
            onStopGeneration = { viewModel.stopGeneration() },
            onOpenHistory = { viewModel.setHistoryDrawerOpen(true) },
            onNewChat = { viewModel.createNewConversation() },
            onSpeakToggle = { message -> viewModel.toggleSpeakMessage(message) },
            onBookmarkToggle = { message -> viewModel.toggleBookmark(message) },
            onRetry = { message -> viewModel.sendMessage(message.content) },
            modifier = Modifier.fillMaxSize()
        )

        // History Bottom Sheet
        if (isHistoryOpen) {
            ConversationHistorySheet(
                conversations = conversations,
                selectedConversationId = currentConv?.id,
                onDismiss = { viewModel.setHistoryDrawerOpen(false) },
                onSelectConversation = { id -> viewModel.selectConversation(id) },
                onNewChat = { viewModel.createNewConversation() },
                onDeleteConversation = { id -> viewModel.deleteConversation(id) }
            )
        }
    }
}

/**
 * Stateless ChatScreenContent designed for an AI assistant interface.
 * Uses a LazyColumn to display a list of messages and a TextField input bar with send button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    messages: List<MessageEntity>,
    isGenerating: Boolean,
    speakingMessageId: Long?,
    conversationTitle: String,
    quickSuggestions: List<String>,
    onSendMessage: (String) -> Unit,
    onStopGeneration: () -> Unit,
    onOpenHistory: () -> Unit,
    onNewChat: () -> Unit,
    onSpeakToggle: (MessageEntity) -> Unit,
    onBookmarkToggle: (MessageEntity) -> Unit,
    onRetry: (MessageEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to latest message when new message arrives or generation starts
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenHistory() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        AstraAvatar(size = 36.dp, isThinking = isGenerating)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = conversationTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isGenerating) AstraCyanLight else Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isGenerating) "Astra is reasoning..." else "Astra Ready",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isGenerating) AstraCyanLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("history_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Chat history",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNewChat,
                        modifier = Modifier.testTag("new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddComment,
                            contentDescription = "New chat",
                            tint = AstraIndigoLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                inputText = inputText,
                onInputTextChange = { inputText = it },
                onSendMessage = { prompt ->
                    inputText = ""
                    onSendMessage(prompt)
                },
                isGenerating = isGenerating,
                onStopGeneration = onStopGeneration,
                quickSuggestions = quickSuggestions,
                onSuggestionSelected = { suggestion ->
                    val cleanPrompt = suggestion.replace(Regex("^[^\u0000-\u007F]+\\s*"), "")
                    inputText = ""
                    onSendMessage(cleanPrompt)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (messages.isEmpty() && !isGenerating) {
                // Empty state greeting
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AstraAvatar(size = 72.dp, isThinking = false)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Astra AI",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            brush = Brush.horizontalGradient(listOf(AstraIndigoLight, AstraCyanLight))
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your all-in-one personal guide for daily solutions, code, study & ideas.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                // Main LazyColumn for chat messages
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp)
                        .testTag("chat_messages_list"),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageItem(
                            message = message,
                            isSpeakingThisMessage = speakingMessageId == message.id,
                            onSpeakToggle = { onSpeakToggle(message) },
                            onBookmarkToggle = { onBookmarkToggle(message) },
                            onRetry = { onRetry(message) }
                        )
                    }

                    if (isGenerating) {
                        item(key = "generating_indicator") {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AstraAvatar(size = 28.dp, isThinking = true)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Astra is formulating the best step-by-step answer...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AstraCyanLight,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Slide-up BottomSheet to display, switch, and delete past conversations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationHistorySheet(
    conversations: List<ConversationEntity>,
    selectedConversationId: Long?,
    onDismiss: () -> Unit,
    onSelectConversation: (Long) -> Unit,
    onNewChat: () -> Unit,
    onDeleteConversation: (Long) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chat History",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = onNewChat,
                    colors = ButtonDefaults.buttonColors(containerColor = AstraIndigoLight),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Chat", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (conversations.isEmpty()) {
                Text(
                    text = "No past conversations yet.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(conversations, key = { it.id }) { conv ->
                        val isSelected = selectedConversationId == conv.id
                        val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(conv.updatedAt))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectConversation(conv.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = null,
                                        tint = if (isSelected) AstraIndigoLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = conv.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDeleteConversation(conv.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete chat",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MyApplicationTheme {
        ChatScreenContent(
            messages = listOf(
                MessageEntity(
                    id = 1,
                    conversationId = 1,
                    sender = "ASTRA",
                    content = "Namaste! I am Astra. How can I help you today?",
                    status = "SUCCESS"
                ),
                MessageEntity(
                    id = 2,
                    conversationId = 1,
                    sender = "USER",
                    content = "Help me plan a 30-minute morning routine.",
                    status = "SUCCESS"
                ),
                MessageEntity(
                    id = 3,
                    conversationId = 1,
                    sender = "ASTRA",
                    content = "### 🌅 30-Minute Morning Routine\n\n1. **Hydration (5m):** Drink 500ml water.\n2. **Mobility (10m):** Light stretches.\n3. **Mind Priming (15m):** Write top priorities.",
                    status = "SUCCESS"
                )
            ),
            isGenerating = false,
            speakingMessageId = null,
            conversationTitle = "Morning Routine",
            quickSuggestions = listOf("Python script", "50/30/20 budget"),
            onSendMessage = {},
            onStopGeneration = {},
            onOpenHistory = {},
            onNewChat = {},
            onSpeakToggle = {},
            onBookmarkToggle = {},
            onRetry = {}
        )
    }
}
