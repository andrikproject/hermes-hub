package com.hermeshub.ui.screens.addconnection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermeshub.ui.theme.*
import com.hermeshub.viewmodel.HermesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConnectionScreen(
    viewModel: HermesViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.addConnectionState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Koneksi", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Help text
            Text(
                "Masukkan URL dan API Key dari Hermes Agent milikmu",
                fontSize = 14.sp,
                color = TextSecondary
            )

            // Connection Name
            Text("Nama Koneksi", color = TextPrimary, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.updateAddConnectionField("name", it) },
                placeholder = { Text("Contoh: Hermes VPS Andrik", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HermesOrange,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = HermesOrange
                ),
                singleLine = true
            )

            // Base URL
            Text("Base URL (Hermes API Server)", color = TextPrimary, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = { viewModel.updateAddConnectionField("url", it) },
                placeholder = { Text("https://ip-anda:8642", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HermesOrange,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = HermesOrange
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        tint = TextMuted
                    )
                }
            )

            // API Key
            Text("API Key", color = TextPrimary, fontWeight = FontWeight.Medium)
            var showKey by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = { viewModel.updateAddConnectionField("apiKey", it) },
                placeholder = { Text("Masukkan API key Hermes", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HermesOrange,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = HermesOrange
                ),
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showKey) "Sembunyikan" else "Tampilkan",
                            tint = TextMuted
                        )
                    }
                }
            )

            // Test connection button
            Button(
                onClick = { viewModel.testConnection() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isTesting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                if (state.isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = HermesOrange,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = HermesOrange)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (state.isTesting) "Mengecek..." else "Test Koneksi",
                    color = TextPrimary
                )
            }

            // Test result
            state.testResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.contains("✅")) DarkSurfaceVariant else DarkSurface.copy(
                            red = 0.2f
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(result, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    viewModel.saveConnection()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !state.isSaving && state.name.isNotBlank() && state.baseUrl.isNotBlank() && state.apiKey.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HermesOrange
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (state.isSaving) "Menyimpan..." else "Simpan Koneksi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
