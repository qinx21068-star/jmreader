package com.jmreader.ui.screen.settings.modules

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
 * 外观设置模块 - RikkaHub 毛玻璃风格
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
    settings: AppSettings,
    onThemeModeChange: suspend (ThemeMode) -> Unit,
    onDynamicColorChange: suspend (Boolean) -> Unit,
    onColorSchemeChange: suspend (String) -> Unit,
    onCardCornerRadiusChange: suspend (Float) -> Unit,
    onCardElevationChange: suspend (Float) -> Unit,
    onDetailParallaxChange: suspend (Boolean) -> Unit,
    onSplashAnimChange: suspend (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .haze(state = hazeState)
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            
            // 主题模式选择
            GlassyCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                hazeState = hazeState
            ) {
                Text(
                    text = "主题模式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
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
                            onClick = { scope.launch { onThemeModeChange(mode) } },
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
            
            // Android 12+ 动态取色开关
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                GlassySwitch(
                    title = "Material You 动态取色",
                    subtitle = "从壁纸提取主题色（Android 12+）",
                    checked = settings.dynamicColor,
                    onCheckedChange = { scope.launch { onDynamicColorChange(it) } },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                
                if (settings.dynamicColor) {
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
            
            // 配色方案选择器
            GlassyCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                hazeState = hazeState
            ) {
                Text(
                    text = "预设配色",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(600.dp)
                ) {
                    items(PresetSchemes) { scheme ->
                        ColorSchemeCardGlassy(
                            scheme = scheme,
                            isSelected = settings.colorSchemeId == scheme.id && !settings.dynamicColor,
                            onClick = {
                                scope.launch {
                                    onColorSchemeChange(scheme.id)
                                    if (settings.dynamicColor) {
                                        onDynamicColorChange(false)
                                    }
                                }
                            }
                        )
                    }
                }
            }
            
            // 圆角与阴影设置
            GlassySlider(
                title = "卡片圆角",
                value = settings.cardCornerRadius,
                onValueChange = { scope.launch { onCardCornerRadiusChange(it) } },
                valueRange = 4f..28f,
                valueFormatter = { "${it.toInt()} dp" },
                hazeState = hazeState,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            GlassySlider(
                title = "卡片阴影",
                value = settings.cardElevation,
                onValueChange = { scope.launch { onCardElevationChange(it) } },
                valueRange = 0f..8f,
                valueFormatter = { "${it.toInt()} dp" },
                hazeState = hazeState,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            // 动画与效果
            GlassySwitch(
                title = "详情页视差滚动",
                subtitle = "封面图随滚动产生视差效果",
                checked = settings.detailParallax,
                onCheckedChange = { scope.launch { onDetailParallaxChange(it) } },
                hazeState = hazeState,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            GlassySwitch(
                title = "启动动画",
                subtitle = "应用启动时的淡入动画",
                checked = settings.splashAnim,
                onCheckedChange = { scope.launch { onSplashAnimChange(it) } },
                hazeState = hazeState,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * 毛玻璃风格配色方案卡片
 */
@Composable
private fun ColorSchemeCardGlassy(
    scheme: PresetScheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.9f),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.large
                        )
                    } else Modifier
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 配色名称
                Text(
                    text = scheme.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // 4 个主色调色块
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    scheme.swatches.forEach { color ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
                
                // 描述文本
                Text(
                    text = scheme.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
            
            // 选中指示器
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "已选中",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
