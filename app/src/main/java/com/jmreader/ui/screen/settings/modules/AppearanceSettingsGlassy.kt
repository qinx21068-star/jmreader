package com.jmreader.ui.screen.settings.modules

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jmreader.data.local.AppSettings
import com.jmreader.ui.components.*
import com.jmreader.ui.theme.PresetScheme
import com.jmreader.ui.theme.PresetSchemes
import com.jmreader.ui.theme.ThemeMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.launch

/**
 * 外观设置模块 - Material 3 设计风格
 * 
 * 包含：
 * - 主题模式（浅色/深色/跟随系统）
 * - 配色方案选择器（9 个预设 + 动态取色）
 * - 卡片圆角滑块（4-28dp）
 * - 卡片阴影滑块（0-8dp）
 * - 视差滚动开关
 * - 启动动画开关
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsGlassy(
    container: com.jmreader.data.AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by container.settingsStore.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    
    Scaffold(
        topBar = {
            GlassyTopAppBar(
                title = "外观设置",
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
            // ============= 主题模式 =============
            item {
                GlassySectionTitle(
                    title = "主题模式",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                GlassyCard(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    hazeState = hazeState
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            ThemeMode.LIGHT to "浅色",
                            ThemeMode.DARK to "深色",
                            ThemeMode.SYSTEM to "跟随系统"
                        ).forEachIndexed { index, (mode, label) ->
                            SegmentedButton(
                                selected = settings.themeMode == mode,
                                onClick = { scope.launch { container.settingsStore.setThemeMode(mode) } },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = 3
                                )
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // ============= 配色方案 =============
            item {
                GlassySectionTitle(
                    title = "配色方案",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            
            // Android 12+ 动态取色开关
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    GlassySwitch(
                        title = "Material You 动态取色",
                        subtitle = "从壁纸提取主题色（Android 12+）",
                        checked = settings.dynamicColor,
                        onCheckedChange = { scope.launch { container.settingsStore.setDynamicColor(it) } },
                        hazeState = hazeState,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                
                if (settings.dynamicColor) {
                    item {
                        Text(
                            text = "✨ 动态取色已启用，预设配色将被覆盖",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )
                    }
                }
            }
            
            // 配色方案列表（流式布局，不固定高度）
            items(PresetSchemes) { scheme ->
                ColorSchemeCard(
                    scheme = scheme,
                    isSelected = settings.colorSchemeId == scheme.id && !settings.dynamicColor,
                    onClick = {
                        scope.launch {
                            container.settingsStore.setColorSchemeId(scheme.id)
                            if (settings.dynamicColor) {
                                container.settingsStore.setDynamicColor(false)
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            // ============= 卡片样式 =============
            item {
                GlassySectionTitle(
                    title = "卡片样式",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            
            item {
                GlassySlider(
                    title = "卡片圆角",
                    value = settings.cardCornerRadius,
                    onValueChange = { scope.launch { container.settingsStore.setCardCornerRadius(it) } },
                    valueRange = 4f..28f,
                    valueLabel = { "${it.toInt()} dp" },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                GlassySlider(
                    title = "卡片阴影",
                    value = settings.cardElevation,
                    onValueChange = { scope.launch { container.settingsStore.setCardElevation(it) } },
                    valueRange = 0f..8f,
                    valueLabel = { "${it.toInt()} dp" },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            // ============= 动画与效果 =============
            item {
                GlassySectionTitle(
                    title = "动画与效果",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "详情页视差滚动",
                    subtitle = "封面图随滚动产生视差效果",
                    checked = settings.detailParallax,
                    onCheckedChange = { scope.launch { container.settingsStore.setDetailParallax(it) } },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "启动动画",
                    subtitle = "应用启动时的淡入动画",
                    checked = settings.splashAnim,
                    onCheckedChange = { scope.launch { container.settingsStore.setSplashAnim(it) } },
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

/**
 * Material 3 风格配色方案卡片
 */
@Composable
private fun ColorSchemeCard(
    scheme: PresetScheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.large
                        )
                    } else Modifier
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 色块预览
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                scheme.swatches.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
            
            // 名称和描述
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = scheme.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = scheme.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            
            // 选中指示器
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "已选中",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
