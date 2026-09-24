package com.jmreader.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmreader.data.local.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 设置页 ViewModel。
 * 
 * v28.0: 使用 Hilt 依赖注入的示例 ViewModel。
 * 演示如何通过 @HiltViewModel 和 @Inject 构造函数注入依赖。
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsStore: SettingsStore
) : ViewModel() {

    val settings: StateFlow<com.jmreader.data.local.Settings> = settingsStore.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = settingsStore.cachedSnapshot
        )

    suspend fun updateThemeMode(mode: com.jmreader.ui.theme.ThemeMode) {
        settingsStore.setThemeMode(mode)
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        settingsStore.setDynamicColor(enabled)
    }
}
