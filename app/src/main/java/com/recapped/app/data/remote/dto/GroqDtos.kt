package com.recapped.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GroqChatRequest(
    @Json(name = "model")
    val model: String = "llama-3.1-8b-instant",

    @Json(name = "messages")
    val messages: List<GroqMessageDto>,

    @Json(name = "temperature")
    val temperature: Double = 0.7,

    @Json(name = "max_completion_tokens")
    val maxCompletionTokens: Int = 700
)

@JsonClass(generateAdapter = true)
data class GroqMessageDto(
    @Json(name = "role")
    val role: String,

    @Json(name = "content")
    val content: String
)

@JsonClass(generateAdapter = true)
data class GroqChatResponse(
    @Json(name = "choices")
    val choices: List<GroqChoiceDto>
)

@JsonClass(generateAdapter = true)
data class GroqChoiceDto(
    @Json(name = "message")
    val message: GroqMessageDto
)