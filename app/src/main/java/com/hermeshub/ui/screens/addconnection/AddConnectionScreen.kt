package com.hermeshub.ui.screens.addconnection

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermeshub.ui.theme.*
import com.hermeshub.viewmodel.HermesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConnectionScreen(
    viewModel: HermesViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.addConnectionState.collectAsState()
    var showKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Tambah Koneksi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetAddConnection()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBg
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HermesOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            tint = HermesOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Hubungkan ke Hermes", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Masukkan URL dan API Key", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // === Connection Name ===
            InputLabel("Nama Koneksi")
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.updateAddConnectionField("name", it) },
                placeholder = { Text("Contoh: Hermes VPS", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Label, contentDescription = null, tint = TextMuted)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // === Base URL ===
            InputLabel("Base URL")
            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = { viewModel.updateAddConnectionField("url", it) },
                placeholder = { Text("https://ip-anda:8642", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                colors = textFieldColors(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Language, contentDescription = null, tint = TextMuted)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // === API Key ===
            InputLabel("API Key")
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = { viewModel.updateAddConnectionField("apiKey", it) },
                placeholder = { Text("Masukkan API Key", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.testConnection() }
                ),
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Default.Key, contentDescription = null, tint = TextMuted)
                },
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle",
                            tint = TextMuted
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // === Test Connection Button ===
            Button(
                onClick = { viewModel.testConnection() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isTesting &&
                    state.baseUrl.isNotBlank() &&
                    state.apiKey.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurfaceVariant
                )
            ) {
                if (state.isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = HermesOrange,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Mengecek Koneksi...", color = TextSecondary)
                } else {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = HermesOrange)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Test Koneksi", color = TextPrimary, fontWeight = FontWeight.Medium)
                }
            }

            // === Test Result ===
            AnimatedVisibility(visible = state.testResult != null) {
                state.testResult?.let { result ->
                    val isSuccess = result.contains("✅")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSuccess) OnlineGreen.copy(alpha = 0.1f)
                            else androidx.compose.ui.graphics.Color(0xFF7F1D1D)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isSuccess) OnlineGreen else androidx.compose.ui.graphics.Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                result.removePrefix("✅ ").removePrefix("❌ "),
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // === Save Button ===
            Button(
                onClick = { viewModel.saveConnection() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !state.isSaving &&
                    state.name.isNotBlank() &&
                    state.baseUrl.isNotBlank() &&
                    state.apiKey.isNotBlank() &&
                    state.testResult?.contains("✅") == true,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HermesOrange
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = androidx.compose.ui.graphics.Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Menyimpan...", color = androidx.compose.ui.graphics.Color.White)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Simpan Koneksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }

            // Hint about test first
            if (state.testResult?.contains("✅") != true) {
                Text(
                    "Test koneksi dulu sebelum menyimpan",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InputLabel(text: String) {
    Text(
        text,
        color = TextPrimary,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = HermesOrange,
    unfocusedBorderColor = DarkBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = HermesOrange,
    focusedContainerColor = DarkSurface,
    unfocusedContainerColor = DarkSurface
)
