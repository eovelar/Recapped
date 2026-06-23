package com.recapped.app.data.remote

import com.recapped.app.data.remote.dto.GroqChatRequest
import com.recapped.app.data.remote.dto.GroqChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApi {

    @POST("chat/completions")
    suspend fun generateRecap(
        @Header("Authorization")
        authorization: String,

        @Body
        request: GroqChatRequest
    ): GroqChatResponse
}