package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.ui.components.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.launch

/**
 * 下载设置模块 - Material 3 设计风格
 */
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
            // ============= 下载性能 =============
            item {
                GlassySectionTitle(
                    title = "下载性能",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                GlassySlider(
                    title = "并发下载数",
                    value = settings.downloadConcurrency.toFloat(),
                    onValueChange = {
                        scope.launch {
                            container.settingsStore.setDownloadConcurrency(it.toInt())
                        }
                    },
                    valueRange = 1f..4f,
                    steps = 2,
                    valueLabel = { "${it.toInt()} 个" },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            // ============= 下载管理 =============
            item {
                GlassySectionTitle(
                    title = "下载管理",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "本地搜索",
                    subtitle = "在已下载内容中搜索",
                    checked = settings.localSearchEnabled,
                    onCheckedChange = {
                        scope.launch {
                            container.settingsStore.setLocalSearchEnabled(it)
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
