package com.jurysim.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OllamaGenerateResponse(
    @Json(name = "model")
    val model: String,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "response")
    val response: String,
    @Json(name = "done")
    val done: Boolean,
    @Json(name = "context")
    val context: List<Int>? = null,
    @Json(name = "total_duration")
    val totalDuration: Long? = null,
    @Json(name = "load_duration")
    val loadDuration: Long? = null,
    @Json(name = "prompt_eval_count")
    val promptEvalCount: Int? = null,
    @Json(name = "eval_count")
    val evalCount: Int? = null
)
