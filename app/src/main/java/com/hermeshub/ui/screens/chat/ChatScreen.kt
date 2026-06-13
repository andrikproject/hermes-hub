package com.hermeshub.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontFamily
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
    onBack: () -> Unit
) {
    val chatState by viewModel.chatState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val connection by viewModel.selectedConnection.collectAsState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(chatState.messages.size, chatState.streamedContent) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size)
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
                        var showClearDialog by remember { mutableStateOf(false) }
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Hapus Chat",
                                tint = TextSecondary
                            )
                        }
                        if (showClearDialog) {
                            AlertDialog(
                                onDismissRequest = { showClearDialog = false },
                                containerColor = DarkSurface,
                                title = { Text("Hapus Chat?", color = TextPrimary) },
                                text = { Text("Semua pesan di sesi ini akan dihapus.", color = TextSecondary) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showClearDialog = false
                                        viewModel.clearChat()
                                    }) {
                                        Text("Hapus", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showClearDialog = false }) {
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
            // Input bar
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
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Ketik pesan ke Hermes...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
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
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank() && !chatState.isLoading) {
                                    viewModel.sendMessageStreaming(inputText)
                                    inputText = ""
                                }
                            }
                        ),
                        maxLines = 4,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !chatState.isLoading) {
                                viewModel.sendMessageStreaming(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (inputText.isNotBlank()) HermesOrange else DarkSurfaceVariant
                        ),
                        enabled = inputText.isNotBlank() && !chatState.isLoading
                    ) {
                        if (chatState.isLoading) {
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
    ) { paddingValues ->
        if (chatState.messages.isEmpty() && !chatState.isLoading) {
            // Empty chat
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = DarkBorder
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Mulai percakapan dengan",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                    Text(
                        connectionName,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Pesan akan dikirim langsung ke Hermes Agent",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chatState.messages, key = { it.id }) { message ->
                    MessageBubble(message)
                }

                // Streaming message in progress
                if (chatState.isStreaming && chatState.streamedContent.isNotBlank()) {
                    item {
                        StreamingBubble(chatState.streamedContent)
                    }
                }

                // Loading indicator (before streaming starts)
                if (chatState.isLoading && !chatState.isStreaming) {
                    item {
                        LoadingBubble()
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) UserBubble else AssistantBubble,
            shadowElevation = 1.dp
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (isUser) "Kamu" else "Hermes",
            fontSize = 10.sp,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun StreamingBubble(content: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = AssistantBubble,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = content,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                // Blinking cursor
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(HermesOrange)
                )
            }
        }
        Text(
            text = "Hermes",
            fontSize = 10.sp,
            color = HermesOrange,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun LoadingBubble() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = AssistantBubble,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Animated dots
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(TextMuted)
                    )
                }
            }
        }
    }
}
