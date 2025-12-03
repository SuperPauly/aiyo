package com.beradeep.aiyo.data.remote.opencode

import android.util.Log
import com.beradeep.aiyo.data.remote.opencode.dto.CreateSessionRequest
import com.beradeep.aiyo.data.remote.opencode.dto.SendMessageRequest
import com.beradeep.aiyo.data.remote.ssh.SshTunnelManager
import com.beradeep.aiyo.domain.model.SshConfig
import com.beradeep.aiyo.domain.repository.RemoteAgentEvent
import com.beradeep.aiyo.domain.repository.RemoteAgentSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenCodeRepository @Inject constructor(
    private val sshTunnelManager: SshTunnelManager
) : RemoteAgentSession {

    companion object {
        /** Default port for the OpenCode agent service */
        const val OPENCODE_DEFAULT_PORT = 4096
    }

    private var api: OpenCodeApi? = null
    private var okHttpClient: OkHttpClient? = null
    private var currentSessionId: String? = null
    private var baseUrl: String? = null

    override val events: Flow<RemoteAgentEvent> = callbackFlow {
        val client = okHttpClient ?: throw IllegalStateException("Not connected")
        val url = baseUrl ?: throw IllegalStateException("No base URL")

        val request = Request.Builder()
            .url("${url}event")
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                trySend(RemoteAgentEvent.Status("Connected to OpenCode Agent"))
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                // TODO: Parse exact OpenCode event structure (JSON).
                // For MVP, we handle 'delta' for text and log others.
                when (type) {
                    "delta" -> trySend(RemoteAgentEvent.OutputChunk(data))
                    "tool" -> trySend(RemoteAgentEvent.Status("Tool Usage: $data"))
                    "status" -> trySend(RemoteAgentEvent.Status(data))
                    else -> Log.d("OpenCodeRepository", "Unknown event type: $type, data: $data")
                }
            }

            override fun onClosed(eventSource: EventSource) {
                trySend(RemoteAgentEvent.Status("Disconnected"))
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                t?.let { trySend(RemoteAgentEvent.Error(it)) }
            }
        }

        val eventSource = EventSources.createFactory(client).newEventSource(request, listener)

        awaitClose {
            eventSource.cancel()
        }
    }

    override suspend fun connect(config: SshConfig) {
        try {
            if (!sshTunnelManager.isConnected()) {
                sshTunnelManager.connect(config)
            }

            val localPort = sshTunnelManager.startForwarding(OPENCODE_DEFAULT_PORT)
            baseUrl = "http://127.0.0.1:$localPort/"

            okHttpClient = OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // Infinite read for SSE
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl!!)
                .client(okHttpClient!!)
                .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
                .build()

            api = retrofit.create(OpenCodeApi::class.java)

            // Create a session immediately upon connection
            val session = api!!.createSession(CreateSessionRequest(title = "Aiyo Chat"))
            currentSessionId = session.id
        } catch (e: Exception) {
            Log.e("OpenCodeRepository", "Connection failed", e)
            throw e
        }
    }

    override suspend fun disconnect() {
        sshTunnelManager.disconnect()
        api = null
        currentSessionId = null
    }

    override suspend fun sendUserMessage(text: String, history: List<Any>) {
        val id = currentSessionId ?: throw IllegalStateException("No active session")
        api?.sendMessage(id, SendMessageRequest(content = text))
    }
}
