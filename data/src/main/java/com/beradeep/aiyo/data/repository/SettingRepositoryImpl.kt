package com.beradeep.aiyo.data.repository

import android.content.Context
import com.beradeep.aiyo.data.local.kv.KVStore
import com.beradeep.aiyo.domain.model.SshConfig
import com.beradeep.aiyo.domain.model.ThemeType
import com.beradeep.aiyo.domain.repository.SettingRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingRepositoryImpl(context: Context) : SettingRepository {

    private val kvStore by lazy { KVStore.getInstance(context) }

    override suspend fun getThemeType(): ThemeType {
        return kvStore.getString(KEY_THEME_TYPE)?.let { ThemeType.valueOf(it) } ?: ThemeType.System
    }

    override suspend fun setThemeType(themeType: ThemeType) {
        return kvStore.putString(KEY_THEME_TYPE, themeType.name)
    }

    override suspend fun getSshConfig(): SshConfig {
        val json = kvStore.getEncryptedString(KEY_SSH_CONFIG)
        return if (json.isNullOrBlank()) {
            SshConfig()
        } else {
            try {
                Json.decodeFromString(json)
            } catch (e: Exception) {
                SshConfig()
            }
        }
    }

    override suspend fun saveSshConfig(config: SshConfig) {
        val json = Json.encodeToString(config)
        kvStore.putEncryptedString(KEY_SSH_CONFIG, json)
    }

    companion object {
        const val KEY_THEME_TYPE = "theme_type"
        const val KEY_SSH_CONFIG = "ssh_config"
    }
}