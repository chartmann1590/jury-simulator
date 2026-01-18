package com.jurysim.data.repository

import com.jurysim.data.model.OllamaModel
import com.jurysim.data.remote.OllamaApiService
import com.jurysim.data.remote.dto.OllamaGenerateRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class OllamaRepository {
    private var apiService: OllamaApiService? = null
    private var currentBaseUrl: String? = null

    fun updateBaseUrl(baseUrl: String) {
        val normalizedUrl = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl

        if (normalizedUrl != currentBaseUrl) {
            currentBaseUrl = normalizedUrl

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            apiService = Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OllamaApiService::class.java)
        }
    }

    suspend fun getModels(): Result<List<OllamaModel>> {
        return try {
            val service = apiService ?: return Result.failure(
                Exception("API service not initialized. Please set server URL first.")
            )

            val response = service.getTags()
            if (response.isSuccessful) {
                val models = response.body()?.models?.map {
                    OllamaModel(
                        name = it.name,
                        modifiedAt = it.modifiedAt,
                        size = it.size,
                        digest = it.digest
                    )
                } ?: emptyList()
                Result.success(models)
            } else {
                Result.failure(Exception("Failed to fetch models: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generate(model: String, prompt: String): Result<String> {
        return try {
            val service = apiService ?: return Result.failure(
                Exception("API service not initialized. Please set server URL first.")
            )

            val request = OllamaGenerateRequest(
                model = model,
                prompt = prompt,
                stream = false
            )

            val response = service.generate(request)
            if (response.isSuccessful) {
                val generatedText = response.body()?.response ?: ""
                Result.success(generatedText)
            } else {
                Result.failure(Exception("Failed to generate response: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(): Result<Boolean> {
        return try {
            val service = apiService ?: return Result.failure(
                Exception("API service not initialized. Please set server URL first.")
            )

            val response = service.getTags()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Connection failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
