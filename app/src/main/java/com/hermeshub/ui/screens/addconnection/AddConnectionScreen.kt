package com.hermeshub.ui.screens.addconnection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.addConnectionState.collectAsState()
    var showKey by remember { mutableStateOf(false) }

    fun textColors() = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = HermesOrange,
        unfocusedBorderColor = DarkBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = HermesOrange,
        focusedContainerColor = DarkSurface,
        unfocusedContainerColor = DarkSurface
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Koneksi", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.resetAddConnection(); onBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // === Info text ===
            Text("Masukkan data koneksi Hermes Agent kamu", color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // === Name ===
            Text("Nama Koneksi", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.updateAddConnectionField("name", it) },
                placeholder = { Text("Contoh: Hermes VPS", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textColors(),
                singleLine = true
            )

            // === URL ===
            Text("Base URL", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = { viewModel.updateAddConnectionField("url", it) },
                placeholder = { Text("http://ip-anda:8642", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                colors = textColors(),
                singleLine = true
            )

            // === API Key ===
            Text("API Key", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = { viewModel.updateAddConnectionField("apiKey", it) },
                placeholder = { Text("API Key Hermes", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textColors(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.testConnection() }),
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = TextMuted
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // === Test Button ===
            Button(
                onClick = { viewModel.testConnection() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !state.isTesting && state.baseUrl.isNotBlank() && state.apiKey.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
            ) {
                if (state.isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = HermesOrange,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mengecek...", color = TextSecondary)
                } else {
                    Text("Test Koneksi", color = TextPrimary, fontWeight = FontWeight.Medium)
                }
            }

            // === Test Result ===
            if (state.testResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val isSuccess = state.testResult!!.contains("✅")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSuccess) Color(0xFF1A3A2A)
                        else Color(0xFF3A1A1A)
                    )
                ) {
                    Text(
                        state.testResult!!,
                        modifier = Modifier.padding(14.dp),
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === Save Button ===
            Button(
                onClick = { viewModel.saveConnection() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isSaving && state.name.isNotBlank() &&
                    state.baseUrl.isNotBlank() && state.apiKey.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HermesOrange)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Menyimpan...", color = Color.White)
                } else {
                    Text("Simpan Koneksi", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
