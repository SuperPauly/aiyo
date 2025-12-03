package com.beradeep.aiyo.domain.repository

import com.beradeep.aiyo.domain.model.SshConfig
import com.beradeep.aiyo.domain.model.ThemeType

interface SettingRepository {
    suspend fun getThemeType(): ThemeType
    suspend fun setThemeType(themeType: ThemeType)

    suspend fun getSshConfig(): SshConfig
    suspend fun saveSshConfig(config: SshConfig)
}