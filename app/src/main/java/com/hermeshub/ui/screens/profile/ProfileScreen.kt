package com.hermeshub.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermeshub.ui.theme.*
import com.hermeshub.viewmodel.HermesViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    viewModel: HermesViewModel? = null
) {
    val isDarkMode by viewModel?.isDarkMode?.collectAsState() ?: remember { mutableStateOf(true) }

    // Live clock
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale("id", "ID"))
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Live Clock Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Jam besar
                Text(
                    timeFormat.format(Date(currentTime)),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    dateFormat.format(Date(currentTime)),
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // App Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(HermesOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Terminal,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Hermes Hub", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("v1.0.0", fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Akses Hermes Agent dari mana aja",
                    fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Theme Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) HermesOrange.copy(alpha = 0.15f) else Color(0xFFFBBF24).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = if (isDarkMode) HermesOrange else Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tampilan", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(
                        if (isDarkMode) "Mode Gelap" else "Mode Terang",
                        fontSize = 12.sp, color = TextSecondary
                    )
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { viewModel?.toggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HermesOrange,
                        checkedTrackColor = HermesOrange.copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cara Pakai
        InfoCard(
            icon = Icons.Default.Info,
            title = "Cara Pakai",
            description = "1. Tambah koneksi Hermes Agent\n2. Test koneksi dulu\n3. Simpan & mulai ngobrol",
            color = HermesOrange
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoCard(
            icon = Icons.Default.Security,
            title = "Keamanan",
            description = "API Key disimpan aman di HP.\nChat cuma di perangkat ini.\nHermes jalan di server masing-masing.",
            color = OnlineGreen
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoCard(
            icon = Icons.Default.Code,
            title = "Open Source",
            description = "Kotlin + Jetpack Compose\nRepo: github.com/andrikproject/hermes-hub",
            color = Color(0xFF818CF8)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Dibuat oleh Andrik Rizki Rohmadani",
            fontSize = 12.sp, color = TextMuted,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}
