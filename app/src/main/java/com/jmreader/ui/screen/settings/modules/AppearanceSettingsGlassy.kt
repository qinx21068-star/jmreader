package com.jmreader.ui.screen.settings.modules

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.data.local.AnimationSpeed
import com.jmreader.data.local.GlassBlurStrength
import com.jmreader.ui.theme.ThemeMode
import com.jmreader.ui.components.RikkaChoiceItem
import com.jmreader.ui.components.RikkaGradientBackground
import com.jmreader.ui.components.RikkaSettingsGroup
import com.jmreader.ui.components.RikkaSettingsItem
import com.jmreader.ui.components.RikkaSliderItem
import com.jmreader.ui.components.RikkaThemeGrid
import com.jmreader.ui.screen.settings.ColorSchemePicker
import com.jmreader.ui.theme.PresetSchemes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsGlassy(
    container: AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by container.settingsStore.settings.collectAsState(initial = container.settingsStore.cachedSnapshot)
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("外观设置") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        RikkaGradientBackground(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding + PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    RikkaSettingsGroup("主题") {
                        RikkaChoiceItem(
                            title = "颜色模式",
                            subtitle = when (settings.themeMode) {
                                ThemeMode.LIGHT -> "浅色"
                                ThemeMode.DARK -> "深色"
                                ThemeMode.SYSTEM -> "跟随系统"
                            },
                            options = listOf(
                                "跟随系统" to (settings.themeMode == ThemeMode.SYSTEM),
                                "浅色" to (settings.themeMode == ThemeMode.LIGHT),
                                "深色" to (settings.themeMode == ThemeMode.DARK),
                            ),
                            onSelect = { label ->
                                scope.launch {
                                    container.settingsStore.setThemeMode(
                                        when (label) {
                                            "浅色" -> ThemeMode.LIGHT
                                            "深色" -> ThemeMode.DARK
                                            else -> ThemeMode.SYSTEM
                                        }
                                    )
                                }
                            },
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            RikkaSettingsItem(
                                title = "动态颜色",
                                subtitle = "基于壁纸动态生成 Material 3 配色",
                                trailingContent = {
                                    Switch(
                                        checked = settings.dynamicColor,
                                        onCheckedChange = { scope.launch { container.settingsStore.setDynamicColor(it) } },
                                    )
                                },
                            )
                        }
                    }
                }
                item {
                    RikkaSettingsGroup("预设主题") {
                        RikkaThemeGrid(
                            schemes = PresetSchemes,
                            selectedId = settings.colorSchemeId,
                            onSelect = { id ->
                                scope.launch {
                                    container.settingsStore.setColorSchemeId(id)
                                    container.settingsStore.setDynamicColor(false)
                                }
                            },
                        )
                    }
                }
                item {
                    RikkaSettingsGroup("自定义主题") {
                        ColorSchemePicker(
                            currentId = settings.colorSchemeId,
                            customColors = settings.customColors,
                            enabled = true,
                            onPickPreset = { id -> scope.launch { container.settingsStore.setColorSchemeId(id) } },
                            onPickCustom = { colors ->
                                scope.launch {
                                    container.settingsStore.setCustomColors(colors)
                                    container.settingsStore.setColorSchemeId("custom")
                                    container.settingsStore.setDynamicColor(false)
                                }
                            },
                        )
                    }
                }
                item {
                    RikkaSettingsGroup("界面") {
                        RikkaSliderItem(
                            title = "卡片圆角",
                            value = settings.cardCornerRadius,
                            valueRange = 4f..28f,
                            valueLabel = "${settings.cardCornerRadius.toInt()} dp",
                            onValueChange = { scope.launch { container.settingsStore.setCardCornerRadius(it) } },
                        )
                        RikkaSliderItem(
                            title = "卡片阴影",
                            value = settings.cardElevation,
                            valueRange = 0f..8f,
                            valueLabel = "${settings.cardElevation.toInt()} dp",
                            onValueChange = { scope.launch { container.settingsStore.setCardElevation(it) } },
                        )
                        RikkaSettingsItem(
                            title = "背景渐变",
                            subtitle = "使用低对比度的 Material 3 层次背景",
                            trailingContent = {
                                Switch(
                                    checked = settings.glassBackgroundEnabled,
                                    onCheckedChange = { scope.launch { container.settingsStore.setGlassBackgroundEnabled(it) } },
                                )
                            },
                        )
                        RikkaChoiceItem(
                            title = "毛玻璃强度",
                            subtitle = when (settings.glassBlurStrength) {
                                GlassBlurStrength.LOW -> "弱"
                                GlassBlurStrength.MEDIUM -> "中"
                                GlassBlurStrength.HIGH -> "强"
                            },
                            options = listOf(
                                "弱" to (settings.glassBlurStrength == GlassBlurStrength.LOW),
                                "中" to (settings.glassBlurStrength == GlassBlurStrength.MEDIUM),
                                "强" to (settings.glassBlurStrength == GlassBlurStrength.HIGH),
                            ),
                            onSelect = { label ->
                                scope.launch {
                                    container.settingsStore.setGlassBlurStrength(
                                        when (label) {
                                            "弱" -> GlassBlurStrength.LOW
                                            "强" -> GlassBlurStrength.HIGH
                                            else -> GlassBlurStrength.MEDIUM
                                        }
                                    )
                                }
                            },
                        )
                        RikkaChoiceItem(
                            title = "页面动画",
                            subtitle = when (settings.animationSpeed) {
                                AnimationSpeed.DISABLED -> "关闭"
                                AnimationSpeed.FAST -> "快速"
                                AnimationSpeed.NORMAL -> "正常"
                            },
                            options = listOf(
                                "关闭" to (settings.animationSpeed == AnimationSpeed.DISABLED),
                                "正常" to (settings.animationSpeed == AnimationSpeed.NORMAL),
                                "快速" to (settings.animationSpeed == AnimationSpeed.FAST),
                            ),
                            onSelect = { label ->
                                scope.launch {
                                    container.settingsStore.setAnimationSpeed(
                                        when (label) {
                                            "关闭" -> AnimationSpeed.DISABLED
                                            "快速" -> AnimationSpeed.FAST
                                            else -> AnimationSpeed.NORMAL
                                        }
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
