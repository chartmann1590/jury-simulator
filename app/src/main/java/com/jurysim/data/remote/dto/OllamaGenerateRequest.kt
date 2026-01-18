package com.jurysim.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OllamaGenerateRequest(
    @Json(name = "model")
    val model: String,
    @Json(name = "prompt")
    val prompt: String,
    @Json(name = "stream")
    val stream: Boolean = false,
    @Json(name = "options")
    val options: Map<String, Any>? = null
)
