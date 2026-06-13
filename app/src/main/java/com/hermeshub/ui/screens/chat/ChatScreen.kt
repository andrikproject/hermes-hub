package com.hermeshub.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermeshub.data.model.ChatMessage
import com.hermeshub.ui.theme.*
import com.hermeshub.viewmodel.HermesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: HermesViewModel,
    connectionName: String,
    connectionId: Long,
    onBack: () -> Unit
) {
    val chatState by viewModel.chatState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll
    LaunchedEffect(chatState.messages.size, chatState.streamedContent) {
        if (chatState.messages.isNotEmpty() || chatState.streamedContent.isNotEmpty()) {
            listState.animateScrollToItem(Int.MAX_VALUE)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            connectionName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (chatState.isStreaming) {
                            Text(
                                "Sedang mengetik...",
                                fontSize = 11.sp,
                                color = HermesOrange
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (chatState.messages.isNotEmpty()) {
                        var showClear by remember { mutableStateOf(false) }
                        IconButton(onClick = { showClear = true }) {
                            Icon(Icons.Default.DeleteSweep, "Hapus", tint = TextSecondary)
                        }
                        if (showClear) {
                            AlertDialog(
                                onDismissRequest = { showClear = false },
                                containerColor = DarkSurface,
                                title = { Text("Hapus Chat?", color = TextPrimary) },
                                text = { Text("Semua pesan akan dihapus.", color = TextSecondary) },
                                confirmButton = {
                                    TextButton(onClick = { showClear = false; viewModel.clearChat() }) {
                                        Text("Hapus", color = Color(0xFFEF4444))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showClear = false }) {
                                        Text("Batal", color = TextSecondary)
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = DarkBackground,
        bottomBar = {
            ChatInputBar(
                inputText = inputText,
                onInputChange = { inputText = it },
                isLoading = chatState.isLoading,
                onSend = {
                    if (inputText.isNotBlank() && !chatState.isLoading) {
                        viewModel.sendMessageStreaming(inputText)
                        inputText = ""
                    }
                }
            )
        }
    ) { paddingValues ->
        if (chatState.messages.isEmpty() && !chatState.isStreaming) {
            EmptyChat(connectionName)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp),
                state = listState,
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(chatState.messages, key = { it.id }) { msg ->
                    MessageBubble(msg)
                }

                // Streaming
                if (chatState.isStreaming && chatState.streamedContent.isNotBlank()) {
                    item { StreamingBubble(chatState.streamedContent) }
                }
                if (chatState.isLoading && !chatState.isStreaming) {
                    item { TypingIndicator() }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Label
        Text(
            if (isUser) "Kamu" else "Hermes",
            fontSize = 10.sp,
            color = if (isUser) UserBubble else HermesOrange,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )

        // Bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = if (isUser) 18.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            ),
            color = if (isUser) UserBubble else AssistantBubble,
            shadowElevation = 1.dp
        ) {
            Text(
                msg.content,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun StreamingBubble(content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "Hermes",
            fontSize = 10.sp,
            color = HermesOrange,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
            color = AssistantBubble,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(content, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(HermesOrange)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Hermes", fontSize = 10.sp, color = HermesOrange, modifier = Modifier.padding(horizontal = 6.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
            color = AssistantBubble,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(TextMuted)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyChat(connectionName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(HermesOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(36.dp), tint = HermesOrange)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("Mulai ngobrol dengan", color = TextSecondary, fontSize = 15.sp)
            Text(connectionName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ketik pesan di bawah", color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkBackground,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ketik pesan...", color = TextMuted, fontSize = 14.sp) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HermesOrange,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = HermesOrange,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 4,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconButton(
                onClick = onSend,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (inputText.isNotBlank() && !isLoading) HermesOrange else DarkSurfaceVariant
                ),
                enabled = inputText.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Kirim",
                        tint = if (inputText.isNotBlank()) Color.White else TextMuted
                    )
                }
            }
        }
    }
}
