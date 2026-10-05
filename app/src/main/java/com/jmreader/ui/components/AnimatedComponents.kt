package com.jmreader.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.jmreader.data.local.AnimationSpeed

@Composable
private fun animationDuration(baseDuration: Int): Int = when (LocalAppSettings.current?.animationSpeed) {
    AnimationSpeed.DISABLED -> 0
    AnimationSpeed.FAST -> (baseDuration / 2).coerceAtLeast(1)
    else -> baseDuration
}

/**
 * Material 3 动画组件集
 * 
 * 包含：
 * - AnimatedComicCard: 带点击缩放反馈的卡片
 * - ShimmerBox: 骨架屏加载动画
 * - AnimatedVisibilityScope: 流畅的显示/隐藏动画
 */

/**
 * 带点击缩放反馈的卡片容器
 * 
 * Material Design 推荐的触摸反馈：按下时缩放到 0.95，松开时弹回 1.0
 * 
 * @param onClick 点击事件
 * @param modifier 修饰符
 * @param enabled 是否启用点击（默认 true）
 * @param content 卡片内容
 */
@Composable
fun AnimatedClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    val animationSpeed = LocalAppSettings.current?.animationSpeed ?: AnimationSpeed.NORMAL
    val scaleSpec: AnimationSpec<Float> = when (animationSpeed) {
        AnimationSpeed.DISABLED -> snap()
        AnimationSpeed.FAST -> spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        )
        AnimationSpeed.NORMAL -> spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        )
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = scaleSpec,
        label = "card_scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            val released = try {
                                tryAwaitRelease()
                            } finally {
                                isPressed = false
                            }
                            if (released) {
                                onClick()
                            }
                        }
                    )
                }
            }
    ) {
        content()
    }
}

/**
 * 骨架屏加载动画（Shimmer Effect）
 * 
 * 用于替换 CircularProgressIndicator，提供更流畅的加载体验
 * 
 * @param modifier 修饰符
 * @param isLoading 是否显示加载动画（false 时显示内容）
 * @param content 实际内容（isLoading = false 时显示）
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true,
    content: @Composable () -> Unit = {},
) {
    if (isLoading) {
        val speed = LocalAppSettings.current?.animationSpeed ?: AnimationSpeed.NORMAL
        val shimmerTranslateState: State<Float> = if (speed == AnimationSpeed.DISABLED) {
            remember { mutableFloatStateOf(0f) }
        } else {
            val shimmerDuration = if (speed == AnimationSpeed.FAST) 600 else 1200
            rememberInfiniteTransition(label = "shimmer").animateFloat(
                initialValue = 0f,
                targetValue = 1000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = shimmerDuration,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "shimmer_translate"
            )
        }
        val shimmerTranslate by shimmerTranslateState
        
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        ),
                        start = Offset(shimmerTranslate - 1000f, shimmerTranslate - 1000f),
                        end = Offset(shimmerTranslate, shimmerTranslate)
                    )
                )
        )
    } else {
        content()
    }
}

/**
 * 列表项进入动画修饰符
 * 
 * 用于 LazyColumn/LazyRow 中的列表项，提供流畅的进入/移除动画
 * 
 * 使用方式：
 * ```
 * LazyColumn {
 *     items(list, key = { it.id }) { item ->
 *         ItemCard(
 *             modifier = Modifier.animateItemPlacement()
 *         )
 *     }
 * }
 * ```
 */
// Note: animateItemPlacement() 是 LazyItemScope 的扩展函数，由 Compose 提供
// 这里提供一个辅助函数用于自定义动画参数
@Composable
fun listItemAnimationSpec(): FiniteAnimationSpec<IntOffset> = when (LocalAppSettings.current?.animationSpeed) {
    com.jmreader.data.local.AnimationSpeed.DISABLED -> tween(durationMillis = 0)
    com.jmreader.data.local.AnimationSpeed.FAST -> spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    else -> spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )
}

/**
 * 带淡入效果的内容容器
 * 
 * @param visible 是否可见
 * @param modifier 修饰符
 * @param content 内容
 */
@Composable
fun FadeInContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val duration = animationDuration(300)
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutSlowInEasing
        ),
        label = "fade_in",
    )
    
    Box(
        modifier = modifier.graphicsLayer { this.alpha = alpha }
    ) {
        if (visible || alpha > 0f) {
            content()
        }
    }
}

/**
 * 带滑动进入效果的内容容器
 * 
 * @param visible 是否可见
 * @param modifier 修饰符
 * @param content 内容
 */
@Composable
fun SlideInContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val speed = LocalAppSettings.current?.animationSpeed ?: AnimationSpeed.NORMAL
    val duration = animationDuration(300)
    val offsetSpec: AnimationSpec<Dp> = when (speed) {
        AnimationSpeed.DISABLED -> snap()
        AnimationSpeed.FAST -> spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        )
        AnimationSpeed.NORMAL -> spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        )
    }
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 20.dp,
        animationSpec = offsetSpec,
        label = "slide_in"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutSlowInEasing
        ),
        label = "slide_in_alpha"
    )
    
    Box(
        modifier = modifier
            .offset(y = offsetY)
            .graphicsLayer { this.alpha = alpha }
    ) {
        if (visible || alpha > 0f) {
            content()
        }
    }
}

/**
 * Material 3 风格的骨架屏卡片
 * 
 * 用于列表加载时的占位
 */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题骨架
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
            )
            
            // 副标题骨架
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
            )
            
            // 内容骨架
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )
        }
    }
}
