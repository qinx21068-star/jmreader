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
import com.jmreader.ui.components.*
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadSettingsGlassy(
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
                title = "下载设置",
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
            // 下载路径
            item {
                Text(
                    text = "下载路径",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySettingsCard(
                    title = "存储位置",
                    subtitle = if (settings.downloadDirUri.isNullOrBlank()) {
                        "默认内部存储"
                    } else {
                        "自定义路径"
                    },
                    onClick = {
                        // TODO: 实现 SAF 目录选择
                    },
                    hazeState = hazeState,
                )
            }
            
            // 下载性能
            item {
                Text(
                    text = "下载性能",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySlider(
                    title = "并发下载数",
                    subtitle = "同时下载的任务数量",
                    value = settings.downloadConcurrency.toFloat(),
                    onValueChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setDownloadConcurrency(it.toInt())
                        }
                    },
                    valueRange = 1f..4f,
                    steps = 2,
                    valueLabel = { "${it.toInt()} 个" },
                    hazeState = hazeState,
                )
            }
            
            // 本地搜索
            item {
                Text(
                    text = "本地搜索",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "启用本地搜索",
                    subtitle = "在下载页搜索已下载内容",
                    checked = settings.localSearchEnabled,
                    onCheckedChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setLocalSearchEnabled(it)
                        }
                    },
                    hazeState = hazeState,
                )
            }
            
            // 通知栏快捷
            item {
                Text(
                    text = "快捷功能",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "通知栏稍后再看",
                    subtitle = "常驻通知快速收藏漫画",
                    checked = settings.readLaterNotificationEnabled,
                    onCheckedChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setReadLaterNotificationEnabled(it)
                        }
                    },
                    hazeState = hazeState,
                )
            }
        }
    }
}
