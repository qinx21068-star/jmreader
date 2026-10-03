package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.data.local.ReaderDirection
import com.jmreader.data.local.TapZoneMode
import com.jmreader.ui.components.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.launch

/**
 * 阅读器设置模块 - Material 3 设计风格
 * 
 * 包含：
 * - 翻页方式（左右/上下/瀑布流/RTL）
 * - 点击分区模式
 * - 音量键翻页
 * - 自动滚动 + 速度
 * - 双指缩放
 * - 进度记忆
 * - 预加载下一章
 * - 夜间模式滤镜 + 强度
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsGlassy(
    container: AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by container.settingsStore.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    
    Scaffold(
        topBar = {
            GlassyTopAppBar(
                title = "阅读器设置",
                hazeState = hazeState,
                onNavigationClick = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .haze(state = hazeState)
                .padding(padding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ============= 翻页方式 =============
            item {
                GlassySectionTitle(
                    title = "翻页方式",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                GlassyCard(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    hazeState = hazeState
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            ReaderDirection.HORIZONTAL_LR to "左右翻页 (→)",
                            ReaderDirection.VERTICAL to "上下滚动 (↓)",
                            ReaderDirection.HORIZONTAL_RL to "右左翻页 (←)"
                        ).forEach { (direction, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                RadioButton(
                                    selected = settings.readerDirection == direction,
                                    onClick = {
                                        scope.launch {
                                            container.settingsStore.setReaderDirection(direction)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // ============= 交互设置 =============
            item {
                GlassySectionTitle(
                    title = "交互设置",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            
            // 点击分区模式
            item {
                GlassyCard(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    hazeState = hazeState
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "点击分区模式",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "点击屏幕不同区域的翻页行为",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        listOf(
                            TapZoneMode.LEFT_RIGHT to "左右分区 (左:上一页 / 右:下一页)",
                            TapZoneMode.THIRD_THIRD to "三分区 (左:上一页 / 中:菜单 / 右:下一页)",
                            TapZoneMode.DISABLED to "禁用点击翻页"
                        ).forEach { (mode, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                RadioButton(
                                    selected = settings.tapZoneMode == mode,
                                    onClick = {
                                        scope.launch {
                                            container.settingsStore.setTapZoneMode(mode)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                GlassySwitch(
                    title = "音量键翻页",
                    subtitle = "音量上/下键翻页（仅左右翻页模式）",
                    checked = settings.volumeKeyPaging,
                    onCheckedChange = {
                        scope.launch {
                            container.settingsStore.setVolumeKeyPaging(it)
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "双指缩放",
                    subtitle = "支持双指缩放查看细节",
                    checked = settings.pinchToZoom,
                    onCheckedChange = {
                        scope.launch {
                            container.settingsStore.setPinchToZoom(it)
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            // ============= 自动滚动 =============
            item {
                GlassySectionTitle(
                    title = "自动滚动",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "自动滚动模式",
                    subtitle = "阅读时自动向下滚动（仅上下滚动模式）",
                    checked = settings.autoScroll,
                    onCheckedChange = {
                        scope.launch {
                            container.settingsStore.setAutoScroll(it)
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            if (settings.autoScroll) {
                item {
                    GlassySlider(
                        title = "滚动速度",
                        value = settings.autoScrollSpeed.toFloat(),
                        onValueChange = {
                            scope.launch {
                                container.settingsStore.setAutoScrollSpeed(it.toInt())
                            }
                        },
                        valueRange = 1f..30f,
                        steps = 28,
                        valueLabel = { "${it.toInt()} 页/分钟" },
                        hazeState = hazeState,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
            
            // ============= 进度与缓存 =============
            item {
                GlassySectionTitle(
                    title = "进度与缓存",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "记住阅读进度",
                    subtitle = "精确记录每章的阅读页码",
                    checked = settings.rememberPageLevel,
                    onCheckedChange = {
                        scope.launch {
                            container.settingsStore.setRememberPageLevel(it)
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "预加载下一章",
                    subtitle = "在当前章节末尾自动加载下一章",
                    checked = settings.preloadNextChapter,
                    onCheckedChange = {
                        scope.launch {
                            container.settingsStore.setPreloadNextChapter(it)
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            // ============= 夜间模式 =============
            item {
                GlassySectionTitle(
                    title = "夜间模式",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "暖色滤镜",
                    subtitle = "减少蓝光，保护眼睛",
                    checked = settings.nightModeFilter,
                    onCheckedChange = {
                        scope.launch {
                            container.settingsStore.setNightModeFilter(it)
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            if (settings.nightModeFilter) {
                item {
                    GlassySlider(
                        title = "滤镜强度",
                        value = settings.nightModeFilterStrength,
                        onValueChange = {
                            scope.launch {
                                container.settingsStore.setNightModeFilterStrength(it)
                            }
                        },
                        valueRange = 0f..1f,
                        steps = 9,
                        valueLabel = { "${(it * 100).toInt()}%" },
                        hazeState = hazeState,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
            
            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
