package com.beradeep.aiyo.data.remote.opencode

import com.beradeep.aiyo.data.remote.opencode.dto.CreateSessionRequest
import com.beradeep.aiyo.data.remote.opencode.dto.SendMessageRequest
import com.beradeep.aiyo.data.remote.opencode.dto.SessionResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface OpenCodeApi {
    @POST("session")
    suspend fun createSession(@Body request: CreateSessionRequest): SessionResponse

    @POST("session/{id}/message")
    suspend fun sendMessage(@Path("id") sessionId: String, @Body request: SendMessageRequest)
}
