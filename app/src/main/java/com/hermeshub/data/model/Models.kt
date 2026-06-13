package com.hermeshub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connections")
data class HermesConnection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val baseUrl: String,          // e.g. https://192.168.1.100:8642
    val apiKey: String,
    val connectionType: String,   // "api" (REST) or "ssh"
    val sshHost: String = "",
    val sshPort: Int = 22,
    val sshUsername: String = "",
    val sshPassword: String = "",
    val isOnline: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val connectionId: Long,
    val role: String,             // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatCompletionRequest(
    val model: String = "hermes-agent",
    val messages: List<Message>,
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage?
)

data class Choice(
    val index: Int,
    val message: Message?
)

data class Usage(
    val prompt_tokens: Int?,
    val completion_tokens: Int?,
    val total_tokens: Int?
)
