package com.hermeshub.ui.screens.connectionlist

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermeshub.data.model.HermesConnection
import com.hermeshub.ui.theme.*
import com.hermeshub.viewmodel.HermesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionListScreen(
    viewModel: HermesViewModel,
    onAddConnection: () -> Unit,
    onOpenChat: (HermesConnection) -> Unit
) {
    val state by viewModel.connectionState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Hermes Hub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Koneksi Hermes Agent kamu",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddConnection) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tambah Koneksi",
                            tint = HermesOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        if (state.connections.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Cable,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Belum ada koneksi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tambahkan koneksi Hermes Agent-mu",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onAddConnection,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HermesOrange
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Koneksi")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.connections, key = { it.id }) { connection ->
                    ConnectionCard(
                        connection = connection,
                        onClick = { onOpenChat(connection) },
                        onDelete = { viewModel.deleteConnection(connection) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionCard(
    connection: HermesConnection,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (connection.isOnline) OnlineGreen else OfflineGray
                    )
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = HermesOrangeLight,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connection.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = connection.baseUrl,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Delete button
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Hapus",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Arrow
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Buka",
                tint = TextMuted
            )
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Hapus Koneksi", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Yakin mau hapus koneksi \"${connection.name}\"? Riwayat chat juga akan ikut terhapus.", color = TextSecondary)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Batal")
                }
            }
        )
    }
}
