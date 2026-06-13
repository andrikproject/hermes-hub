package com.hermeshub.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hermeshub.data.local.HermesDatabase
import com.hermeshub.data.model.ChatMessage
import com.hermeshub.data.model.HermesConnection
import com.hermeshub.data.model.Message
import com.hermeshub.data.repository.HermesRepository
import com.hermeshub.util.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ConnectionUiState(
    val connections: List<HermesConnection> = emptyList(),
    val isLoading: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val streamedContent: String = ""
)

data class AddConnectionUiState(
    val name: String = "",
    val baseUrl: String = "",
    val apiKey: String = "",
    val connectionType: String = "api",
    val isTesting: Boolean = false,
    val testResult: String? = null,
    val isSaving: Boolean = false
)

class HermesViewModel(application: Application) : AndroidViewModel(application) {
    private val db = HermesDatabase.getDatabase(application)
    private val repository = HermesRepository(
        connectionDao = db.connectionDao(),
        messageDao = db.messageDao()
    )

    // Theme state
    private val prefs = application.getSharedPreferences("hermes_hub_prefs", Context.MODE_PRIVATE)
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Connection state
    private val _connectionState = MutableStateFlow(ConnectionUiState())
    val connectionState: StateFlow<ConnectionUiState> = _connectionState.asStateFlow()

    // Chat state
    private val _chatState = MutableStateFlow(ChatUiState())
    val chatState: StateFlow<ChatUiState> = _chatState.asStateFlow()

    // Add connection state
    private val _addConnectionState = MutableStateFlow(AddConnectionUiState())
    val addConnectionState: StateFlow<AddConnectionUiState> = _addConnectionState.asStateFlow()

    // Selected connection
    private val _selectedConnection = MutableStateFlow<HermesConnection?>(null)
    val selectedConnection: StateFlow<HermesConnection?> = _selectedConnection.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllConnections().collect { connections ->
                _connectionState.value = _connectionState.value.copy(
                    connections = connections,
                    isLoading = false
                )
            }
        }
    }

    // ========== ADD CONNECTION ==========

    fun updateAddConnectionField(field: String, value: String) {
        _addConnectionState.value = when (field) {
            "name" -> _addConnectionState.value.copy(name = value)
            "url" -> _addConnectionState.value.copy(baseUrl = value)
            "apiKey" -> _addConnectionState.value.copy(apiKey = value)
            else -> _addConnectionState.value
        }
    }

    fun testConnection() {
        val state = _addConnectionState.value
        if (state.baseUrl.isBlank() || state.apiKey.isBlank()) {
            _addConnectionState.value = state.copy(testResult = "URL dan API Key harus diisi!")
            return
        }

        viewModelScope.launch {
            _addConnectionState.value = _addConnectionState.value.copy(isTesting = true, testResult = null)
            val result = repository.checkConnection(state.baseUrl, state.apiKey)
            _addConnectionState.value = _addConnectionState.value.copy(
                isTesting = false,
                testResult = if (result.isSuccess) "✅ Koneksi berhasil!" else "❌ Gagal: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    fun saveConnection() {
        val state = _addConnectionState.value
        if (state.name.isBlank() || state.baseUrl.isBlank() || state.apiKey.isBlank()) {
            _addConnectionState.value = state.copy(testResult = "Semua field harus diisi!")
            return
        }

        viewModelScope.launch {
            _addConnectionState.value = _addConnectionState.value.copy(isSaving = true)
            val connection = HermesConnection(
                name = state.name,
                baseUrl = state.baseUrl.trimEnd('/'),
                apiKey = state.apiKey,
                connectionType = "api"
            )
            repository.saveConnection(connection)
            resetAddConnection()
        }
    }

    fun resetAddConnection() {
        _addConnectionState.value = AddConnectionUiState()
    }

    // ========== CONNECTIONS ==========

    fun selectConnection(connection: HermesConnection) {
        _selectedConnection.value = connection
        viewModelScope.launch {
            repository.getMessages(connection.id).collect { messages ->
                _chatState.value = _chatState.value.copy(messages = messages)
            }
        }
    }

    fun deleteConnection(connection: HermesConnection) {
        viewModelScope.launch {
            repository.deleteConnection(connection)
            if (_selectedConnection.value?.id == connection.id) {
                _selectedConnection.value = null
                _chatState.value = ChatUiState()
            }
        }
    }

    // ========== CHAT ==========

    fun sendMessage(text: String) {
        val connection = _selectedConnection.value ?: return
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            connectionId = connection.id,
            role = "user",
            content = text
        )

        viewModelScope.launch {
            // Save user message
            repository.saveMessage(userMessage)

            // Set loading
            _chatState.value = _chatState.value.copy(isLoading = true)

            // Build history from existing messages
            val history = _chatState.value.messages.map { msg ->
                Message(role = msg.role, content = msg.content)
            }

            // Send to API
            val result = repository.sendMessage(connection, text, history)

            result.onSuccess { response ->
                val assistantMessage = ChatMessage(
                    connectionId = connection.id,
                    role = "assistant",
                    content = response
                )
                repository.saveMessage(assistantMessage)
                _chatState.value = _chatState.value.copy(isLoading = false)
            }.onFailure { error ->
                val errorMessage = ChatMessage(
                    connectionId = connection.id,
                    role = "assistant",
                    content = "❌ Error: ${error.message}"
                )
                repository.saveMessage(errorMessage)
                _chatState.value = _chatState.value.copy(isLoading = false)
            }
        }
    }

    fun sendMessageStreaming(text: String) {
        val connection = _selectedConnection.value ?: return
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            connectionId = connection.id,
            role = "user",
            content = text
        )

        viewModelScope.launch {
            repository.saveMessage(userMessage)

            val history = _chatState.value.messages.map { msg ->
                Message(role = msg.role, content = msg.content)
            }

            _chatState.value = _chatState.value.copy(
                isLoading = true,
                isStreaming = true,
                streamedContent = ""
            )

            repository.streamChat(
                connection = connection,
                userMessage = text,
                history = history,
                onChunk = { chunk ->
                    _chatState.value = _chatState.value.copy(
                        streamedContent = _chatState.value.streamedContent + chunk
                    )
                },
                onComplete = { fullContent ->
                    viewModelScope.launch {
                        val assistantMessage = ChatMessage(
                            connectionId = connection.id,
                            role = "assistant",
                            content = fullContent
                        )
                        repository.saveMessage(assistantMessage)
                        _chatState.value = _chatState.value.copy(
                            isLoading = false,
                            isStreaming = false,
                            streamedContent = ""
                        )
                        // Notifikasi
                        NotificationHelper.showResponseNotification(
                            getApplication(),
                            connection.name,
                            fullContent
                        )
                    }
                },
                onError = { error ->
                    viewModelScope.launch {
                        val errorMessage = ChatMessage(
                            connectionId = connection.id,
                            role = "assistant",
                            content = "❌ Error: ${error.message}"
                        )
                        repository.saveMessage(errorMessage)
                        _chatState.value = _chatState.value.copy(
                            isLoading = false,
                            isStreaming = false,
                            streamedContent = ""
                        )
                        NotificationHelper.showErrorNotification(
                            getApplication(),
                            connection.name,
                            error.message ?: "Unknown error"
                        )
                    }
                }
            )
        }
    }

    fun clearChat() {
        val connection = _selectedConnection.value ?: return
        viewModelScope.launch {
            repository.clearMessages(connection.id)
        }
    }

    // ========== THEME ==========

    fun toggleTheme() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        prefs.edit().putBoolean("dark_mode", newValue).apply()
    }
}
