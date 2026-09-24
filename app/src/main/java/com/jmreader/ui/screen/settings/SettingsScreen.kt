@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.jmreader.ui.screen.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmreader.R
import com.jmreader.data.AppContainer
import com.jmreader.data.local.BlockMode
import com.jmreader.data.local.CornerMode
import com.jmreader.data.local.ImageQuality
import com.jmreader.data.local.RankingPeriod
import com.jmreader.data.local.ReaderDirection
import com.jmreader.data.local.TabBarStyle
import com.jmreader.data.local.TapZoneMode
import com.jmreader.data.repository.Resource
import com.jmreader.ui.components.DisclaimerDialog
import com.jmreader.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// ============================================================================
// ViewModel（与原版完全一致，未做任何逻辑改动）
// ============================================================================

// SettingsViewModel 已在独立文件 SettingsViewModel.kt
// v28.0 使用 Hilt
// 主入口：在 6 个二级页面之间切换
// ============================================================================

/**
 * v27.5 设置页重构：把原本 11 个分组一屏堆叠的设置页拆为 6 个二级页面，
 * 主入口只展示分类入口列表，点进去看对应分组的详细设置。
 *
 * 二级页面切换用 [rememberSaveable] 持有当前 section 名，不引入新 NavHost 路由，
 * 避免改动全局导航图。系统返回键由 [BackHandler] 拦截：在二级页时先回主入口，再退出设置。
 *
 * 分类（6 个）：
 * - appearance  显示与外观（原「外观」+「列表与界面自定义」合并）
 * - reader      阅读器（原「阅读」）
 * - discovery   内容与发现（原「搜索」+「排行榜」+「内容过滤」合并）
 * - network     网络与下载（原「网络」+「下载」合并）
 * - privacy     账号与隐私（原「隐私」+「账号」合并）
 * - about       关于（原「关于」）
 *
 * 合并原则：把原 11 个 1-3 项的稀疏分组按用户认知聚合，减少主入口卡片数，
 * 让用户一眼看到全部类别，不用滚动找。
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    onOpenLogs: () -> Unit = {},
    onOpenDomains: () -> Unit = {}
) {
    val vm: SettingsViewModel = hiltViewModel()
    // v27.5 性能优化：用 cachedSnapshot 作为初始值，避免 null → 默认 → 真实 两轮重组
    val settings by vm.settings.collectAsState(initial = vm.cachedSnapshot)
    val blockedTags by vm.blockedTags.collectAsState(initial = emptySet())
    val blockedNames by vm.blockedNames.collectAsState(initial = emptySet())
    val blockedAuthors by vm.blockedAuthors.collectAsState(initial = emptySet())
    val loggingIn by vm.loggingIn.collectAsState()
    val savingAndChecking by vm.savingAndChecking.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    // 关于页查看免责声明：点击按钮显示，无倒计时强制（用户已同意过）
    var showDisclaimer by remember { mutableStateOf(false) }

    // v27.5 性能优化：settings 一定非 null（cachedSnapshot 已作为初始值），
    // 之前 settingsLoaded 用于防止 settings==null 期间误操作，现已不需要。
    val settingsLoaded = true

    LaunchedEffect(Unit) {
        vm.events.collect { snackbar.showSnackbar(it) }
    }

    // 二级页面切换：空字符串=主入口列表，否则展示对应分类详情
    var section by rememberSaveable { mutableStateOf("") }

    // 系统返回键拦截：在二级页时按返回先回主入口，再交还系统处理
    BackHandler(enabled = section.isNotEmpty()) { section = "" }

    val ctx = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (section) {
                            "appearance" -> "显示与外观"
                            "reader" -> "阅读器"
                            "discovery" -> "内容与发现"
                            "network" -> "网络与下载"
                            "privacy" -> "账号与隐私"
                            "about" -> "关于"
                            else -> "设置"
                        }
                    )
                },
                navigationIcon = {
                    if (section.isNotEmpty()) {
                        IconButton(onClick = { section = "" }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
            )
        },
    ) { inner ->
        val scrollState = rememberScrollState()
        when (section) {
            "appearance" -> AppearanceSettingsScreen(
                vm = vm,
                settings = settings,
                settingsLoaded = settingsLoaded,
                ctx = ctx,
                inner = inner,
                scrollState = scrollState,
            )
            "reader" -> ReaderSettingsScreen(
                vm = vm,
                settings = settings,
                settingsLoaded = settingsLoaded,
                inner = inner,
                scrollState = scrollState,
            )
            "discovery" -> DiscoverySettingsScreen(
                vm = vm,
                settings = settings,
                blockedTags = blockedTags,
                blockedNames = blockedNames,
                blockedAuthors = blockedAuthors,
                settingsLoaded = settingsLoaded,
                inner = inner,
                scrollState = scrollState,
            )
            "network" -> NetworkSettingsScreen(
                vm = vm,
                container = container,
                settings = settings,
                savingAndChecking = savingAndChecking,
                settingsLoaded = settingsLoaded,
                onOpenDomains = onOpenDomains,
                inner = inner,
                scrollState = scrollState,
            )
            "privacy" -> PrivacySettingsScreen(
                vm = vm,
                settings = settings,
                loggingIn = loggingIn,
                settingsLoaded = settingsLoaded,
                inner = inner,
                scrollState = scrollState,
            )
            "about" -> AboutSettingsScreen(
                onOpenLogs = onOpenLogs,
                onShowDisclaimer = { showDisclaimer = true },
                inner = inner,
                scrollState = scrollState,
            )
            else -> SettingsEntryList(inner = inner, onOpen = { section = it })
        }
    }

    // 关于页查看免责声明：无倒计时，仅查看（用户首次启动时已同意过）
    if (showDisclaimer) {
        DisclaimerDialog(
            forceCountdown = false,
            onAccept = { showDisclaimer = false },
            onDismiss = { showDisclaimer = false },
        )
    }
}

// ============================================================================
// 主入口列表：6 个分类入口卡片
// ============================================================================

@Composable
private fun SettingsEntryList(
    inner: PaddingValues,
    onOpen: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            EntryCard(
                icon = Icons.Outlined.Palette,
                title = "显示与外观",
                subtitle = "主题、配色、背景图、列表样式、卡片圆角、字体",
                onClick = { onOpen("appearance") },
            )
        }
        item {
            EntryCard(
                icon = Icons.Outlined.MenuBook,
                title = "阅读器",
                subtitle = "阅读方向、翻页、缩放、夜间滤镜、自动滚动、预加载",
                onClick = { onOpen("reader") },
            )
        }
        item {
            EntryCard(
                icon = Icons.Outlined.Search,
                title = "内容与发现",
                subtitle = "搜索、排行榜、屏蔽规则",
                onClick = { onOpen("discovery") },
            )
        }
        item {
            EntryCard(
                icon = Icons.Outlined.Cloud,
                title = "网络与下载",
                subtitle = "后端、API 域名、代理、图片、下载、缓存清理",
                onClick = { onOpen("network") },
            )
        }
        item {
            EntryCard(
                icon = Icons.Outlined.Lock,
                title = "账号与隐私",
                subtitle = "登录、应用锁、隐身模式、屏蔽截图",
                onClick = { onOpen("privacy") },
            )
        }
        item {
            EntryCard(
                icon = Icons.Outlined.Info,
                title = "关于",
                subtitle = "版本、日志、免责声明",
                onClick = { onOpen("about") },
            )
        }
    }
}

/**
 * 入口卡片：左侧图标 + 标题/副标题，右侧 chevron 提示可点击进入。
 * 视觉对标微信/哔哩哔哩设置项：浅色背景 + 圆角 + 一行高度紧凑。
 */
@Composable
private fun EntryCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = com.jmreader.ui.theme.LocalCardElevation.current),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ============================================================================
// 二级页面 1：显示与外观
// ============================================================================

@Composable
private fun AppearanceSettingsScreen(
    vm: SettingsViewModel,
    settings: com.jmreader.data.local.AppSettings?,
    settingsLoaded: Boolean,
    ctx: android.content.Context,
    inner: PaddingValues,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ============= 主题与配色 =============
        GroupedSection(
            title = "主题与配色",
            icon = Icons.Outlined.Palette,
        ) {
            Text(
                stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            val mode = settings?.themeMode ?: ThemeMode.SYSTEM
            val labels = listOf(
                stringResource(R.string.settings_theme_system),
                stringResource(R.string.settings_theme_light),
                stringResource(R.string.settings_theme_dark),
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                ThemeMode.entries.forEachIndexed { index, m ->
                    SegmentedButton(
                        selected = mode == m,
                        onClick = { vm.setTheme(m) },
                        shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                        enabled = settingsLoaded,
                    ) { Text(labels[index]) }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // 动态取色
            val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val dynamicSubtitle = if (dynamicColorSupported) {
                stringResource(R.string.settings_dynamic_color_desc)
            } else {
                "Android 12 以下不支持动态取色，需升级系统后使用。"
            }
            SwitchRow(
                title = stringResource(R.string.settings_dynamic_color),
                subtitle = dynamicSubtitle,
                checked = settings?.dynamicColor ?: false,
                onCheckedChange = { vm.setDynamicColor(it) },
                enabled = dynamicColorSupported && settingsLoaded,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // 配色方案
            ColorSchemePicker(
                currentId = settings?.colorSchemeId ?: "kazumi_blue",
                customColors = settings?.customColors,
                enabled = settingsLoaded,
                onPickPreset = { vm.setColorScheme(it) },
                onPickCustom = { vm.setCustomColors(it) },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // 背景图片
            BackgroundImageSection(
                imageUri = settings?.backgroundImageUri,
                opacity = settings?.backgroundImageOpacity ?: 1.0f,
                blur = settings?.backgroundImageBlur ?: 0f,
                lightOnly = settings?.backgroundImageLightOnly ?: true,
                enabled = settingsLoaded,
                onPickUri = { vm.setBackgroundImageUri(it) },
                onClearUri = { vm.setBackgroundImageUri(null) },
                onOpacityChange = { vm.setBackgroundImageOpacity(it) },
                onBlurChange = { vm.setBackgroundImageBlur(it) },
                onLightOnlyChange = { vm.setBackgroundImageLightOnly(it) },
            )
        }

        // ============= 列表与卡片 =============
        GroupedSection(
            title = "列表与卡片",
            icon = Icons.Outlined.Palette,
        ) {
            // 列表样式
            Text(
                "列表样式",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                "选择首页/搜索/收藏的列表展示样式。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            val currentStyle = settings?.listStyle ?: com.jmreader.data.local.ListStyle.LIST
            val styleOptions = listOf(
                com.jmreader.data.local.ListStyle.LIST to "列表",
                com.jmreader.data.local.ListStyle.GRID to "双列网格",
                com.jmreader.data.local.ListStyle.COMPACT_GRID to "三列紧凑",
                com.jmreader.data.local.ListStyle.CARD to "卡片",
                com.jmreader.data.local.ListStyle.MAGAZINE to "杂志风",
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                styleOptions.forEach { (style, label) ->
                    androidx.compose.material3.FilterChip(
                        selected = currentStyle == style,
                        onClick = { vm.setListStyle(style) },
                        enabled = settingsLoaded,
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SliderRow(
                title = "卡片圆角",
                subtitle = "列表/卡片/对话框等圆角半径，0=直角，越大越圆润。",
                value = settings?.cardCornerRadius ?: 14f,
                valueRange = 0f..28f,
                onValueChange = { vm.setCardCornerRadius(it) },
                enabled = settingsLoaded,
                valueFormat = { String.format("%.0f dp", it) },
                preview = { v ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(v.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "圆角 ${String.format("%.0f", v)} dp 预览",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            SliderRow(
                title = "卡片阴影",
                subtitle = "卡片浮起感，0=无阴影（扁平），越大越立体。",
                value = settings?.cardElevation ?: 1f,
                valueRange = 0f..8f,
                onValueChange = { vm.setCardElevation(it) },
                enabled = settingsLoaded,
                valueFormat = { String.format("%.0f dp", it) },
                preview = { v ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = v.dp),
                    ) {
                        Text(
                            "阴影 ${String.format("%.0f", v)} dp 预览",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text("圆角模式", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                "UNIFIED=所有圆角统一；SECTIONED=标题/内容分段不同圆角（信息卡片更分明）。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            val cm = settings?.cornerMode ?: CornerMode.UNIFIED
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                CornerMode.entries.forEachIndexed { idx, m ->
                    SegmentedButton(
                        selected = cm == m,
                        onClick = { vm.setCornerMode(m) },
                        shape = SegmentedButtonDefaults.itemShape(idx, CornerMode.entries.size),
                        enabled = settingsLoaded,
                    ) { Text(if (m == CornerMode.UNIFIED) "统一" else "分段", style = MaterialTheme.typography.labelSmall) }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            SliderRow(
                title = "列表标题字体",
                subtitle = "列表/卡片中标题的字号。",
                value = settings?.listTitleFontSize ?: 13f,
                valueRange = 10f..18f,
                onValueChange = { vm.setListTitleFontSize(it) },
                enabled = settingsLoaded,
                valueFormat = { String.format("%.0f sp", it) },
                preview = { v ->
                    Text(
                        "示例标题：禁漫天堂",
                        fontSize = v.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
            )
            SliderRow(
                title = "列表正文字体",
                subtitle = "列表/卡片中副标题、统计等正文字号。",
                value = settings?.listBodyFontSize ?: 12f,
                valueRange = 10f..16f,
                onValueChange = { vm.setListBodyFontSize(it) },
                enabled = settingsLoaded,
                valueFormat = { String.format("%.0f sp", it) },
                preview = { v ->
                    Text(
                        "示例正文：作者 · 12 章 · 1.2k 收藏",
                        fontSize = v.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text("封面宽高比", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                "杂志风/网格列表中封面的宽高比。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            val ar = settings?.coverAspectRatio ?: "2:3"
            val arOptions = listOf("2:3", "3:4", "1:1", "4:5")
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                arOptions.forEach { opt ->
                    androidx.compose.material3.FilterChip(
                        selected = ar == opt,
                        onClick = { vm.setCoverAspectRatio(opt) },
                        enabled = settingsLoaded,
                        label = { Text(opt, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text("顶部 Tab 栏样式", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                "DEFAULT=图标+文字；COMPACT=紧凑图标+文字；ICON_ONLY=仅图标（节省横向空间）。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            val tbs = settings?.tabBarStyle ?: TabBarStyle.DEFAULT
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                TabBarStyle.entries.forEachIndexed { idx, s ->
                    SegmentedButton(
                        selected = tbs == s,
                        onClick = { vm.setTabBarStyle(s) },
                        shape = SegmentedButtonDefaults.itemShape(idx, TabBarStyle.entries.size),
                        enabled = settingsLoaded,
                    ) {
                        Text(
                            when (s) {
                                TabBarStyle.DEFAULT -> "默认"
                                TabBarStyle.COMPACT -> "紧凑"
                                TabBarStyle.ICON_ONLY -> "仅图标"
                            },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }

        // ============= 显示性能 =============
        GroupedSection(
            title = "显示性能",
            icon = Icons.Outlined.Palette,
        ) {
            SwitchRow(
                title = stringResource(R.string.settings_max_refresh_rate),
                subtitle = stringResource(R.string.settings_max_refresh_rate_desc),
                checked = settings?.preferMaxRefreshRate ?: false,
                onCheckedChange = { vm.setMaxRefreshRate(it) },
                enabled = settingsLoaded,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SwitchRow(
                title = "详情页封面视差",
                subtitle = "详情页向上滚动时封面以较慢速度移动，产生纵深感。",
                checked = settings?.detailParallax ?: true,
                onCheckedChange = { vm.setDetailParallax(it) },
                enabled = settingsLoaded,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SwitchRow(
                title = "应用启动动画",
                subtitle = "Splash 后列表淡入动画。关闭可加快冷启动感知。",
                checked = settings?.splashAnim ?: true,
                onCheckedChange = { vm.setSplashAnim(it) },
                enabled = settingsLoaded,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // 设备刷新率诊断：告诉用户系统实际支持的最高刷新率
            val maxHz = remember {
                runCatching {
                    com.jmreader.MainActivity.maxRefreshRate(ctx as android.app.Activity)
                }.getOrDefault(60f).toInt()
            }
            Text(
                text = when {
                    maxHz >= 90 -> "设备支持最高 ${maxHz}Hz（已开启高刷，本开关可生效）"
                    else -> "设备当前最高 ${maxHz}Hz（系统设置可能未开启高刷新率，本开关无法提升）"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ============================================================================
// 二级页面 2：阅读器
// ============================================================================

@Composable
private fun ReaderSettingsScreen(
    vm: SettingsViewModel,
    settings: com.jmreader.data.local.AppSettings?,
    settingsLoaded: Boolean,
    inner: PaddingValues,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ============= 翻页与导航 =============
        GroupedSection(
            title = "翻页与导航",
            icon = Icons.Outlined.MenuBook,
        ) {
            Text(
                stringResource(R.string.settings_reader_direction),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            val dir = settings?.readerDirection ?: ReaderDirection.VERTICAL
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                SegmentedButton(
                    selected = dir == ReaderDirection.VERTICAL,
                    onClick = { vm.setReaderDirection(ReaderDirection.VERTICAL) },
                    shape = SegmentedButtonDefaults.itemShape(0, 3),
                    enabled = settingsLoaded,
                ) { Text(stringResource(R.string.settings_reader_vertical)) }
                SegmentedButton(
                    selected = dir == ReaderDirection.HORIZONTAL_LR,
                    onClick = { vm.setReaderDirection(ReaderDirection.HORIZONTAL_LR) },
                    shape = SegmentedButtonDefaults.itemShape(1, 3),
                    enabled = settingsLoaded,
                ) { Text("左右(左→右)") }
                SegmentedButton(
                    selected = dir == ReaderDirection.HORIZONTAL_RL,
                    onClick = { vm.setReaderDirection(ReaderDirection.HORIZONTAL_RL) },
                    shape = SegmentedButtonDefaults.itemShape(2, 3),
                    enabled = settingsLoaded,
                ) { Text("左右(右→左)") }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SwitchRow(
                title = "音量键翻页",
                subtitle = "阅读时按音量上/下键翻到上一页/下一页（左右翻页模式效果最佳）。",
                checked = settings?.volumeKeyPaging ?: false,
                onCheckedChange = { vm.setVolumeKeyPaging(it) },
                enabled = settingsLoaded,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text("点击翻页区域", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                "横向阅读时屏幕点击区域分配。竖向阅读单击始终切换工具栏。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            val tapZone = settings?.tapZoneMode ?: TapZoneMode.THIRD_THIRD
            val tapLabels = listOf("左/右两区", "三三制(左中右)", "禁用(仅切栏)")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                TapZoneMode.entries.forEachIndexed { idx, m ->
                    SegmentedButton(
                        selected = tapZone == m,
                        onClick = { vm.setTapZoneMode(m) },
                        shape = SegmentedButtonDefaults.itemShape(idx, TapZoneMode.entries.size),
                        enabled = settingsLoaded,
                    ) { Text(tapLabels[idx], style = MaterialTheme.typography.labelSmall) }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SwitchRow(
                title = "双指缩放",
                subtitle = "阅读时支持双指缩放/双击放大图片。关闭可避免与某些手势冲突。",
                checked = settings?.pinchZoom ?: true,
                onCheckedChange = { vm.setPinchZoom(it) },
                enabled = settingsLoaded,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SwitchRow(
                title = "自动滚动（竖向）",
                subtitle = "竖向阅读时按设定速度自动滚屏，解放双手。",
                checked = settings?.autoScroll ?: false,
                onCheckedChange = { vm.setAutoScroll(it) },
                enabled = settingsLoaded,
            )
            if (settings?.autoScroll == true) {
                SliderRow(
                    title = "自动滚动速度",
                    subtitle = "数值越大滚动越快。",
                    value = settings?.autoScrollSpeed ?: 5f,
                    valueRange = 1f..30f,
                    onValueChange = { vm.setAutoScrollSpeed(it) },
                    enabled = settingsLoaded,
                    valueFormat = { String.format("%.0f px/s", it) },
                    preview = { v ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val arrowCount = ((v - 1f) / (30f - 1f) * 4f).toInt() + 1
                            repeat(arrowCount) {
                                Text(
                                    "▼",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp,
                                )
                            }
                            Text(
                                "速度 ${String.format("%.0f", v)} px/s · 约 ${String.format("%.1f", v * 60 / 100)} 屏/分钟",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            }
        }

        // ============= 显示与字体 =============
        GroupedSection(
            title = "显示与字体",
            icon = Icons.Outlined.MenuBook,
        ) {
            SliderRow(
                title = "阅读器文字大小",
                subtitle = "影响阅读器顶栏标题、页码、提示文字的字号。",
                value = settings?.readerFontSize ?: 14f,
                valueRange = 10f..24f,
                onValueChange = { vm.setReaderFontSize(it) },
                enabled = settingsLoaded,
                valueFormat = { String.format("%.0f sp", it) },
                preview = { v ->
                    Text(
                        "第 1/24 页 · 阅读器文字预览",
                        fontSize = v.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
            )
            SliderRow(
                title = "阅读器行距",
                subtitle = "错误提示等多行文字的行距倍数。",
                value = settings?.readerLineSpacing ?: 1.2f,
                valueRange = 1f..2f,
                onValueChange = { vm.setReaderLineSpacing(it) },
                enabled = settingsLoaded,
                valueFormat = { String.format("%.1f×", it) },
                preview = { v ->
                    Text(
                        buildString {
                            append("第 1 行：加载失败，请重试\n")
                            append("第 2 行：图片正在缓存中\n")
                            append("第 3 行：返回上一章")
                        },
                        fontSize = 14.sp,
                        lineHeight = (14.sp.value * v).sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SwitchRow(
                title = "夜间护眼滤镜",
                subtitle = "在阅读内容上叠加暖色滤镜，降低蓝光，长时间夜间阅读更护眼。",
                checked = settings?.nightModeFilter ?: false,
                onCheckedChange = { vm.setNightModeFilter(it) },
                enabled = settingsLoaded,
            )
            if (settings?.nightModeFilter == true) {
                SliderRow(
                    title = "滤镜强度",
                    subtitle = "0=无叠加，1=强暖色。",
                    value = settings?.nightModeFilterStrength ?: 0.3f,
                    valueRange = 0f..1f,
                    onValueChange = { vm.setNightModeFilterStrength(it) },
                    enabled = settingsLoaded,
                    valueFormat = { String.format("%.0f%%", it * 100) },
                    preview = { v ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(MaterialTheme.colorScheme.surface)
                                .drawWithContent {
                                    drawContent()
                                    drawRect(
                                        color = Color(0xFFFF8800).copy(alpha = v.coerceIn(0f, 1f)),
                                    )
                                },
                        ) {
                            Text(
                                "护眼滤镜预览（强度 ${String.format("%.0f", v * 100)}%）",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                )
            }
        }

        // ============= 加载与进度 =============
        GroupedSection(
            title = "加载与进度",
            icon = Icons.Outlined.MenuBook,
        ) {
            SwitchRow(
                title = "预加载下一章",
                subtitle = "阅读当前章节时后台预取下一章图片，翻章不白屏。",
                checked = settings?.preloadNextChapter ?: true,
                onCheckedChange = { vm.setPreloadNextChapter(it) },
                enabled = settingsLoaded,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SwitchRow(
                title = "进度记忆到具体页",
                subtitle = "记录上次阅读到的具体页码，下次打开自动恢复。关闭则只记到章节。",
                checked = settings?.rememberPageLevel ?: true,
                onCheckedChange = { vm.setRememberPageLevel(it) },
                enabled = settingsLoaded,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ============================================================================
// 二级页面 3：内容与发现
// ============================================================================

@Composable
private fun DiscoverySettingsScreen(
    vm: SettingsViewModel,
    settings: com.jmreader.data.local.AppSettings?,
    blockedTags: Set<String>,
    blockedNames: Set<String>,
    blockedAuthors: Set<String>,
    settingsLoaded: Boolean,
    inner: PaddingValues,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ============= 搜索 =============
        GroupedSection(
            title = "搜索",
            icon = Icons.Outlined.Search,
        ) {
            SwitchRow(
                title = "记录搜索历史",
                subtitle = "在搜索页保存最近搜索词。关闭则不写入本地。",
                checked = settings?.saveSearchHistory ?: true,
                onCheckedChange = { vm.setSaveSearchHistory(it) },
                enabled = settingsLoaded,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            SwitchRow(
                title = "搜索结果 Tag 筛选",
                subtitle = "搜索后显示 Tag chips 二级过滤，方便从结果中精选。",
                checked = settings?.searchTagFilter ?: true,
                onCheckedChange = { vm.setSearchTagFilter(it) },
                enabled = settingsLoaded,
            )
        }

        // ============= 排行榜 =============
        GroupedSection(
            title = "排行榜",
            icon = Icons.Outlined.FilterAlt,
        ) {
            Text("默认时间维度", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                "首页「排行」Tab 默认显示的时间维度。可在首页 Tab 内随时切换。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            val rp = settings?.rankingPeriod ?: RankingPeriod.WEEKLY
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                RankingPeriod.entries.forEachIndexed { idx, p ->
                    SegmentedButton(
                        selected = rp == p,
                        onClick = { vm.setRankingPeriod(p) },
                        shape = SegmentedButtonDefaults.itemShape(idx, RankingPeriod.entries.size),
                        enabled = settingsLoaded,
                    ) {
                        Text(
                            when (p) {
                                RankingPeriod.DAILY -> "日榜"
                                RankingPeriod.WEEKLY -> "周榜"
                                RankingPeriod.MONTHLY -> "月榜"
                                RankingPeriod.ALL -> "总榜"
                            },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }

        // ============= 内容过滤 =============
        GroupedSection(
            title = stringResource(R.string.settings_group_filter),
            icon = Icons.Outlined.FilterAlt,
        ) {
            // v27.14：屏蔽模式选择
            Text("屏蔽模式", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                "命中屏蔽规则的本子如何显示：直接隐藏从列表移除；仅隐藏封面则保留卡片（标题/作者/标签等），封面替换为打叉占位图。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            val bm = settings?.blockMode ?: BlockMode.HIDE
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                SegmentedButton(
                    selected = bm == BlockMode.HIDE,
                    onClick = { vm.setBlockMode(BlockMode.HIDE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) {
                    Text("直接隐藏", style = MaterialTheme.typography.labelSmall)
                }
                SegmentedButton(
                    selected = bm == BlockMode.COVER_ONLY,
                    onClick = { vm.setBlockMode(BlockMode.COVER_ONLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) {
                    Text("仅隐藏封面", style = MaterialTheme.typography.labelSmall)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            BlockListSection(
                title = stringResource(R.string.settings_blocked_tags),
                subtitle = stringResource(R.string.settings_blocked_tags_desc),
                items = blockedTags,
                addButtonText = stringResource(R.string.settings_add_tag),
                inputHint = "输入 Tag 名称",
                emptyText = "暂无屏蔽 Tag",
                onAdd = { vm.addBlockedTag(it) },
                onRemove = { vm.removeBlockedTag(it) },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            BlockListSection(
                title = stringResource(R.string.settings_blocked_names),
                subtitle = stringResource(R.string.settings_blocked_names_desc),
                items = blockedNames,
                addButtonText = stringResource(R.string.settings_add_name),
                inputHint = "输入标题关键词，标题含此词的本子将被屏蔽",
                emptyText = "暂无屏蔽名称",
                onAdd = { vm.addBlockedName(it) },
                onRemove = { vm.removeBlockedName(it) },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            BlockListSection(
                title = "屏蔽作者",
                subtitle = "屏蔽指定作者的所有作品。作者名精确匹配（不区分大小写/繁简），列表即时过滤无需补全。",
                items = blockedAuthors,
                addButtonText = "添加作者",
                inputHint = "输入作者名",
                emptyText = "暂无屏蔽作者",
                onAdd = { vm.addBlockedAuthor(it) },
                onRemove = { vm.removeBlockedAuthor(it) },
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ============================================================================
// 二级页面 4：网络与下载
// ============================================================================

@Composable
private fun NetworkSettingsScreen(
    vm: SettingsViewModel,
    container: AppContainer,
    settings: com.jmreader.data.local.AppSettings?,
    savingAndChecking: Boolean,
    settingsLoaded: Boolean,
    onOpenDomains: () -> Unit,
    inner: PaddingValues,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ============= 后端与代理 =============
        GroupedSection(
            title = "后端与代理",
            icon = Icons.Outlined.Cloud,
        ) {
            Text(
                stringResource(R.string.settings_server),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            var url by remember { mutableStateOf("") }
            LaunchedEffect(settings?.serverUrl) {
                val sv = settings?.serverUrl
                if (url.isBlank() && !sv.isNullOrEmpty()) {
                    url = sv
                }
            }
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                placeholder = { Text(stringResource(R.string.settings_server_hint)) },
                singleLine = true,
            )
            Text(
                text = stringResource(R.string.settings_server_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = { vm.setServerUrl(url.trim()) }) { Text("保存并应用") }
                OutlinedButton(
                    onClick = { vm.saveAndHealthCheck(url.trim()) },
                    enabled = !savingAndChecking,
                ) { Text(if (savingAndChecking) "测试中…" else "保存并测试") }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text("代理设置", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                "支持 host:port（HTTP）或 socks5://host:port。留空=不走代理。改后立即重建网络客户端。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            var proxyInput by remember { mutableStateOf("") }
            LaunchedEffect(settings?.proxy) {
                val px = settings?.proxy
                if (proxyInput.isBlank() && !px.isNullOrBlank()) {
                    proxyInput = px
                }
            }
            OutlinedTextField(
                value = proxyInput,
                onValueChange = { proxyInput = it },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                placeholder = { Text("如 127.0.0.1:7890 或 socks5://127.0.0.1:1080") },
                singleLine = true,
            )
            Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.setProxy(proxyInput) }) { Text("应用") }
                OutlinedButton(onClick = {
                    proxyInput = ""
                    vm.setProxy(null)
                }) { Text("清除") }
            }
        }

        // ============= API 域名管理 =============
        GroupedSection(
            title = "API 域名管理",
            icon = Icons.Outlined.Cloud,
        ) {
            Text(
                "查看/测速/增删禁漫 API 域名。若「最新」加载失败多半是域名过期，可在此手动切换或添加新域名。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "当前域名：${container.directClient.currentDomain().ifBlank { "无" }}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),
            )
            Button(
                onClick = onOpenDomains,
                modifier = Modifier.padding(top = 8.dp),
            ) { Text("进入域名管理") }
        }

        // ============= 图片 =============
        GroupedSection(
            title = "图片",
            icon = Icons.Outlined.Cloud,
        ) {
            Text("图片质量", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                "影响图片加载清晰度与流量消耗。原图最清晰但最耗流量，低最省流量。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            val q = settings?.imageQuality ?: ImageQuality.HIGH
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                ImageQuality.entries.forEachIndexed { idx, qq ->
                    SegmentedButton(
                        selected = q == qq,
                        onClick = { vm.setImageQuality(qq) },
                        shape = SegmentedButtonDefaults.itemShape(idx, ImageQuality.entries.size),
                        enabled = settingsLoaded,
                    ) { Text(qq.label(), style = MaterialTheme.typography.labelSmall) }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text("图片 CDN", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                "留空=自动轮换 CDN。填写域名（如 cdn.example.com）可锁定图片源，避免某些 CDN 不稳定。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            var cdnInput by remember { mutableStateOf("") }
            LaunchedEffect(settings?.pinnedImageCdn) {
                val cdn = settings?.pinnedImageCdn
                if (cdnInput.isBlank() && !cdn.isNullOrBlank()) {
                    cdnInput = cdn
                }
            }
            OutlinedTextField(
                value = cdnInput,
                onValueChange = { cdnInput = it },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                placeholder = { Text("如 cdn.jmcomic.xxx 或留空自动轮换") },
                singleLine = true,
            )
            Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.setPinnedImageCdn(cdnInput.trim().ifBlank { null }) }) { Text("应用") }
                OutlinedButton(onClick = {
                    cdnInput = ""
                    vm.setPinnedImageCdn(null)
                }) { Text("清除") }
            }
        }

        // ============= 下载 =============
        GroupedSection(
            title = "下载",
            icon = Icons.Outlined.Download,
        ) {
            SliderRow(
                title = "下载并发数",
                subtitle = "同时下载的本子数。越大越快但易被限流；in-flight 任务用旧限制完成。",
                value = (settings?.downloadConcurrency ?: 2).toFloat(),
                valueRange = 1f..4f,
                onValueChange = { vm.setDownloadConcurrency(it.toInt()) },
                enabled = settingsLoaded,
                valueFormat = { String.format("%.0f", it) },
                preview = { v ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val n = v.toInt().coerceIn(1, 4)
                        repeat(4) { i ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (i < n) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                            )
                        }
                        Text(
                            "  $n / 4 并发槽位",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically),
                        )
                    }
                },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            SwitchRow(
                title = "已下载本地搜索",
                subtitle = "下载页提供搜索框，按标题/作者/JM号过滤已下载的本子。",
                checked = settings?.localSearchEnabled ?: true,
                onCheckedChange = { vm.setLocalSearchEnabled(it) },
                enabled = settingsLoaded,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text("下载路径", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                "当前：${settings?.downloadDirUri?.let { "自定义外部路径（下载后自动复制）" } ?: "App 内部存储（默认）"}\n" +
                    "在「下载」页面长按任务或菜单中可设置外部存储路径，下载完成后自动复制到该目录。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            if (settings?.downloadDirUri != null) {
                OutlinedButton(
                    onClick = { vm.setDownloadDirUri(null) },
                    modifier = Modifier.padding(top = 6.dp),
                ) { Text("重置为默认路径") }
            }
        }

        // ============= 缓存清理 =============
        GroupedSection(
            title = "缓存清理",
            icon = Icons.Outlined.Cloud,
        ) {
            Text(
                "清除图片缓存可释放磁盘空间但下次浏览需重新下载。清除搜索历史不可恢复。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { vm.clearCache(com.jmreader.data.local.ClearCacheTarget.IMAGES) },
                    modifier = Modifier.weight(1f),
                ) { Text("清图片缓存", maxLines = 1) }
                OutlinedButton(
                    onClick = { vm.clearCache(com.jmreader.data.local.ClearCacheTarget.SEARCH) },
                    modifier = Modifier.weight(1f),
                ) { Text("清搜索历史", maxLines = 1) }
                OutlinedButton(
                    onClick = { vm.clearCache(com.jmreader.data.local.ClearCacheTarget.ALL) },
                    modifier = Modifier.weight(1f),
                ) { Text("全部清除", maxLines = 1) }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ============================================================================
// 二级页面 5：账号与隐私
// ============================================================================

@Composable
private fun PrivacySettingsScreen(
    vm: SettingsViewModel,
    settings: com.jmreader.data.local.AppSettings?,
    loggingIn: Boolean,
    settingsLoaded: Boolean,
    inner: PaddingValues,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ============= 账号 =============
        GroupedSection(
            title = stringResource(R.string.settings_group_account),
            icon = Icons.Outlined.Person,
        ) {
            val user = settings?.loggedInUser
            if (user != null) {
                Text("当前账号：$user", style = MaterialTheme.typography.bodyMedium)
                var showLogoutConfirm by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { showLogoutConfirm = true }, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(R.string.settings_logout))
                }
                // 退出登录二次确认：避免误触导致站点收藏/同步不可用
                if (showLogoutConfirm) {
                    AlertDialog(
                        onDismissRequest = { showLogoutConfirm = false },
                        title = { Text("退出登录") },
                        text = { Text("退出后将无法访问站点收藏和同步功能。\n\n确认退出？") },
                        confirmButton = {
                            TextButton(onClick = {
                                showLogoutConfirm = false
                                vm.logout()
                            }) { Text("退出", color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutConfirm = false }) {
                                Text("取消")
                            }
                        },
                    )
                }
            } else {
                var u by remember { mutableStateOf("") }
                var p by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = u, onValueChange = { u = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("账号") }, singleLine = true,
                )
                OutlinedTextField(
                    value = p, onValueChange = { p = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text("密码") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        capitalization = KeyboardCapitalization.None,
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                )
                Button(
                    onClick = { vm.login(u, p) },
                    modifier = Modifier.padding(top = 8.dp),
                    // 空输入禁用 + 登录中禁用，避免发空请求和重复点击
                    enabled = u.isNotBlank() && p.isNotBlank() && !loggingIn,
                ) { Text(if (loggingIn) "登录中…" else stringResource(R.string.settings_login)) }
            }
        }

        // ============= 应用锁 =============
        GroupedSection(
            title = "应用锁",
            icon = Icons.Outlined.Lock,
        ) {
            SwitchRow(
                title = "应用锁",
                subtitle = "从后台返回前台时要求指纹解锁或输入 PIN。PIN 留空则使用指纹。",
                checked = settings?.appLockEnabled ?: false,
                onCheckedChange = { vm.setAppLockEnabled(it) },
                enabled = settingsLoaded,
            )
            if (settings?.appLockEnabled == true) {
                var pinInput by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it.filter { c -> c.isDigit() }.take(8) },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    label = { Text("PIN 码（4-8 位数字，留空用指纹）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                    ),
                )
                Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        val pin = pinInput.trim()
                        vm.setAppLockPin(if (pin.length in 4..8) pin else null)
                        pinInput = ""
                    }) { Text("保存 PIN") }
                    OutlinedButton(onClick = {
                        pinInput = ""
                        vm.setAppLockPin(null)
                    }) { Text("改用指纹") }
                }
            }
        }

        // ============= 隐私 =============
        GroupedSection(
            title = "隐私",
            icon = Icons.Outlined.Lock,
        ) {
            SwitchRow(
                title = "隐身模式",
                subtitle = "不记录浏览历史、阅读进度、搜索历史。下载仍会正常保存。",
                checked = settings?.incognito ?: false,
                onCheckedChange = { vm.setIncognito(it) },
                enabled = settingsLoaded,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            SwitchRow(
                title = "屏蔽截图",
                subtitle = "给 Activity 加 FLAG_SECURE，阻止系统截图与最近任务预览。重启后生效。",
                checked = settings?.blockScreenshots ?: false,
                onCheckedChange = { vm.setBlockScreenshots(it) },
                enabled = settingsLoaded,
            )
            // v27.15：通知栏稍后再看开关
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            SwitchRow(
                title = "通知栏稍后再看",
                subtitle = "在通知栏常驻一条通知，下拉后在通知里直接输入 JM 号（支持空格/逗号分隔多个）加入「稍后再看」收藏夹。" +
                    "首次开启会申请通知权限，授予后通知出现；用户可在系统设置里随时收回权限。",
                checked = settings?.readLaterNotificationEnabled ?: false,
                onCheckedChange = { vm.setReadLaterNotificationEnabled(it) },
                enabled = settingsLoaded,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ============================================================================
// 二级页面 6：关于
// ============================================================================

@Composable
private fun AboutSettingsScreen(
    onOpenLogs: () -> Unit,
    onShowDisclaimer: () -> Unit,
    inner: PaddingValues,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GroupedSection(
            title = stringResource(R.string.settings_group_about),
            icon = Icons.Outlined.Info,
        ) {
            Text(
                "一根葱 v${com.jmreader.BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                "一根葱 · 第三方 jmcomic2 客户端。干净无广告，仅供个人学习交流。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            OutlinedButton(onClick = onOpenLogs, modifier = Modifier.padding(top = 8.dp)) {
                Text("查看日志 / 诊断")
            }
            OutlinedButton(
                onClick = onShowDisclaimer,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("查看免责声明")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ============================================================================
// 共用 UI 组件（与原版完全一致，未做逻辑改动）
// ============================================================================

/**
 * 分组卡片：顶部图标 + 标题，内容区域由 content 提供。
 * 替代旧的 Section，把零散的设置项按 外观/阅读/网络/过滤/账号/关于 归类。
 *
 * v27.3：补 border + shape + 适度 elevation，让卡片在浅蓝灰背景上边界更清晰；
 * 之前 surfaceVariant=白卡片浮在 EFF3F6 背景上对比度不足，加之无 border 无阴影，
 * 视觉上"卡片糊在一起"。现在用 outlineVariant 描边 + 1dp 阴影，分层更明确。
 */
@Composable
private fun GroupedSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = com.jmreader.ui.theme.LocalCardElevation.current),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/** 带标题 + 副标题的开关行，外观/性能选项统一用这个样式。
 *
 *  v27.3：Row 加 fillMaxWidth，确保 Switch 始终靠右；
 *  副标题加 top padding 2dp 与标题区分；
 *  disabled 时整体 alpha 降低，视觉反馈更明确。 */
@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

/**
 * 屏蔽规则列表 Section（Tag / 名称关键词共用）。
 * 已有规则以可点击删除的 Chip 展示，点「添加」弹出输入框。
 */
@Composable
private fun BlockListSection(
    title: String,
    subtitle: String,
    items: Set<String>,
    addButtonText: String,
    inputHint: String,
    emptyText: String,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
    Text(
        subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp),
    )
    // 检索框：总是显示，方便快速定位某条已禁用的 tag/名称/作者来删除或核对。
    // 用户反馈需要在禁用 tag 区域检索已禁用的项，不再受数量阈值限制。
    var filterText by remember { mutableStateOf("") }
    OutlinedTextField(
        value = filterText,
        onValueChange = { filterText = it },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        placeholder = { Text(if (items.isEmpty()) "检索已屏蔽项" else "检索已屏蔽项（共 ${items.size} 条）") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (filterText.isNotEmpty()) {
                IconButton(onClick = { filterText = "" }) {
                    Icon(Icons.Outlined.Close, contentDescription = "清空检索")
                }
            }
        },
    )
    val visibleItems = remember(items, filterText) {
        if (filterText.isBlank()) items.toList()
        else items.filter { it.contains(filterText.trim(), ignoreCase = true) }
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 8.dp),
    ) {
        visibleItems.forEach { item ->
            // AssistChip：点 chip 主体无操作（避免误删），点 trailingIcon 的叉号才删除
            AssistChip(
                onClick = {},
                label = { Text(item, maxLines = 1) },
                trailingIcon = {
                    IconButton(
                        onClick = { onRemove(item) },
                        modifier = Modifier.size(20.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "移除 $item",
                            modifier = Modifier.size(14.dp),
                        )
                    }
                },
                colors = AssistChipDefaults.assistChipColors(),
            )
        }
        when {
            items.isEmpty() -> Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
            visibleItems.isEmpty() -> Text(
                "无匹配「$filterText」的屏蔽项",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    var newItem by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = { showAdd = true },
        modifier = Modifier.padding(top = 8.dp),
    ) {
        Icon(Icons.Outlined.Add, contentDescription = null)
        Text("  $addButtonText")
    }
    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false; newItem = "" },
            title = { Text(addButtonText) },
            text = {
                OutlinedTextField(
                    value = newItem,
                    onValueChange = { newItem = it },
                    singleLine = true,
                    placeholder = { Text(inputHint) },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newItem.isNotBlank()) {
                        onAdd(newItem.trim())
                        newItem = ""
                    }
                    showAdd = false
                }) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false; newItem = "" }) { Text("取消") }
            },
        )
    }
}

// ============================ v27.5 通用 UI 辅助 ============================

/**
 * 滑块行：标题 + 副标题 + 当前值显示 + Slider + 可选小预览。
 *
 * v27.5 性能修复（卡顿主因）：
 * - 之前 `onValueChange` 直接调 `vm.setXxx()` 写 DataStore，Slider 拖动时每帧（60fps）
 *   都触发 DataStore 写入 → settings StateFlow emit → 所有订阅 settings 的屏幕
 *   （Home/Detail/Reader/Search/Favorites/Downloads/Author/Settings 共 9 个）全部重组。
 *   表现为"拖动滑块时整个 App 卡住"。
 * - 现在 SliderRow 内部用 [localValue] 缓存拖动中的值，拖动时只更新本地 state
 *   （只有 SliderRow 自身重组），拖动结束（onValueChangeFinished）才调
 *   [onValueChange] 写一次 DataStore。
 * - [remember(value)] 的 key 是外部 value：拖动结束写 DataStore 后 settings flow emit，
 *   外部 value 更新 → localValue 重置为新值，保持一致；拖动过程中外部 value 不变，
 *   localValue 跟随手指不被打断。
 *
 * v27.5 小预览：
 * - [preview] 接收当前本地值（拖动中实时变化），用于展示效果预览（如卡片圆角预览一个 MiniCard）。
 *
 * @param onValueChange 拖动**结束**时触发一次（不再每帧触发），用于写 DataStore。
 */
@Composable
private fun SliderRow(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    enabled: Boolean = true,
    valueFormat: (Float) -> String = { String.format("%.0f", it) },
    preview: @Composable ((Float) -> Unit)? = null,
) {
    // 本地缓存拖动中的值，避免每帧写 DataStore 触发全局重组。
    // key=value：外部 value 变化（DataStore 写入完成）时重置 localValue，保持一致。
    var localValue by remember(value) { mutableStateOf(value) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Text(
                valueFormat(localValue),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Slider(
            value = localValue,
            onValueChange = { localValue = it },
            onValueChangeFinished = { onValueChange(localValue) },
            valueRange = valueRange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
        // 小预览：拖动时用本地值实时展示效果
        if (preview != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                preview(localValue)
            }
        }
    }
}

/** 标签辅助：图片质量枚举转中文标签。 */
private fun ImageQuality.label(): String = when (this) {
    ImageQuality.ORIGINAL -> "原图"
    ImageQuality.HIGH -> "高"
    ImageQuality.MEDIUM -> "中"
    ImageQuality.LOW -> "低"
}
