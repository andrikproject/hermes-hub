package com.hermeshub.data.repository

import com.hermeshub.data.local.ConnectionDao
import com.hermeshub.data.local.MessageDao
import com.hermeshub.data.model.ChatMessage
import com.hermeshub.data.model.HermesConnection
import com.hermeshub.data.model.Message
import com.hermeshub.data.remote.HermesApiService
import com.hermeshub.data.model.ChatCompletionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HermesRepository(
    private val connectionDao: ConnectionDao,
    private val messageDao: MessageDao
) {
    // ========== CONNECTIONS ==========

    fun getAllConnections(): Flow<List<HermesConnection>> =
        connectionDao.getAllConnections()

    suspend fun getConnectionById(id: Long): HermesConnection? =
        connectionDao.getConnectionById(id)

    suspend fun saveConnection(connection: HermesConnection): Long =
        connectionDao.insertConnection(connection)

    suspend fun updateConnection(connection: HermesConnection) =
        connectionDao.updateConnection(connection)

    suspend fun deleteConnection(connection: HermesConnection) =
        connectionDao.deleteConnection(connection)

    suspend fun checkConnection(baseUrl: String, apiKey: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val service = HermesApiService.create(baseUrl, apiKey)
                val response = service.getModels()
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Gagal: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ========== MESSAGES ==========

    fun getMessages(connectionId: Long): Flow<List<ChatMessage>> =
        messageDao.getMessagesForConnection(connectionId)

    suspend fun saveMessage(message: ChatMessage): Long =
        messageDao.insertMessage(message)

    suspend fun clearMessages(connectionId: Long) =
        messageDao.deleteMessagesForConnection(connectionId)

    // ========== CHAT COMPLETION ==========

    suspend fun sendMessage(
        connection: HermesConnection,
        userMessage: String,
        history: List<Message>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = HermesApiService.create(connection.baseUrl, connection.apiKey)
            val messages = history + Message(role = "user", content = userMessage)
            val request = ChatCompletionRequest(
                model = "hermes-agent",
                messages = messages,
                stream = false
            )
            val response = service.sendChat(request)
            if (response.isSuccessful) {
                val body = response.body()
                val content = body?.choices?.firstOrNull()?.message?.content ?: ""
                Result.success(content)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== STREAMING CHAT (SSE) ==========

    fun streamChat(
        connection: HermesConnection,
        userMessage: String,
        history: List<Message>,
        onChunk: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val messages = history + Message(role = "user", content = userMessage)
        val requestBody = JSONObject().apply {
            put("model", "hermes-agent")
            put("stream", true)
            val messagesArray = org.json.JSONArray()
            for (msg in messages) {
                val msgObj = JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                }
                messagesArray.put(msgObj)
            }
            put("messages", messagesArray)
        }

        val jsonMediaType = "application/json".toMediaType()

        val request = Request.Builder()
            .url("${connection.baseUrl.trimEnd('/')}/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${connection.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(okhttp3.RequestBody.create(jsonMediaType, requestBody.toString()))
            .build()

        val buffer = StringBuilder()

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (data == "[DONE]") {
                    onComplete(buffer.toString())
                    return
                }
                try {
                    val json = JSONObject(data)
                    val choices = json.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val delta = choices.getJSONObject(0).optJSONObject("delta")
                        val content = delta?.optString("content", "") ?: ""
                        if (content.isNotEmpty()) {
                            buffer.append(content)
                            onChunk(content)
                        }
                    }
                } catch (e: Exception) {
                    // Skip parse errors for incomplete chunks
                }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: okhttp3.Response?
            ) {
                if (buffer.isNotEmpty()) {
                    onComplete(buffer.toString())
                } else {
                    onError(Exception(t?.message ?: "Koneksi gagal", t))
                }
            }

            override fun onClosed(eventSource: EventSource) {
                if (buffer.isNotEmpty()) {
                    onComplete(buffer.toString())
                }
            }
        }

        val factory = EventSources.createFactory(client)
        factory.newEventSource(request, listener)
    }
}
