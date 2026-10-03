package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.data.local.ListStyle
import com.jmreader.ui.components.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.launch

/**
 * 列表设置模块 - Material 3 设计风格
 * 
 * 包含：
 * - 列表样式（网格/列表/紧凑）
 * - 封面裁切模式
 * - 封面宽高比
 * - 标题字号
 * - 正文字号
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListSettingsGlassy(
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
                title = "列表设置",
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
            // ============= 列表样式 =============
            item {
                GlassySectionTitle(
                    title = "列表样式",
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
                            ListStyle.GRID to "双列网格",
                            ListStyle.LIST to "单列列表",
                            ListStyle.COMPACT_GRID to "三列紧凑",
                            ListStyle.CARD to "卡片样式",
                            ListStyle.MAGAZINE to "杂志风格"
                        ).forEach { (style, label) ->
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
                                    selected = settings.listStyle == style,
                                    onClick = {
                                        scope.launch {
                                            container.settingsStore.setListStyle(style)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // ============= 封面设置 =============
            item {
                GlassySectionTitle(
                    title = "封面设置",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
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
                        Text(
                            text = "封面宽高比",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        listOf(
                            "3:4" to "标准漫画 (3:4)",
                            "2:3" to "经典竖版 (2:3)",
                            "1:1" to "正方形 (1:1)",
                            "16:9" to "横版宽屏 (16:9)"
                        ).forEach { (ratio, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                RadioButton(
                                    selected = settings.coverAspectRatio == ratio,
                                    onClick = {
                                        scope.launch {
                                            container.settingsStore.setCoverAspectRatio(ratio)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // ============= 字体大小 =============
            item {
                GlassySectionTitle(
                    title = "字体大小",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            
            item {
                GlassySlider(
                    title = "标题字号",
                    value = settings.listTitleFontSize,
                    onValueChange = {
                        scope.launch {
                            container.settingsStore.setListTitleFontSize(it)
                        }
                    },
                    valueRange = 10f..18f,
                    steps = 7,
                    valueLabel = { "${it.toInt()} sp" },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                GlassySlider(
                    title = "正文字号",
                    value = settings.listBodyFontSize,
                    onValueChange = {
                        scope.launch {
                            container.settingsStore.setListBodyFontSize(it)
                        }
                    },
                    valueRange = 10f..16f,
                    steps = 5,
                    valueLabel = { "${it.toInt()} sp" },
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
