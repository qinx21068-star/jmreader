package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.data.local.ListStyle
import com.jmreader.data.local.CornerMode
import com.jmreader.ui.components.*
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListSettingsGlassy(
    container: AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by container.settingsStore.settings.collectAsState()
    val hazeState = remember { HazeState() }
    
    Scaffold(
        topBar = {
            GlassyTopAppBar(
                title = "列表设置",
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
            // 列表样式
            item {
                Text(
                    text = "列表样式",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassyCard(hazeState = hazeState) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "展示风格",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        
                        ListStyle.entries.forEach { style ->
                            FilterChip(
                                selected = settings.listStyle == style,
                                onClick = {
                                    kotlinx.coroutines.MainScope().launch {
                                        container.settingsStore.setListStyle(style)
                                    }
                                },
                                label = {
                                    Text(
                                        when (style) {
                                            ListStyle.LIST -> "单列列表"
                                            ListStyle.GRID -> "双列网格"
                                            ListStyle.COMPACT_GRID -> "三列紧凑"
                                            ListStyle.CARD -> "单列卡片"
                                            ListStyle.MAGAZINE -> "双列杂志"
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            
            // 封面设置
            item {
                Text(
                    text = "封面设置",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassyCard(hazeState = hazeState) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "封面比例",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val ratios = listOf("2:3", "3:4", "1:1", "4:5")
                            ratios.forEachIndexed { index, ratio ->
                                SegmentedButton(
                                    selected = settings.coverAspectRatio == ratio,
                                    onClick = {
                                        kotlinx.coroutines.MainScope().launch {
                                            container.settingsStore.setCoverAspectRatio(ratio)
                                        }
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = ratios.size
                                    ),
                                ) {
                                    Text(ratio)
                                }
                            }
                        }
                    }
                }
            }
            
            // 字体设置
            item {
                Text(
                    text = "字体设置",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySlider(
                    title = "标题字号",
                    subtitle = "列表卡片标题大小",
                    value = settings.listTitleFontSize,
                    onValueChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setListTitleFontSize(it)
                        }
                    },
                    valueRange = 10f..18f,
                    steps = 7,
                    valueLabel = { "${it.toInt()} sp" },
                    hazeState = hazeState,
                )
            }
            
            item {
                GlassySlider(
                    title = "正文字号",
                    subtitle = "副标题和详情文字",
                    value = settings.listBodyFontSize,
                    onValueChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setListBodyFontSize(it)
                        }
                    },
                    valueRange = 10f..16f,
                    steps = 5,
                    valueLabel = { "${it.toInt()} sp" },
                    hazeState = hazeState,
                )
            }
            
            // 卡片样式
            item {
                Text(
                    text = "卡片样式",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassyCard(hazeState = hazeState) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "圆角模式",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CornerMode.entries.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = settings.cornerMode == mode,
                                    onClick = {
                                        kotlinx.coroutines.MainScope().launch {
                                            container.settingsStore.setCornerMode(mode)
                                        }
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = CornerMode.entries.size
                                    ),
                                ) {
                                    Text(
                                        when (mode) {
                                            CornerMode.UNIFIED -> "统一圆角"
                                            CornerMode.SECTIONED -> "分段圆角"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
