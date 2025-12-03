package com.beradeep.aiyo.domain.repository

import com.beradeep.aiyo.domain.model.SshConfig
import kotlinx.coroutines.flow.Flow

interface RemoteAgentSession {
    val events: Flow<RemoteAgentEvent>

    suspend fun connect(config: SshConfig)
    suspend fun disconnect()
    suspend fun sendUserMessage(text: String, history: List<Any> = emptyList()) // History type can be refined
}

sealed class RemoteAgentEvent {
    data class OutputChunk(val text: String) : RemoteAgentEvent()
    data class Status(val state: String) : RemoteAgentEvent() // e.g., "Connecting...", "Tool: grep"
    data class Error(val throwable: Throwable) : RemoteAgentEvent()
}
