package com.jmreader.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Material You 配色方案
 * 
 * 基于 Google Material Design 3 规范
 * - 动态取色：Android 12+ 支持从壁纸提取主题色
 * - 固定配色：Android 11- 使用紫罗兰基调
 * 
 * 特点：
 * - 柔和的紫罗兰主色
 * - 青灰次要色
 * - 暖橙第三色
 * - 大圆角（24-32dp）
 * - 彩色阴影
 * - 流畅动效
 * 
 * v29.0 - Material You 风格升级
 */

/**
 * Material You 亮色方案
 * 
 * 配色灵感：Google Photos、Google Calendar 亮色模式
 * 背景：柔和白 #FFFBFE（非纯白，带微妙暖色调）
 * 主色：紫罗兰 #6750A4（优雅、专业）
 * 次要：青灰 #625B71（柔和、中性）
 * 第三：暖橙 #7D5260（温暖、强调）
 */
val MaterialYouLightScheme = lightColorScheme(
    // 主色系 - 紫罗兰
    primary = Color(0xFF6750A4),               // 主要交互元素（按钮、选中态）
    onPrimary = Color(0xFFFFFFFF),             // 主色上的文字（白色）
    primaryContainer = Color(0xFFEADDFF),      // 主色容器（浅紫）
    onPrimaryContainer = Color(0xFF21005D),    // 主色容器上的文字（深紫）
    
    // 次要色系 - 青灰
    secondary = Color(0xFF625B71),             // 次要交互元素（辅助按钮、chip）
    onSecondary = Color(0xFFFFFFFF),           // 次要色上的文字
    secondaryContainer = Color(0xFFE8DEF8),    // 次要色容器（浅紫灰）
    onSecondaryContainer = Color(0xFF1D192B),  // 次要色容器上的文字
    
    // 第三色系 - 暖橙
    tertiary = Color(0xFF7D5260),              // 强调元素（提示、警告）
    onTertiary = Color(0xFFFFFFFF),            // 第三色上的文字
    tertiaryContainer = Color(0xFFFFD8E4),     // 第三色容器（浅粉）
    onTertiaryContainer = Color(0xFF31111D),   // 第三色容器上的文字
    
    // 背景与表面
    background = Color(0xFFFFFBFE),            // 页面背景（柔和白）
    onBackground = Color(0xFF1C1B1F),          // 背景上的文字（深灰）
    surface = Color(0xFFFFFBFE),               // 卡片表面（同背景）
    onSurface = Color(0xFF1C1B1F),             // 表面上的文字
    surfaceVariant = Color(0xFFE7E0EC),        // 表面变体（浅紫灰，用于输入框、chip）
    onSurfaceVariant = Color(0xFF49454F),      // 表面变体上的文字（中灰）
    
    // 轮廓与边框
    outline = Color(0xFF79747E),               // 主要边框（中灰）
    outlineVariant = Color(0xFFCAC4D0),        // 次要边框（浅灰）
    
    // 错误色系
    error = Color(0xFFB3261E),                 // 错误提示
    onError = Color(0xFFFFFFFF),               // 错误色上的文字
    errorContainer = Color(0xFFF9DEDC),        // 错误容器
    onErrorContainer = Color(0xFF410E0B),      // 错误容器上的文字
    
    // 反色表面（对话框、BottomSheet）
    inverseSurface = Color(0xFF313033),        // 反色表面（深灰）
    inverseOnSurface = Color(0xFFF4EFF4),      // 反色表面上的文字（浅灰）
    inversePrimary = Color(0xFFD0BCFF),        // 反色主色（高亮紫）
    
    // 阴影与遮罩
    scrim = Color(0xFF000000),                 // 阴影遮罩（黑色半透明）
)

/**
 * Material You 深色方案
 * 
 * 配色灵感：Google Photos、Google Calendar 深色模式
 * 背景：深紫灰 #1C1B1F（非纯黑，带微妙紫色调）
 * 主色：高亮紫罗兰 #D0BCFF（比亮色版更亮，确保深色背景上清晰）
 * 次要：柔和青灰 #CCC2DC（柔和、不刺眼）
 * 第三：柔和暖橙 #EFB8C8（温暖、舒适）
 */
val MaterialYouDarkScheme = darkColorScheme(
    // 主色系 - 高亮紫罗兰
    primary = Color(0xFFD0BCFF),               // 主要交互元素（更亮，深色背景清晰）
    onPrimary = Color(0xFF381E72),             // 主色上的文字（深紫）
    primaryContainer = Color(0xFF4F378B),      // 主色容器（中紫）
    onPrimaryContainer = Color(0xFFEADDFF),    // 主色容器上的文字（浅紫）
    
    // 次要色系 - 柔和青灰
    secondary = Color(0xFFCCC2DC),             // 次要交互元素
    onSecondary = Color(0xFF332D41),           // 次要色上的文字
    secondaryContainer = Color(0xFF4A4458),    // 次要色容器
    onSecondaryContainer = Color(0xFFE8DEF8),  // 次要色容器上的文字
    
    // 第三色系 - 柔和暖橙
    tertiary = Color(0xFFEFB8C8),              // 强调元素
    onTertiary = Color(0xFF492532),            // 第三色上的文字
    tertiaryContainer = Color(0xFF633B48),     // 第三色容器
    onTertiaryContainer = Color(0xFFFFD8E4),   // 第三色容器上的文字
    
    // 背景与表面
    background = Color(0xFF1C1B1F),            // 页面背景（深紫灰）
    onBackground = Color(0xFFE6E1E5),          // 背景上的文字（浅灰）
    surface = Color(0xFF1C1B1F),               // 卡片表面（同背景）
    onSurface = Color(0xFFE6E1E5),             // 表面上的文字
    surfaceVariant = Color(0xFF49454F),        // 表面变体（中灰，用于输入框、chip）
    onSurfaceVariant = Color(0xFFCAC4D0),      // 表面变体上的文字（浅灰）
    
    // 轮廓与边框
    outline = Color(0xFF938F99),               // 主要边框（中灰）
    outlineVariant = Color(0xFF49454F),        // 次要边框（深灰）
    
    // 错误色系
    error = Color(0xFFF2B8B5),                 // 错误提示（柔和红）
    onError = Color(0xFF601410),               // 错误色上的文字
    errorContainer = Color(0xFF8C1D18),        // 错误容器
    onErrorContainer = Color(0xFFF9DEDC),      // 错误容器上的文字
    
    // 反色表面（对话框、BottomSheet）
    inverseSurface = Color(0xFFE6E1E5),        // 反色表面（浅灰）
    inverseOnSurface = Color(0xFF313033),      // 反色表面上的文字（深灰）
    inversePrimary = Color(0xFF6750A4),        // 反色主色（紫罗兰）
    
    // 阴影与遮罩
    scrim = Color(0xFF000000),                 // 阴影遮罩（黑色半透明）
)

// Material You 配色预览色块已在 ColorSchemes.kt 的 PresetScheme.swatches 中定义
