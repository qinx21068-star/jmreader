package com.jmreader.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * v29.0 - 预设配色方案（RikkaHub 同款 9 套配色）
 * 
 * 每个配色包含：
 * - id: 唯一标识
 * - name: 显示名称
 * - description: 简短描述
 * - swatches: 4 个代表色（用于预览）
 * - lightColors/darkColors: Material 3 完整色板
 */
data class PresetScheme(
    val id: String,
    val name: String,
    val description: String,
    val swatches: List<Color>, // 4 个代表色用于预览卡片
    val lightColors: androidx.compose.material3.ColorScheme,
    val darkColors: androidx.compose.material3.ColorScheme,
)

/**
 * 9 套预设配色（灵感来自 RikkaHub 设计语言）
 */
val PresetSchemes = listOf(
    // 1. 深邃海洋（默认）
    PresetScheme(
        id = "ocean",
        name = "深邃海洋",
        description = "沉稳内敛，适合长时间阅读",
        swatches = listOf(
            Color(0xFF0A7EA4),
            Color(0xFF1E88E5),
            Color(0xFF42A5F5),
            Color(0xFF64B5F6)
        ),
        lightColors = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFF0A7EA4),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFB3E5FC),
            onPrimaryContainer = Color(0xFF001F24),
            secondary = Color(0xFF4A6572),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCFE5F2),
            onSecondaryContainer = Color(0xFF051F26),
        ),
        darkColors = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFF5FC6ED),
            onPrimary = Color(0xFF003543),
            primaryContainer = Color(0xFF004D61),
            onPrimaryContainer = Color(0xFFB3E5FC),
            secondary = Color(0xFFB3C8D6),
            onSecondary = Color(0xFF1D333C),
            secondaryContainer = Color(0xFF334A53),
            onSecondaryContainer = Color(0xFFCFE5F2),
        )
    ),
    
    // 2. 樱花粉梦
    PresetScheme(
        id = "sakura",
        name = "樱花粉梦",
        description = "温柔浪漫，明亮清新",
        swatches = listOf(
            Color(0xFFEC407A),
            Color(0xFFF06292),
            Color(0xFFF48FB1),
            Color(0xFFF8BBD0)
        ),
        lightColors = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFFEC407A),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFD9E2),
            onPrimaryContainer = Color(0xFF3E001A),
            secondary = Color(0xFFC2185B),
            onSecondary = Color(0xFFFFFFFF),
        ),
        darkColors = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFFF48FB1),
            onPrimary = Color(0xFF5C0032),
            primaryContainer = Color(0xFF7B0048),
            onPrimaryContainer = Color(0xFFFFD9E2),
            secondary = Color(0xFFF06292),
            onSecondary = Color(0xFF4A0024),
        )
    ),
    
    // 3. 翠竹青葱
    PresetScheme(
        id = "bamboo",
        name = "翠竹青葱",
        description = "自然清爽，护眼舒适",
        swatches = listOf(
            Color(0xFF43A047),
            Color(0xFF66BB6A),
            Color(0xFF81C784),
            Color(0xFFA5D6A7)
        ),
        lightColors = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFF43A047),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFC8E6C9),
            onPrimaryContainer = Color(0xFF00210A),
            secondary = Color(0xFF558B2F),
            onSecondary = Color(0xFFFFFFFF),
        ),
        darkColors = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFF81C784),
            onPrimary = Color(0xFF003911),
            primaryContainer = Color(0xFF00531A),
            onPrimaryContainer = Color(0xFFC8E6C9),
            secondary = Color(0xFF9CCC65),
            onSecondary = Color(0xFF1A3700),
        )
    ),
    
    // 4. 薰衣草紫
    PresetScheme(
        id = "lavender",
        name = "薰衣草紫",
        description = "优雅神秘，艺术气息",
        swatches = listOf(
            Color(0xFF7E57C2),
            Color(0xFF9575CD),
            Color(0xFFB39DDB),
            Color(0xFFD1C4E9)
        ),
        lightColors = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFF7E57C2),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE1BEE7),
            onPrimaryContainer = Color(0xFF1A0033),
            secondary = Color(0xFF5E35B1),
            onSecondary = Color(0xFFFFFFFF),
        ),
        darkColors = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFFB39DDB),
            onPrimary = Color(0xFF381E72),
            primaryContainer = Color(0xFF4F378B),
            onPrimaryContainer = Color(0xFFE1BEE7),
            secondary = Color(0xFF9575CD),
            onSecondary = Color(0xFF280680),
        )
    ),
    
    // 5. 日落橙光
    PresetScheme(
        id = "sunset",
        name = "日落橙光",
        description = "活力热情，温暖明媚",
        swatches = listOf(
            Color(0xFFFF6F00),
            Color(0xFFFF8F00),
            Color(0xFFFFA726),
            Color(0xFFFFB74D)
        ),
        lightColors = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFFFF6F00),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFE0B2),
            onPrimaryContainer = Color(0xFF2C1600),
            secondary = Color(0xFFF57C00),
            onSecondary = Color(0xFFFFFFFF),
        ),
        darkColors = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFFFFB74D),
            onPrimary = Color(0xFF4A2800),
            primaryContainer = Color(0xFF6A3C00),
            onPrimaryContainer = Color(0xFFFFE0B2),
            secondary = Color(0xFFFFA726),
            onSecondary = Color(0xFF3E1F00),
        )
    ),
    
    // 6. 极光青蓝
    PresetScheme(
        id = "aurora",
        name = "极光青蓝",
        description = "科技感强，现代简约",
        swatches = listOf(
            Color(0xFF00ACC1),
            Color(0xFF26C6DA),
            Color(0xFF4DD0E1),
            Color(0xFF80DEEA)
        ),
        lightColors = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFF00ACC1),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFB2EBF2),
            onPrimaryContainer = Color(0xFF002026),
            secondary = Color(0xFF0097A7),
            onSecondary = Color(0xFFFFFFFF),
        ),
        darkColors = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFF4DD0E1),
            onPrimary = Color(0xFF00363D),
            primaryContainer = Color(0xFF004F58),
            onPrimaryContainer = Color(0xFFB2EBF2),
            secondary = Color(0xFF26C6DA),
            onSecondary = Color(0xFF00292E),
        )
    ),
    
    // 7. 霞光红韵
    PresetScheme(
        id = "crimson",
        name = "霞光红韵",
        description = "浓郁深邃，质感高级",
        swatches = listOf(
            Color(0xFFD32F2F),
            Color(0xFFE57373),
            Color(0xFFEF5350),
            Color(0xFFFF8A80)
        ),
        lightColors = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFFD32F2F),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFCDD2),
            onPrimaryContainer = Color(0xFF410002),
            secondary = Color(0xFFC62828),
            onSecondary = Color(0xFFFFFFFF),
        ),
        darkColors = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFFEF5350),
            onPrimary = Color(0xFF690005),
            primaryContainer = Color(0xFF93000A),
            onPrimaryContainer = Color(0xFFFFCDD2),
            secondary = Color(0xFFE57373),
            onSecondary = Color(0xFF5F0009),
        )
    ),
    
    // 8. 午夜墨蓝
    PresetScheme(
        id = "midnight",
        name = "午夜墨蓝",
        description = "深沉静谧，专注夜读",
        swatches = listOf(
            Color(0xFF1565C0),
            Color(0xFF1976D2),
            Color(0xFF1E88E5),
            Color(0xFF42A5F5)
        ),
        lightColors = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFF1565C0),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFBBDEFB),
            onPrimaryContainer = Color(0xFF001D35),
            secondary = Color(0xFF0D47A1),
            onSecondary = Color(0xFFFFFFFF),
        ),
        darkColors = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFF42A5F5),
            onPrimary = Color(0xFF003258),
            primaryContainer = Color(0xFF00497D),
            onPrimaryContainer = Color(0xFFBBDEFB),
            secondary = Color(0xFF1E88E5),
            onSecondary = Color(0xFF00284D),
        )
    ),
    
    // 9. 琥珀暖金
    PresetScheme(
        id = "amber",
        name = "琥珀暖金",
        description = "复古典雅，温馨怀旧",
        swatches = listOf(
            Color(0xFFFF8F00),
            Color(0xFFFFA726),
            Color(0xFFFFB74D),
            Color(0xFFFFCC80)
        ),
        lightColors = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFFFF8F00),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFE0B2),
            onPrimaryContainer = Color(0xFF2A1800),
            secondary = Color(0xFFF57C00),
            onSecondary = Color(0xFFFFFFFF),
        ),
        darkColors = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFFFFB74D),
            onPrimary = Color(0xFF4A2800),
            primaryContainer = Color(0xFF6A3C00),
            onPrimaryContainer = Color(0xFFFFE0B2),
            secondary = Color(0xFFFFA726),
            onSecondary = Color(0xFF3E1F00),
        )
    ),
)
