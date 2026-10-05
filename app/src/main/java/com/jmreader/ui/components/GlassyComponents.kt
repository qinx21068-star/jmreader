package com.jmreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Shape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.jmreader.data.local.AnimationSpeed
import com.jmreader.data.local.AppSettings
import com.jmreader.data.local.GlassBlurStrength
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

val LocalAppSettings = compositionLocalOf<AppSettings?> { null }

private fun motionDuration(speed: AnimationSpeed?, baseDuration: Int): Int = when (speed) {
    AnimationSpeed.DISABLED -> 0
    AnimationSpeed.FAST -> (baseDuration / 2).coerceAtLeast(1)
    AnimationSpeed.NORMAL, null -> baseDuration
}

/**
 * RikkaHub 同款 - 毛玻璃卡片组件
 * 
 * 使用 Haze 库实现背景模糊 + 半透明效果
 * Material 3 风格 + 超大圆角
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    blurRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val duration = motionDuration(LocalAppSettings.current?.animationSpeed, 300)
    // 使用实心卡片，不应用模糊效果
    // 毛玻璃效果应该只用在顶部导航栏等浮动元素
    val cardModifier = modifier
        .animateContentSize(animationSpec = tween(duration))
        .clip(shape)
        .background(backgroundColor)
    
    val finalModifier = if (onClick != null) {
        cardModifier.clickable(onClick = onClick)
    } else {
        cardModifier
    }
    
    Column(
        modifier = finalModifier.padding(20.dp),
        content = content
    )
}

/**
 * RikkaHub 同款 - 毛玻璃顶部 AppBar
 * 
 * 透明背景 + 背景模糊效果
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassyTopAppBar(
    title: String,
    hazeState: HazeState? = null,
    blurRadius: Dp? = null,
    glassEnabled: Boolean? = null,
    navigationIcon: ImageVector = Icons.AutoMirrored.Outlined.ArrowBack,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val settings = LocalAppSettings.current
    val resolvedGlassEnabled = glassEnabled ?: settings?.glassBackgroundEnabled ?: true
    val resolvedBlurRadius = blurRadius ?: when (settings?.glassBlurStrength ?: GlassBlurStrength.MEDIUM) {
        GlassBlurStrength.LOW -> 15.dp
        GlassBlurStrength.MEDIUM -> 30.dp
        GlassBlurStrength.HIGH -> 45.dp
    }
    val appBarModifier = if (resolvedGlassEnabled && hazeState != null) {
        modifier.hazeChild(
            state = hazeState,
            style = HazeStyle(
                blurRadius = resolvedBlurRadius,
                tint = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f),
            )
        )
    } else {
        modifier.background(MaterialTheme.colorScheme.surfaceContainer)
    }
    
    TopAppBar(
        title = { 
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            ) 
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = navigationIcon,
                    contentDescription = "返回"
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        modifier = appBarModifier
    )
}

/**
 * RikkaHub 同款 - 毛玻璃设置项卡片
 * 
 * Pill 图标 + 标题 + 副标题 + 右箭头
 * 带毛玻璃背景模糊效果
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassySettingsCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    hazeState: HazeState? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassyCard(
        modifier = modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = MaterialTheme.shapes.extraLarge,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        blurRadius = 20.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Pill 形状图标背景
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // 标题 + 副标题
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 右箭头
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * RikkaHub 同款 - 毛玻璃开关组件
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassySwitch(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    hazeState: HazeState? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val duration = motionDuration(LocalAppSettings.current?.animationSpeed, 180)
    val switchTrackColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(duration),
        label = "switch_track",
    )
    GlassyCard(
        modifier = modifier.fillMaxWidth(),
        hazeState = hazeState,
        backgroundColor = switchTrackColor,
        blurRadius = 20.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(duration)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

/**
 * RikkaHub 同款 - 毛玻璃滑块组件
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassySlider(
    title: String,
    subtitle: String? = null,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    valueLabel: (Float) -> String = { "%.1f".format(it) },
    hazeState: HazeState? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val duration = motionDuration(LocalAppSettings.current?.animationSpeed, 120)
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(duration),
        label = "slider_value",
    )
    GlassyCard(
        modifier = modifier.fillMaxWidth(),
        hazeState = hazeState,
        backgroundColor = MaterialTheme.colorScheme.surface,
        blurRadius = 20.dp
    ) {
        // 标题 + 副标题（可选）+ 当前值
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            Text(
                text = valueLabel(value),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(Modifier.height(12.dp))
        
        // 滑块
        Slider(
            value = animatedValue,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

/**
 * 带毛玻璃导航栏的 Scaffold
 * 
 * 用于快速升级现有页面，只需替换 Scaffold 为 GlassyScaffold
 * 自动处理 HazeState 和 haze 背景层
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassyScaffold(
    title: String,
    onNavigationClick: () -> Unit,
    navigationIcon: ImageVector,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val hazeState = remember { HazeState() }
    val settings = LocalAppSettings.current
    val blurRadius = when (settings?.glassBlurStrength ?: GlassBlurStrength.MEDIUM) {
        GlassBlurStrength.LOW -> 15.dp
        GlassBlurStrength.MEDIUM -> 30.dp
        GlassBlurStrength.HIGH -> 45.dp
    }
    val glassEnabled = settings?.glassBackgroundEnabled != false
    
    Scaffold(
        topBar = {
            GlassyTopAppBar(
                title = title,
                onNavigationClick = onNavigationClick,
                navigationIcon = navigationIcon,
                hazeState = hazeState,
                blurRadius = blurRadius,
                glassEnabled = glassEnabled,
                actions = actions
            )
        },
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        modifier = modifier
    ) { paddingValues ->
        val backgroundModifier = if (glassEnabled) {
            Modifier.background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceContainer,
                    ),
                )
            )
        } else {
            Modifier.background(MaterialTheme.colorScheme.background)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier)
                .haze(state = hazeState)
        ) {
            content(paddingValues)
        }
    }
}

/**
 * 带渐变背景的内容容器
 * 
 * 作为 haze 源，让毛玻璃效果更明显
 */
@Composable
fun GlassyBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val backgroundModifier = if (LocalAppSettings.current?.glassBackgroundEnabled != false) {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.surfaceContainer,
                ),
                startY = 0f,
                endY = 1000f,
            )
        )
    } else {
        Modifier.background(MaterialTheme.colorScheme.background)
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(backgroundModifier)
    ) {
        content()
    }
}

/**
 * Material 3 分组标题组件
 * 
 * 用于设置页面的分组标题，统一样式
 */
@Composable
fun GlassySectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(vertical = 8.dp)
    )
}
