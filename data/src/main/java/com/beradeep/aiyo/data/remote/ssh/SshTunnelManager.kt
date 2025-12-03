package com.beradeep.aiyo.data.remote.ssh

import com.beradeep.aiyo.data.remote.opencode.OpenCodeRepository.Companion.OPENCODE_DEFAULT_PORT
import com.beradeep.aiyo.domain.model.SshConfig
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ServerSocket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SshTunnelManager @Inject constructor() {
    private var session: Session? = null
    private var forwardedPort: Int? = null

    suspend fun connect(config: SshConfig) = withContext(Dispatchers.IO) {
        if (session?.isConnected == true) return@withContext

        try {
            val jSch = JSch()
            if (config.isKeyAuth && config.privateKey.isNotBlank()) {
                val keyName = "temp_key_${System.currentTimeMillis()}"
                // JSch requires bytes for private key addIdentity
                jSch.addIdentity(keyName, config.privateKey.toByteArray(), null, null)
            }

            session = jSch.getSession(config.username, config.host, config.port).apply {
                if (!config.isKeyAuth) {
                    setPassword(config.password)
                }
                // TODO: SECURITY CRITICAL: Disabling StrictHostKeyChecking is a significant security risk (MITM).
                // This is a temporary measure for the MVP to allow easy connection to any host without user interaction.
                // In a production version, we MUST implement a proper host key verification mechanism (Trust On First Use + UI Dialog).
                setConfig("StrictHostKeyChecking", "no")
                connect(30000) // 30s timeout
            }
        } catch (e: Exception) {
            disconnect()
            throw IOException("SSH Connection failed: ${e.message}", e)
        }
    }

    suspend fun startForwarding(remotePort: Int = OPENCODE_DEFAULT_PORT): Int = withContext(Dispatchers.IO) {
        if (session?.isConnected != true) throw IOException("SSH Session not connected")

        forwardedPort?.let { return@withContext it }

        val localPort = findFreePort()
        try {
            session?.setPortForwardingL(localPort, "127.0.0.1", remotePort)
            forwardedPort = localPort
            return@withContext localPort
        } catch (e: Exception) {
            throw IOException("Failed to setup port forwarding: ${e.message}", e)
        }
    }

    fun disconnect() {
        try {
            forwardedPort = null
            session?.disconnect()
        } catch (e: Exception) {
            // Ignore disconnect errors
        } finally {
            session = null
        }
    }

    private fun findFreePort(): Int {
        ServerSocket(0).use { socket ->
            return socket.localPort
        }
    }

    fun isConnected(): Boolean = session?.isConnected == true
}
