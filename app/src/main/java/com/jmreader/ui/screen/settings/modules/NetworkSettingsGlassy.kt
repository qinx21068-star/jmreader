package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.data.local.ImageQuality
import com.jmreader.ui.components.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.launch

/**
 * 网络设置模块 - Material 3 设计风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsGlassy(
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
                title = "网络设置",
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
            // ============= 图片质量 =============
            item {
                GlassySectionTitle(
                    title = "图片质量",
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
                            ImageQuality.ORIGINAL to "原图（高流量）",
                            ImageQuality.HIGH to "高清（推荐）",
                            ImageQuality.MEDIUM to "中等（省流量）",
                            ImageQuality.LOW to "低画质（极省流量）"
                        ).forEach { (quality, label) ->
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
                                    selected = settings.imageQuality == quality,
                                    onClick = {
                                        scope.launch {
                                            container.settingsStore.setImageQuality(quality)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // ============= 网络优化 =============
            item {
                GlassySectionTitle(
                    title = "网络优化",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "强制最高刷新率",
                    subtitle = "锁定屏幕最高刷新率（更流畅但更耗电）",
                    checked = settings.preferMaxRefreshRate,
                    onCheckedChange = {
                        scope.launch {
                            container.settingsStore.setPreferMaxRefreshRate(it)
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
