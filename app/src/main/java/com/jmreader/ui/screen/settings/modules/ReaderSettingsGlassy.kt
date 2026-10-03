package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.data.local.ReaderDirection
import com.jmreader.data.local.TapZoneMode
import com.jmreader.ui.components.*
import dev.chrisbanes.haze.HazeState

/**
 * RikkaHub 毛玻璃风格 - 阅读器设置模块
 * 
 * v29.0 Material You + Haze 毛玻璃升级
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
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                onNavigationClick = onBack,
            )
        },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            // 翻页方式
            item {
                Text(
                    text = "翻页方式",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassyCard(hazeState = hazeState) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "阅读方向",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ReaderDirection.entries.forEachIndexed { index, dir ->
                                SegmentedButton(
                                    selected = settings.readerDirection == dir,
                                    onClick = {
                                        kotlinx.coroutines.MainScope().launch {
                                            container.settingsStore.setReaderDirection(dir)
                                        }
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = ReaderDirection.entries.size
                                    ),
                                ) {
                                    Text(
                                        when (dir) {
                                            ReaderDirection.VERTICAL -> "上下滚动"
                                            ReaderDirection.HORIZONTAL_LR -> "左右翻页"
                                            ReaderDirection.HORIZONTAL_RL -> "右左翻页"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // 横向翻页设置
            if (settings.readerDirection != ReaderDirection.VERTICAL) {
                item {
                    GlassyCard(hazeState = hazeState) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "点击翻页区域",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TapZoneMode.entries.forEachIndexed { index, mode ->
                                    SegmentedButton(
                                        selected = settings.tapZoneMode == mode,
                                        onClick = {
                                            kotlinx.coroutines.MainScope().launch {
                                                container.settingsStore.setTapZoneMode(mode)
                                            }
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = TapZoneMode.entries.size
                                        ),
                                    ) {
                                        Text(
                                            when (mode) {
                                                TapZoneMode.LEFT_RIGHT -> "左右翻页"
                                                TapZoneMode.THIRD_THIRD -> "三分区"
                                                TapZoneMode.DISABLED -> "禁用"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    GlassySwitch(
                        title = "音量键翻页",
                        subtitle = "按音量键快速翻页",
                        checked = settings.volumeKeyPaging,
                        onCheckedChange = {
                            kotlinx.coroutines.MainScope().launch {
                                container.settingsStore.setVolumeKeyPaging(it)
                            }
                        },
                        hazeState = hazeState,
                    )
                }
            }
            
            // 竖向滚动设置
            if (settings.readerDirection == ReaderDirection.VERTICAL) {
                item {
                    GlassySwitch(
                        title = "自动滚动",
                        subtitle = "按设定速度自动滚屏",
                        checked = settings.autoScroll,
                        onCheckedChange = {
                            kotlinx.coroutines.MainScope().launch {
                                container.settingsStore.setAutoScroll(it)
                            }
                        },
                        hazeState = hazeState,
                    )
                }
                
                if (settings.autoScroll) {
                    item {
                        GlassySlider(
                            title = "滚动速度",
                            subtitle = "自动滚屏的速度",
                            value = settings.autoScrollSpeed,
                            onValueChange = {
                                kotlinx.coroutines.MainScope().launch {
                                    container.settingsStore.setAutoScrollSpeed(it)
                                }
                            },
                            valueRange = 1f..30f,
                            steps = 28,
                            valueLabel = { "${it.toInt()} px/s" },
                            hazeState = hazeState,
                        )
                    }
                }
            }
            
            // 缩放与交互
            item {
                Text(
                    text = "缩放与交互",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "双指缩放",
                    subtitle = "捏合手势放大缩小图片",
                    checked = settings.pinchZoom,
                    onCheckedChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setPinchZoom(it)
                        }
                    },
                    hazeState = hazeState,
                )
            }
            
            // 进度与预加载
            item {
                Text(
                    text = "进度与预加载",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "记忆页码级进度",
                    subtitle = "精确记住每章的阅读页码",
                    checked = settings.rememberPageLevel,
                    onCheckedChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setRememberPageLevel(it)
                        }
                    },
                    hazeState = hazeState,
                )
            }
            
            item {
                GlassySwitch(
                    title = "预加载下一章",
                    subtitle = "避免翻章时白屏等待",
                    checked = settings.preloadNextChapter,
                    onCheckedChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setPreloadNextChapter(it)
                        }
                    },
                    hazeState = hazeState,
                )
            }
            
            // 夜间护眼
            item {
                Text(
                    text = "夜间护眼",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "护眼滤镜",
                    subtitle = "暖色叠加层，降低蓝光",
                    checked = settings.nightModeFilter,
                    onCheckedChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setNightModeFilter(it)
                        }
                    },
                    hazeState = hazeState,
                )
            }
            
            if (settings.nightModeFilter) {
                item {
                    GlassySlider(
                        title = "滤镜强度",
                        subtitle = "暖色调的强度",
                        value = settings.nightModeFilterStrength,
                        onValueChange = {
                            kotlinx.coroutines.MainScope().launch {
                                container.settingsStore.setNightModeFilterStrength(it)
                            }
                        },
                        valueRange = 0f..1f,
                        steps = 9,
                        valueLabel = { "${(it * 100).toInt()}%" },
                        hazeState = hazeState,
                    )
                }
            }
        }
    }
}
