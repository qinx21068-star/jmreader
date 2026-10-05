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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.data.local.ReaderDirection
import com.jmreader.data.local.TapZoneMode
import com.jmreader.ui.components.GlassyTopAppBar
import com.jmreader.ui.components.RikkaChoiceItem
import com.jmreader.ui.components.RikkaGradientBackground
import com.jmreader.ui.components.RikkaSettingsGroup
import com.jmreader.ui.components.RikkaSettingsItem
import com.jmreader.ui.components.RikkaSliderItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsGlassy(container: AppContainer, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val settings by container.settingsStore.settings.collectAsState(initial = container.settingsStore.cachedSnapshot)
    val scope = rememberCoroutineScope()
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { GlassyTopAppBar("阅读器设置", navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack, onNavigationClick = onBack) }) { padding ->
        RikkaGradientBackground(modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    RikkaSettingsGroup("翻页方式") {
                        RikkaChoiceItem("阅读方向", settings.readerDirection.name, listOf("左右" to (settings.readerDirection == ReaderDirection.HORIZONTAL_LR), "上下" to (settings.readerDirection == ReaderDirection.VERTICAL), "右左" to (settings.readerDirection == ReaderDirection.HORIZONTAL_RL))) { label -> scope.launch { container.settingsStore.setReaderDirection(when (label) { "左右" -> ReaderDirection.HORIZONTAL_LR; "右左" -> ReaderDirection.HORIZONTAL_RL; else -> ReaderDirection.VERTICAL }) } }
                        RikkaChoiceItem("点击分区", settings.tapZoneMode.name, listOf("左右" to (settings.tapZoneMode == TapZoneMode.LEFT_RIGHT), "三分区" to (settings.tapZoneMode == TapZoneMode.THIRD_THIRD), "关闭" to (settings.tapZoneMode == TapZoneMode.DISABLED))) { label -> scope.launch { container.settingsStore.setTapZoneMode(when (label) { "左右" -> TapZoneMode.LEFT_RIGHT; "关闭" -> TapZoneMode.DISABLED; else -> TapZoneMode.THIRD_THIRD }) } }
                    }
                }
                item {
                    RikkaSettingsGroup("交互") {
                        RikkaSettingsItem(
                            "音量键翻页", "仅左右翻页模式生效",
                            trailingContent = { Switch(checked = settings.volumeKeyPaging, onCheckedChange = { value -> scope.launch { container.settingsStore.setVolumeKeyPaging(value) } }) },
                        )
                        RikkaSettingsItem(
                            "双指缩放", "查看图片细节",
                            trailingContent = { Switch(checked = settings.pinchZoom, onCheckedChange = { value -> scope.launch { container.settingsStore.setPinchZoom(value) } }) },
                        )
                        RikkaSettingsItem(
                            "记住阅读进度", "记录每章页码",
                            trailingContent = { Switch(checked = settings.rememberPageLevel, onCheckedChange = { value -> scope.launch { container.settingsStore.setRememberPageLevel(value) } }) },
                        )
                        RikkaSettingsItem(
                            "预加载下一章", "减少章节切换等待",
                            trailingContent = { Switch(checked = settings.preloadNextChapter, onCheckedChange = { value -> scope.launch { container.settingsStore.setPreloadNextChapter(value) } }) },
                        )
                    }
                }
                item {
                    RikkaSettingsGroup("自动滚动与夜间模式") {
                        RikkaSettingsItem(
                            "自动滚动", "上下滚动模式生效",
                            trailingContent = { Switch(checked = settings.autoScroll, onCheckedChange = { value -> scope.launch { container.settingsStore.setAutoScroll(value) } }) },
                        )
                        if (settings.autoScroll) RikkaSliderItem("滚动速度", settings.autoScrollSpeed, 1f..30f, "${settings.autoScrollSpeed.toInt()} 页/分钟") { scope.launch { container.settingsStore.setAutoScrollSpeed(it) } }
                        RikkaSettingsItem(
                            "暖色滤镜", "减少蓝光",
                            trailingContent = { Switch(checked = settings.nightModeFilter, onCheckedChange = { value -> scope.launch { container.settingsStore.setNightModeFilter(value) } }) },
                        )
                        if (settings.nightModeFilter) RikkaSliderItem("滤镜强度", settings.nightModeFilterStrength, 0f..1f, "${(settings.nightModeFilterStrength * 100).toInt()}%") { scope.launch { container.settingsStore.setNightModeFilterStrength(it) } }
                    }
                }
            }
        }
    }
}
