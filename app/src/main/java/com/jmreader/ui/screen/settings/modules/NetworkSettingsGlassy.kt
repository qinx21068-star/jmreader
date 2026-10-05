package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.data.local.ImageQuality
import com.jmreader.ui.components.GlassyTopAppBar
import com.jmreader.ui.components.RikkaChoiceItem
import com.jmreader.ui.components.RikkaGradientBackground
import com.jmreader.ui.components.RikkaSettingsGroup
import com.jmreader.ui.components.RikkaSettingsItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsGlassy(container: AppContainer, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val settings by container.settingsStore.settings.collectAsState(initial = container.settingsStore.cachedSnapshot)
    val scope = rememberCoroutineScope()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { GlassyTopAppBar("网络设置", navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack, onNavigationClick = onBack) },
    ) { padding ->
        RikkaGradientBackground(modifier.fillMaxSize()) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    RikkaSettingsGroup("图片质量") {
                        RikkaChoiceItem(
                            title = "图片质量",
                            subtitle = when (settings.imageQuality) {
                                ImageQuality.ORIGINAL -> "原图"
                                ImageQuality.HIGH -> "高清"
                                ImageQuality.MEDIUM -> "中等"
                                ImageQuality.LOW -> "低画质"
                            },
                            options = listOf(
                                "原图" to (settings.imageQuality == ImageQuality.ORIGINAL),
                                "高清" to (settings.imageQuality == ImageQuality.HIGH),
                                "中等" to (settings.imageQuality == ImageQuality.MEDIUM),
                                "低画质" to (settings.imageQuality == ImageQuality.LOW),
                            ),
                            onSelect = { label -> scope.launch { container.settingsStore.setImageQuality(when (label) { "原图" -> ImageQuality.ORIGINAL; "高清" -> ImageQuality.HIGH; "低画质" -> ImageQuality.LOW; else -> ImageQuality.MEDIUM }) } },
                        )
                    }
                }
                item {
                    RikkaSettingsGroup("网络优化") {
                        RikkaSettingsItem(
                            "强制最高刷新率",
                            "锁定屏幕最高刷新率（更流畅但更耗电）",
                            trailingContent = {
                                Switch(
                                    checked = settings.preferMaxRefreshRate,
                                    onCheckedChange = { value -> scope.launch { container.settingsStore.setPreferMaxRefreshRate(value) } },
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
