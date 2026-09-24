@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.jmreader.ui.screen.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.NavigateBefore
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import me.saket.telephoto.zoomable.zoomable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmreader.data.AppContainer
import com.jmreader.data.dto.ComicBriefDto
import com.jmreader.data.dto.ComicDetailDto
import com.jmreader.data.local.ReaderDirection
import com.jmreader.data.local.TapZoneMode
import com.jmreader.data.repository.Resource
import com.jmreader.data.repository.proxiedImageUrl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

// ReaderViewModel 已迁移到独立文件 ReaderViewModel.kt
// v28.0 使用 Hilt Assisted Injection

@Composable
fun ReaderScreen(
    container: AppContainer,
    comicId: String,
    chapterId: String,
    onBack: () -> Unit,
    onOpenLogs: () -> Unit = {}
) {
    val vm: ReaderViewModel = hiltViewModel<ReaderViewModel, ReaderViewModel.Factory> { factory ->
        factory.create(comicId, chapterId)
    }
    val state by vm.state.collectAsState()
    // v27.5 性能优化：用 cachedSnapshot 作为初始值，避免 null → 默认 → 真实 两轮重组
    val settings by container.settingsStore.settings.collectAsState(initial = container.settingsStore.cachedSnapshot)
    val direction = settings.readerDirection
    val volumeKeyPaging = settings.volumeKeyPaging
    // v27.5 #5：阅读器增强设置
    val tapZoneMode = settings.tapZoneMode
    val pinchZoom = settings.pinchZoom
    val autoScroll = settings.autoScroll
    val autoScrollSpeed = settings.autoScrollSpeed
    val readerFontSize = settings.readerFontSize
    // v27.6：阅读器行距（之前未应用，设置项无效）
    val readerLineSpacing = settings.readerLineSpacing
    // v27.6：图片质量档位（之前未接线，设置页改了无效）
    val imageQuality = settings.imageQuality
    // v27.5 #5：夜间护眼滤镜
    val nightModeFilter = settings.nightModeFilter
    val nightModeFilterStrength = settings.nightModeFilterStrength

    var uiVisible by remember { mutableStateOf(true) }
    // 阅读进度恢复提示：进入章节自动滚到上次位置后，弹 snackbar 告知用户
    val jumpSnackbar = remember { androidx.compose.material3.SnackbarHostState() }
    val jumpScope = androidx.compose.runtime.rememberCoroutineScope()

    // 当前页码（0-based），用于底栏页码指示器和跳页滑块。
    // 注意：用 state.imageFiles 作为 key，切章后 imageFiles 变化，currentPage 重置为 0，
    // 避免旧章遗留页码污染新章（导致页码错乱、Slider value 越界）。
    var currentPage by remember(state.imageFiles) { mutableStateOf(0) }
    // Slider 拖动期间的中间值：拖动时只更新此值，释放后才写入 currentPage 并真正滚动，
    // 避免拖动期间 currentPage 被改写导致 UI 显示与实际页面不同步。
    var sliderDragging by remember { mutableStateOf(0f) }
    var isSliderDragging by remember { mutableStateOf(false) }
    // 竖滑和横滑各自的滚动状态，提升到 ReaderScreen 以便底栏 Slider 操控
    val verticalListState = rememberLazyListState()
    val horizontalPagerState = rememberPagerState(pageCount = { state.imageFiles.size })

    // 切章时把页码、Slider 中间值重置到首页。
    // 滚动状态（ListState/PagerState）的重置由 ReaderPager 内部的 LaunchedEffect 统一处理
    // （滚到 initialPage 或 0），避免两个 LaunchedEffect 冲突导致闪烁。
    //
    // 关键修复（Bug 42）：之前未重置 isSliderDragging。若用户拖动 Slider 期间点"下一章"，
    // 切章后 sliderDragging 重置为 0f 但 isSliderDragging 仍 true →
    // 新章节 Slider value 取 sliderDragging=0f，但实际页面可能是恢复的 initialPage=5 →
    // Slider 显示 0 但实际在第 5 页；用户继续拖动会从 0 跳，被强制跳到第 0 页。
    LaunchedEffect(state.imageFiles) {
        currentPage = 0
        sliderDragging = 0f
        isSliderDragging = false
    }

    // 阅读器沉浸态：工具栏隐藏时，第一次系统返回键先唤出工具栏，第二次才退出。
    // 避免用户误触返回直接退出阅读器，丢失上下文。
    BackHandler(enabled = !uiVisible) {
        uiVisible = true
    }

    val totalPages = state.imageFiles.size

    /**
     * 跳转到指定页码（v26 新增：音量键翻页用）。
     *
     * 与底栏 Slider 释放后的跳转逻辑一致：更新 currentPage + 触发实际滚动。
     * 越界自动 coerce 到 [0, totalPages-1]，越界时不动作（避免无意义刷新）。
     */
    fun jumpToPage(target: Int) {
        if (totalPages <= 0) return
        val clamped = target.coerceIn(0, totalPages - 1)
        if (clamped == currentPage) return
        currentPage = clamped
        jumpScope.launch {
            if (direction == ReaderDirection.VERTICAL) {
                verticalListState.scrollToItem(clamped)
            } else {
                horizontalPagerState.scrollToPage(clamped)
            }
        }
    }

    // 音量键翻页（v26 新增）：用户在设置中开启后，阅读器拦截音量上/下键翻页。
    //
    // v27.6 修复：原实现仅用 Compose `onPreviewKeyEvent`，在阅读器沉浸态（顶/底栏隐藏、
    // 无 TextField 聚焦）下事件可能不进入 Compose modifier → 音量键无反应。
    // 改为 Activity.onKeyDown + 单例桥接器双保险：
    // - Compose onPreviewKeyEvent 仍保留（处理有焦点节点时的事件）
    // - MainActivity.onKeyDown 调用 ReaderVolumeKeyBridge 处理无焦点时的事件
    //
    // 用 onPreviewKeyEvent 在系统处理音量之前消费事件，返回 true 阻止系统调整音量。
    // - VOLUME_DOWN → 下一页（与翻书方向一致，更自然）
    // - VOLUME_UP → 上一页
    // 仅在加载完成且有图片时生效；加载中/出错时不拦截，让用户能调整音量。
    val volumeKeyModifier = if (volumeKeyPaging && totalPages > 0) {
        Modifier.onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            when (event.key) {
                Key.VolumeDown -> {
                    jumpToPage(currentPage + 1)
                    true
                }
                Key.VolumeUp -> {
                    jumpToPage(currentPage - 1)
                    true
                }
                else -> false
            }
        }
    } else Modifier

    // v27.6：注册音量键翻页回调到单例桥接器，让 MainActivity.onKeyDown 能触发翻页
    androidx.compose.runtime.DisposableEffect(volumeKeyPaging, totalPages) {
        if (volumeKeyPaging && totalPages > 0) {
            ReaderVolumeKeyBridge.callback = { isUp ->
                if (isUp) jumpToPage(currentPage - 1) else jumpToPage(currentPage + 1)
            }
        }
        onDispose { ReaderVolumeKeyBridge.callback = null }
    }

    Scaffold(
        modifier = volumeKeyModifier,
        snackbarHost = { androidx.compose.material3.SnackbarHost(jumpSnackbar) },
        topBar = {
            if (uiVisible) {
                // v27.6：线路切换 sheet 状态
                var showLineSheet by remember { mutableStateOf(false) }
                TopAppBar(
                    title = {
                        // v27.5 #22：阅读器标题字体大小由设置控制
                        Text(
                            state.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = readerFontSize.sp,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                        }
                    },
                    // v27.6：顶栏右侧加测速切换线路按钮
                    actions = {
                        IconButton(onClick = { showLineSheet = true }) {
                            Icon(Icons.Outlined.Speed, contentDescription = "线路测速")
                        }
                    },
                    modifier = Modifier.statusBarsPadding(),
                )
                // v27.6 线路测速切换 Sheet
                if (showLineSheet) {
                    LineSpeedTestSheet(
                        container = container,
                        onDismiss = { showLineSheet = false },
                    )
                }
            }
        },
        bottomBar = {
            if (uiVisible && totalPages > 0) {
                BottomAppBar(
                    modifier = Modifier.navigationBarsPadding(),
                ) {
                    IconButton(
                        onClick = { state.prevChapterId?.let { vm.gotoChapter(it) } },
                        enabled = state.prevChapterId != null,
                    ) { Icon(Icons.AutoMirrored.Outlined.NavigateBefore, contentDescription = "上一章") }
                    // 页码指示器：拖动 Slider 时显示拖动中间值，平时显示实际页码
                    val displayPage = if (isSliderDragging) sliderDragging.toInt() else currentPage
                    Text(
                        "${(displayPage + 1).coerceIn(1, totalPages)}/$totalPages",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = readerFontSize.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                    Slider(
                        value = if (isSliderDragging) sliderDragging else currentPage.toFloat(),
                        onValueChange = {
                            isSliderDragging = true
                            sliderDragging = it
                        },
                        onValueChangeFinished = {
                            val target = sliderDragging.toInt().coerceIn(0, totalPages - 1)
                            isSliderDragging = false
                            currentPage = target
                            jumpScope.launch {
                                if (direction == ReaderDirection.VERTICAL) {
                                    verticalListState.scrollToItem(target)
                                } else {
                                    horizontalPagerState.scrollToPage(target)
                                }
                            }
                        },
                        valueRange = 0f..(totalPages - 1).toFloat().coerceAtLeast(0f),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    )
                    IconButton(
                        onClick = { state.nextChapterId?.let { vm.gotoChapter(it) } },
                        enabled = state.nextChapterId != null,
                    ) { Icon(Icons.AutoMirrored.Outlined.NavigateNext, contentDescription = "下一章") }
                }
            }
        },
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner).background(Color.Black)) {
            // v27.5 稳定性加固：捕获 state 到本地 val，避免 `state.error != null` 通过后
            // 在另一线程写 StateFlow 时 state.error 变 null，下一行 `state.error!!` 抛 NPE。
            // collectAsState 的 by 委托每次访问都读最新 snapshot，单次 composition 内不保证一致。
            val s = state
            when {
                s.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
                s.error != null -> com.jmreader.ui.components.ErrorBox(
                    message = s.error,
                    onRetry = { vm.load() },
                    onViewLogs = onOpenLogs,
                )
                s.imageFiles.isEmpty() -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("无图片", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontSize = readerFontSize.sp, lineHeight = (readerFontSize * readerLineSpacing).sp))
                    Text(
                        "该章节可能未发布或被删除",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = readerFontSize.sp, lineHeight = (readerFontSize * readerLineSpacing).sp),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        androidx.compose.material3.Button(onClick = { vm.load() }) { Text("重试") }
                        // v27.13：空章节提供翻章按钮，避免用户被困在空章节无法继续
                        if (s.prevChapterId != null) {
                            androidx.compose.material3.OutlinedButton(onClick = { vm.gotoChapter(s.prevChapterId) }) { Text("上一章") }
                        }
                        if (s.nextChapterId != null) {
                            androidx.compose.material3.OutlinedButton(onClick = { vm.gotoChapter(s.nextChapterId) }) { Text("下一章") }
                        }
                    }
                }
                else -> {
                    // v27.5 性能修复：lambda 用 remember 缓存，避免 ReaderPager 因 lambda 不稳定而重组
                    val onPageChanged = remember(vm) { { it: Int ->
                        currentPage = it
                        vm.saveProgress(it)
                    } }
                    val onTap = remember { { uiVisible = !uiVisible } }
                    val onJumpNotice = remember(vm, jumpScope, jumpSnackbar) { { page: Int ->
                        // 页码从 0 开始，用户视角从 1 开始
                        currentPage = page
                        jumpScope.launch {
                            jumpSnackbar.showSnackbar("已自动跳转至第 ${page + 1} 页")
                        }
                        Unit
                    } }
                    ReaderPager(
                        images = s.imageFiles,
                        direction = direction,
                        initialPage = s.initialPage,
                        verticalListState = verticalListState,
                        horizontalPagerState = horizontalPagerState,
                        onPageChanged = onPageChanged,
                        onTap = onTap,
                        onJumpNotice = onJumpNotice,
                        // v27.5 #1 #3 #5
                        pinchZoom = pinchZoom,
                        tapZoneMode = tapZoneMode,
                        autoScroll = autoScroll,
                        autoScrollSpeed = autoScrollSpeed,
                        onZoneTap = { delta ->
                            // 横向阅读时点击区域翻页：delta=-1 上一页，+1 下一页
                            jumpToPage(currentPage + delta)
                        },
                        readerFontSize = readerFontSize,
                        readerLineSpacing = readerLineSpacing,
                        imageQuality = imageQuality,
                    )
                }
            }
            // v27.5 #5：夜间护眼滤镜叠加层（暖色半透明黄/橙色）。
            // 放在 Box 内最上层覆盖所有 reader 内容（loading/error/正常），
            // 不消费点击事件（pointerInput 不拦截），仅做颜色叠加。
            if (nightModeFilter) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            // 暖橙色 #FF8C00 透明叠加，强度由用户控制
                            Color(0xFFFF8C00).copy(alpha = nightModeFilterStrength.coerceIn(0f, 0.85f)),
                        ),
                )
            }
        }
    }
}

@Composable
private fun ReaderPager(
    images: List<String>,
    direction: ReaderDirection,
    initialPage: Int,
    verticalListState: LazyListState,
    horizontalPagerState: PagerState,
    onPageChanged: (Int) -> Unit,
    onTap: () -> Unit,
    onJumpNotice: (Int) -> Unit = {},
    // v27.5 新增
    pinchZoom: Boolean = true,
    tapZoneMode: TapZoneMode = TapZoneMode.THIRD_THIRD,
    autoScroll: Boolean = false,
    autoScrollSpeed: Float = 5f,
    onZoneTap: (Int) -> Unit = {}, // delta: -1=上一页, +1=下一页
    readerFontSize: Float = 16f,
    readerLineSpacing: Float = 1.5f,
    imageQuality: com.jmreader.data.local.ImageQuality = com.jmreader.data.local.ImageQuality.HIGH,
) {
    when (direction) {
        ReaderDirection.VERTICAL -> VerticalReader(
            images = images,
            initialPage = initialPage,
            listState = verticalListState,
            onPageChanged = onPageChanged,
            onTap = onTap,
            onJumpNotice = onJumpNotice,
            pinchZoom = pinchZoom,
            autoScroll = autoScroll,
            autoScrollSpeed = autoScrollSpeed,
            readerFontSize = readerFontSize,
            readerLineSpacing = readerLineSpacing,
            imageQuality = imageQuality,
        )
        ReaderDirection.HORIZONTAL_LR, ReaderDirection.HORIZONTAL_RL ->
            HorizontalReader(
                images = images,
                initialPage = initialPage,
                pagerState = horizontalPagerState,
                onPageChanged = onPageChanged,
                onTap = onTap,
                onJumpNotice = onJumpNotice,
                reverse = direction == ReaderDirection.HORIZONTAL_RL,
                pinchZoom = pinchZoom,
                tapZoneMode = tapZoneMode,
                onZoneTap = onZoneTap,
                readerFontSize = readerFontSize,
                readerLineSpacing = readerLineSpacing,
                imageQuality = imageQuality,
            )
    }
}

@Composable
private fun VerticalReader(
    images: List<String>,
    initialPage: Int,
    listState: LazyListState,
    onPageChanged: (Int) -> Unit,
    onTap: () -> Unit,
    onJumpNotice: (Int) -> Unit = {},
    pinchZoom: Boolean = true,
    autoScroll: Boolean = false,
    autoScrollSpeed: Float = 5f,
    readerFontSize: Float = 16f,
    readerLineSpacing: Float = 1.5f,
    imageQuality: com.jmreader.data.local.ImageQuality = com.jmreader.data.local.ImageQuality.HIGH,
) {
    // 进入章节时恢复到上次阅读位置（仅首次，initialPage/images 变化时触发一次）
    // 关键修复：之前条件是 initialPage > 0，导致切到无历史章节（initialPage=0）时不滚动，
    // ListState 保留旧章位置，用户从错误页码开始阅读。现在总是滚动到 initialPage（包括 0）。
    LaunchedEffect(initialPage, images) {
        if (images.isEmpty()) return@LaunchedEffect
        val target = initialPage.coerceIn(0, images.lastIndex)
        listState.scrollToItem(target)
        if (target > 0) onJumpNotice(target)
    }
    // 用 snapshotFlow + distinctUntilChanged 节流，避免滚动时频繁触发
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { onPageChanged(it) }
    }
    // v27.5 #8：自动滚动。每帧推进 px = speed * dt（按真实帧间隔，跟随系统刷新率）。
    // 用户手动滑动时 listState.scrollBy 仍可工作，但 telephoto zoomable 在缩放后会消费手势，
    // 这里是独立 LaunchedEffect，与用户手势并存：若用户暂停手指则会继续自动滚。
    // isUserScrolling 检测：用户正在拖动时短暂跳过一帧避免冲突。
    if (autoScroll && images.isNotEmpty()) {
        LaunchedEffect(autoScroll, autoScrollSpeed, listState) {
            // 等 600ms 再开始，避免与 initialPage 恢复滚动冲突
            kotlinx.coroutines.delay(600)
            // v27.5 卡顿修复：之前硬编码 delay(33)（~30fps）与用户反馈"不到30帧"完全吻合。
            // 改为 delay(16)（~60fps），并按真实帧间隔 dt 推进像素，保证速度恒定（speed 单位 = px/s）。
            // 即使在 120Hz 屏上，delay(16) 节拍约 60fps，但 dt 按真实时间计算，速度依然正确。
            // v27.5 稳定性加固：dt 用 coerceIn(0f, 0.1f) clamp，避免设备睡眠唤醒后
            // dt 巨大（如 60 秒）让 scrollBy 一帧跳过几百页触发 LazyColumn 越界。
            var lastMs = 0L
            while (true) {
                val nowMs = System.nanoTime() / 1_000_000
                if (lastMs != 0L && !listState.isScrollInProgress) {
                    val dtSec = ((nowMs - lastMs) / 1000f).coerceIn(0f, 0.1f)
                    listState.scrollBy(autoScrollSpeed * dtSec)
                }
                lastMs = nowMs
                kotlinx.coroutines.delay(16)
            }
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        // v27.5 稳定性加固：用 itemsIndexed + 复合 key，避免图片路径重复时
        // LazyColumn 抛 IllegalArgumentException: Key "xxx" was already used 崩溃
        itemsIndexed(images, key = { i, p -> "$i:$p" }) { _, path ->
            // 竖向滚动也用 telephoto zoomable：支持双指缩放 + 放大后拖拽平移。
            // telephoto 在未缩放（1x）时让滑动手势透传给 LazyColumn，缩放后消费拖拽做平移，
            // 不影响正常上下翻页。
            ReaderImage(path = path, onTap = onTap, pinchZoom = pinchZoom, modifier = Modifier.fillMaxWidth(), readerFontSize = readerFontSize, readerLineSpacing = readerLineSpacing, imageQuality = imageQuality)
        }
    }
}

@Composable
private fun HorizontalReader(
    images: List<String>,
    initialPage: Int,
    pagerState: PagerState,
    onPageChanged: (Int) -> Unit,
    onTap: () -> Unit,
    onJumpNotice: (Int) -> Unit = {},
    reverse: Boolean = false,
    pinchZoom: Boolean = true,
    tapZoneMode: TapZoneMode = TapZoneMode.THIRD_THIRD,
    onZoneTap: (Int) -> Unit = {},
    readerFontSize: Float = 16f,
    readerLineSpacing: Float = 1.5f,
    imageQuality: com.jmreader.data.local.ImageQuality = com.jmreader.data.local.ImageQuality.HIGH,
) {
    // 恢复上次阅读页，恢复后弹提示
    // 关键修复：同 VerticalReader，总是滚动到 initialPage（包括 0），避免切章遗留旧位置
    LaunchedEffect(initialPage, images) {
        if (images.isEmpty()) return@LaunchedEffect
        val target = initialPage.coerceIn(0, images.lastIndex)
        pagerState.scrollToPage(target)
        if (target > 0) onJumpNotice(target)
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { onPageChanged(it) }
    }
    // v27.5 #6：右→左阅读用 reverseLayout，pager 视觉上从最后一页开始
    //
    // v27.5 #7：点击区域翻页（仅 HORIZONTAL 模式）。
    // - THIRD_THIRD：左 1/3 = 上一页，中 1/3 = 切换 UI，右 1/3 = 下一页
    // - LEFT_RIGHT：左半 = 上一页，右半 = 下一页（中无 UI 切换，靠长按或菜单）
    // - DISABLED：纯 zoomable onClick 切换 UI
    // 注意：RL 方向时左/右语义翻转——左→下一页、右→上一页，符合右→左阅读直觉。
    var pagerWidthPx: Int by remember { mutableStateOf(0) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onGloballyPositioned { pagerWidthPx = it.size.width },
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            reverseLayout = reverse,
            beyondViewportPageCount = 1,
            key = { it },
        ) { page ->
            // 越界保护：page 理论上不会越界，但加保护避免极端情况闪退
            val path = images.getOrNull(page) ?: return@HorizontalPager
            ReaderImage(path = path, onTap = onTap, pinchZoom = pinchZoom, modifier = Modifier.fillMaxSize(), readerFontSize = readerFontSize, readerLineSpacing = readerLineSpacing, imageQuality = imageQuality)
        }
        // 点击区域覆盖层：拦截单击，缩放/拖拽由 zoomable 在下层处理
        if (tapZoneMode != TapZoneMode.DISABLED && pagerWidthPx > 0 && images.size > 1) {
            val w = pagerWidthPx
            val third = w / 3
            // 左 1/3 / 右 1/3 区
            // v27.5 稳定性加固（high 修复）：pointerInput key 必须含 images.size。
            // 之前 key 只有 (tapZoneMode, reverse, w)，切章后这些不变 → pointerInput 块不重启，
            // 内部 onZoneTap 仍捕获旧的 jumpToPage → 旧的 totalPages。从 10 页章节切到 5 页章节后，
            // 翻第 4 页点右 1/3 区域 jumpToPage(4+1)，旧 totalPages=10 把它 clamp 到 5，越界跳页。
            // 加入 images.size 作为 key：切章时 size 变化 → pointerInput 重启 → 捕获新的 onZoneTap/totalPages。
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(tapZoneMode, reverse, w, images.size) {
                        detectTapGestures(
                            onTap = { offset ->
                                val x = offset.x.toInt()
                                val delta = when (tapZoneMode) {
                                    TapZoneMode.THIRD_THIRD -> when {
                                        x < third -> -1
                                        x > w - third -> 1
                                        else -> 0 // 中间区：切换 UI
                                    }
                                    TapZoneMode.LEFT_RIGHT -> if (x < w / 2) -1 else 1
                                    TapZoneMode.DISABLED -> 0
                                }
                                if (delta == 0) {
                                    onTap()
                                } else {
                                    // RL 方向：左右翻页语义翻转
                                    onZoneTap(if (reverse) -delta else delta)
                                }
                            },
                        )
                    },
            )
        }
    }
}

/**
 * 单张阅读图片：远程 URL 或本地文件。
 *
 * 统一用 AsyncImage + telephoto zoomable 修饰符，竖滑/横滑模式共用同一套逻辑：
 * - 双指缩放（pinch-to-zoom），maxZoomFactor=5f，可自由放大
 * - 放大后拖拽平移查看细节（telephoto 自动处理与 LazyColumn/Pager 的手势冲突）
 * - 双击在 1x/maxZoom 间切换
 * - 单击切换顶/底栏显隐（通过 zoomable 的 onClick 回调，避免 pointerInput 冲突）
 *
 * 之前横滑用 ZoomableAsyncImage 出现全黑：该组件与 JmImageFetcher 的 SourceResult
 * 配合时有渲染问题。改用 AsyncImage + zoomable 修饰符分离图片加载与缩放逻辑，更可靠。
 * 本地文件（已下载）也走同一路径，AsyncImage 对 File model 有原生支持。
 */
@Composable
private fun ReaderImage(
    path: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    // v27.5 #1：双指缩放开关。关闭时 maxZoomFactor=1f，等价于禁用缩放，
    // telephoto 仍保留 onClick 单击回调用于切换 UI。
    pinchZoom: Boolean = true,
    // v27.6：阅读器文字大小/行距设置，应用到错误提示文本
    readerFontSize: Float = 16f,
    readerLineSpacing: Float = 1.5f,
    // v27.6：图片质量档位，控制解码尺寸
    imageQuality: com.jmreader.data.local.ImageQuality = com.jmreader.data.local.ImageQuality.HIGH,
) {
    val ctx = LocalContext.current
    val model = remember(path, imageQuality) {
        when {
            // v27.5 性能修复：移除 crossfade(true)。
            // 之前每张图加载完都跑 300ms alpha 淡入动画，与滑动 frame 抢主线程时间片，
            // 阅读器翻一章 30-50 张图就是 30-50 个并发动画，是滑动卡顿主因。
            // 全局 Coil 配置未开 crossfade（CoilSetup.kt 注释明确说明列表滚动场景禁用），
            // 这里也不该单独开启。
            //
            // v27.6：根据 imageQuality 设置解码尺寸
            // - ORIGINAL: 原图解码，清晰度最高但内存最大
            // - HIGH: 按 View 测量尺寸解码（默认行为，平衡）
            // - MEDIUM: 限制长边 ~1350px，省约 44% 内存
            // - LOW: 限制长边 ~900px，省约 75% 内存
            path.startsWith("http") -> {
                val builder = coil.request.ImageRequest.Builder(ctx).data(path)
                when (imageQuality) {
                    com.jmreader.data.local.ImageQuality.ORIGINAL -> builder.size(coil.size.Size.ORIGINAL)
                    com.jmreader.data.local.ImageQuality.HIGH -> {} // 按 View 测量，不指定
                    com.jmreader.data.local.ImageQuality.MEDIUM -> builder.size(900, 1350)
                    com.jmreader.data.local.ImageQuality.LOW -> builder.size(600, 900)
                }
                builder.build()
            }
            path.startsWith("content://") -> {
                val builder = coil.request.ImageRequest.Builder(ctx).data(android.net.Uri.parse(path))
                when (imageQuality) {
                    com.jmreader.data.local.ImageQuality.ORIGINAL -> builder.size(coil.size.Size.ORIGINAL)
                    com.jmreader.data.local.ImageQuality.HIGH -> {}
                    com.jmreader.data.local.ImageQuality.MEDIUM -> builder.size(900, 1350)
                    com.jmreader.data.local.ImageQuality.LOW -> builder.size(600, 900)
                }
                builder.build()
            }
            else -> File(path)
        }
    }
    var imgState by remember(path) {
        mutableStateOf<coil.compose.AsyncImagePainter.State>(coil.compose.AsyncImagePainter.State.Empty)
    }
    // v27.5 卡顿修复：pinchZoom=false 时不挂 telephoto zoomable——
    // 之前即使 maxZoomFactor=1f，每个 reader item 仍注册完整手势检测器 + graphicsLayer，
    // 满屏 20-30 张图每帧重算 transforms，是阅读器滑动卡顿主因之一。
    // v27.5 稳定性加固：补全 onDoubleTap / onLongPress，避免用户感知"卡死"。
    // 关闭缩放时仅用 pointerInput + detectTapGestures 处理单击切 UI，开销几乎为零。
    val imageModifier = if (pinchZoom) {
        val zoomState = me.saket.telephoto.zoomable.rememberZoomableState(
            zoomSpec = me.saket.telephoto.zoomable.ZoomSpec(maxZoomFactor = 5f)
        )
        Modifier
            .fillMaxWidth()
            .zoomable(
                state = zoomState,
                onClick = { onTap() },
            )
    } else {
        Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { /* 双击也切 UI，与单击等价；不做缩放 */ onTap() },
                    onLongPress = { /* 长按也切 UI，避免用户长按无响应误判卡死 */ onTap() },
                )
            }
    }
    Box(modifier = modifier.background(Color(0xFF1A1A1A)), contentAlignment = Alignment.Center) {
        coil.compose.AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            onState = { imgState = it },
            modifier = imageModifier,
        )
        when (val s = imgState) {
            is coil.compose.AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(36.dp))
            }
            is coil.compose.AsyncImagePainter.State.Error -> {
                com.jmreader.core.Logger.w("Reader", "图片加载失败: $path, ${com.jmreader.core.Logger.brief(s.result.throwable)}")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("加载失败", color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontSize = readerFontSize.sp, lineHeight = (readerFontSize * readerLineSpacing).sp))
                    Text(
                        text = com.jmreader.core.Logger.friendlyError(com.jmreader.core.Logger.brief(s.result.throwable)),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = readerFontSize.sp, lineHeight = (readerFontSize * readerLineSpacing).sp),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            else -> {}
        }
    }
}

/**
 * v27.6 音量键翻页桥接器：让 MainActivity.onKeyDown 能触发 ReaderScreen 的翻页。
 *
 * ReaderScreen 进入时通过 DisposableEffect 注册回调，离开时清除。
 * MainActivity.onKeyDown 在音量键按下时调用 [handleVolumeKey]。
 */
object ReaderVolumeKeyBridge {
    @Volatile
    var callback: ((isUp: Boolean) -> Unit)? = null

    /** 返回 true 表示已消费（阻止系统调音量），false 表示不消费（让系统处理） */
    fun handleVolumeKey(isUp: Boolean): Boolean {
        val cb = callback ?: return false
        return try {
            cb(isUp)
            true
        } catch (_: Throwable) {
            false
        }
    }
}

/**
 * v27.6 线路测速切换 Sheet：在阅读器内一键测速所有 API 域名并切换到最快的。
 *
 * 复用 JmDirectClient.testAllDomains() 并发测速（Semaphore 4 限流）。
 * 测速完成后可点击"自动选最快"或手动选择某条线路。
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun LineSpeedTestSheet(
    container: com.jmreader.data.AppContainer,
    onDismiss: () -> Unit,
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
    // 测速结果：域名 → (延迟ms, 错误信息)
    var results by remember { mutableStateOf<Map<String, Pair<Long?, String?>>>(emptyMap()) }
    var testing by remember { mutableStateOf(false) }
    val currentDomain = remember { container.directClient.currentDomain() }
    val domains = remember { container.directClient.apiDomainList() }

    // 进入时自动测速一次
    androidx.compose.runtime.LaunchedEffect(Unit) {
        testing = true
        try {
            val rs = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                container.directClient.testAllDomains()
            }
            results = rs.associate { it.first to (it.second to it.third) }
        } catch (_: Throwable) {}
        testing = false
    }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "线路测速",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                // 自动选最快按钮
                androidx.compose.material3.TextButton(
                    onClick = {
                        // v28.0 安全性修复：避免 first!! 强解，用 filterNotNull
                        val fastest = results.entries
                            .mapNotNull { entry -> entry.value.first?.let { latency -> entry.key to latency } }
                            .minByOrNull { it.second }
                        if (fastest != null) {
                            container.directClient.selectDomain(fastest.first)
                            onDismiss()
                        }
                    },
                    enabled = !testing && results.any { it.value.first != null },
                ) { Text("自动选最快") }
                // 重新测速按钮
                androidx.compose.material3.TextButton(
                    onClick = {
                        scope.launch {
                            testing = true
                            results = emptyMap()
                            try {
                                val rs = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    container.directClient.testAllDomains()
                                }
                                results = rs.associate { it.first to (it.second to it.third) }
                            } catch (_: Throwable) {}
                            testing = false
                        }
                    },
                    enabled = !testing,
                ) { Text(if (testing) "测速中…" else "重新测速") }
            }
            if (testing && results.isEmpty()) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("正在测速 ${domains.size} 条线路…", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            // 域名列表
            domains.forEach { host ->
                val res = results[host]
                val isSelected = host == currentDomain
                val latency = res?.first
                val err = res?.second
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (isSelected) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                            else Modifier
                        )
                        .clickable {
                            container.directClient.selectDomain(host)
                            onDismiss()
                        }
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            host,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                        Text(
                            when {
                                testing && res == null -> "等待中…"
                                latency != null -> "${latency} ms"
                                err != null -> "失败：$err"
                                else -> "未测速"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                latency != null && latency < 500 -> Color(0xFF4CAF50)
                                latency != null -> Color(0xFFFF9800)
                                err != null -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = "当前线路",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                androidx.compose.foundation.layout.Spacer(Modifier.height(2.dp))
            }
        }
    }
}
