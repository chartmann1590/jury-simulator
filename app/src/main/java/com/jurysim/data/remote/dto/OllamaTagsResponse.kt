package com.jurysim.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OllamaTagsResponse(
    @Json(name = "models")
    val models: List<ModelInfo>
)

@JsonClass(generateAdapter = true)
data class ModelInfo(
    @Json(name = "name")
    val name: String,
    @Json(name = "modified_at")
    val modifiedAt: String? = null,
    @Json(name = "size")
    val size: Long? = null,
    @Json(name = "digest")
    val digest: String? = null
)
