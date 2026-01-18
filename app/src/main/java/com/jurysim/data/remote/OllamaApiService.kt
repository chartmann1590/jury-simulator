package com.jurysim.data.remote

import com.jurysim.data.remote.dto.OllamaGenerateRequest
import com.jurysim.data.remote.dto.OllamaGenerateResponse
import com.jurysim.data.remote.dto.OllamaTagsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OllamaApiService {
    @GET("api/tags")
    suspend fun getTags(): Response<OllamaTagsResponse>

    @POST("api/generate")
    suspend fun generate(@Body request: OllamaGenerateRequest): Response<OllamaGenerateResponse>
}
