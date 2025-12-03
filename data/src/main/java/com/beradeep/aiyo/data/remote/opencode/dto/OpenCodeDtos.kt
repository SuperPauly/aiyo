package com.beradeep.aiyo.data.remote.opencode.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionRequest(
    val title: String? = null
)

@Serializable
data class SessionResponse(
    val id: String,
    val title: String?
)

@Serializable
data class SendMessageRequest(
    val content: String
)

@Serializable
data class OpenCodeEvent(
    val type: String, // e.g., "delta", "status" - waiting for confirmation on exact values, assuming robust defaults for now
    val data: String? = null
)
