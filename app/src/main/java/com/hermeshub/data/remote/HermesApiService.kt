package com.hermeshub.data.remote

import com.hermeshub.data.model.ChatCompletionRequest
import com.hermeshub.data.model.ChatCompletionResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface HermesApiService {
    @POST("v1/chat/completions")
    suspend fun sendChat(
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>

    @GET("v1/models")
    suspend fun getModels(): Response<Map<String, Any>>

    companion object {
        fun create(baseUrl: String, apiKey: String): HermesApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $apiKey")
                        .addHeader("Content-Type", "application/json")
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

            return Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(HermesApiService::class.java)
        }
    }
}
