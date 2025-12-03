package com.beradeep.aiyo.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SshConfig(
    val host: String = "",
    val port: Int = 22,
    val username: String = "",
    val privateKey: String = "", // Used if isKeyAuth is true
    val password: String = "", // Used if isKeyAuth is false
    val isKeyAuth: Boolean = true
)
