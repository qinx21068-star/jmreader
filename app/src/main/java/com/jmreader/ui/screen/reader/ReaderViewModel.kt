package com.jmreader.ui.screen.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmreader.core.Logger
import com.jmreader.data.AppContainer
import com.jmreader.data.dto.ComicBriefDto
import com.jmreader.data.dto.ComicDetailDto
import com.jmreader.data.repository.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 阅读器 ViewModel
 * 
 * v28.0 已迁移到 Hilt，使用 Assisted Injection 支持运行时参数
 */
data class ReaderUiState(
    val loading: Boolean = true,
    val imageFiles: List<String> = emptyList(),
    val fromLocal: Boolean = false,
    val title: String = "",
    val prevChapterId: String? = null,
    val nextChapterId: String? = null,
    /** 进入章节时应恢复到的页码索引（来自上次阅读历史）。null=从头开始。 */
    val initialPage: Int = 0,
    val error: String? = null,
)

@HiltViewModel(assistedFactory = ReaderViewModel.Factory::class)
class ReaderViewModel @AssistedInject constructor(
    private val container: AppContainer,
    @Assisted private val comicId: String,
    @Assisted initialChapterId: String,
) : ViewModel() {

    private var chapterId = initialChapterId
    private var cachedDetail: ComicDetailDto? = null
    /** 节流：相同页码不重复保存 */
    private var lastSavedPage = -1
    /** 进度保存协程：debounce 期间新页码来时取消旧的，避免高频写盘 */
    private var saveJob: Job? = null
    /** 当前加载协程：连点下一章/重试时取消旧的，避免竞态覆盖章节状态 */
    private var loadJob: Job? = null
    /** v27.13：预加载下一章协程：切章时取消旧的，避免连点切章重复请求 chapterImages */
    private var preloadJob: Job? = null
    /** 切章时短暂禁用 saveProgress，避免旧章节的 snapshotFlow 末次发射写入新章进度 */
    private var saveEnabled: Boolean = true

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        // 关键修复：取消上一次未完成的 load，避免连点下一章时旧请求覆盖新章节 state
        loadJob?.cancel()
        // 同步置 loading=true 并禁用保存：让 UI 立即卸载旧 Pager（取消其 LaunchedEffect
        // 和 snapshotFlow collect），避免切章瞬间旧章页码串写到新章进度。
        saveEnabled = false
        _state.value = _state.value.copy(loading = true, error = null, imageFiles = emptyList())
        loadJob = viewModelScope.launch {
            com.jmreader.core.Logger.d("Reader", "load comic=$comicId chapter=$chapterId")
            try {
                // 读取设置：是否进度记忆到具体页
                val settings = container.settingsStore.settings.first()
                val rememberPageLevel = settings.rememberPageLevel

                // 查历史，恢复上次阅读页码
                // 关键修复（Bug 33）：之前用 withTimeoutOrNull(2000){ items.first{it.isNotEmpty()} }。
                // 但 StateFlow 值未变化时不发射：用户若无历史（首次安装/清空过），
                // init 加载完成后 _items.value 仍是 emptyList()（与初值相同）→ 永不发射 → 卡 2s 才 fallback。
                // 首次用户每打开一个章节/详情页都要白等 2 秒。改用 ensureLoaded() 等加载完成即可。
                container.historyStore.ensureLoaded()
                val historyList = container.historyStore.items.value
                val histPage = if (rememberPageLevel) {
                    historyList.firstOrNull { it.comic.id == comicId && it.chapterId == chapterId }?.page ?: 0
                } else 0
                val safeInitial = if (histPage > 0) histPage else 0
                com.jmreader.core.Logger.i("Reader", "恢复进度: $comicId/$chapterId → page=$safeInitial (rememberPageLevel=$rememberPageLevel)")

                // 优先读本地离线图片
                val local = container.downloadManager.listLocalFiles(comicId, chapterId)
                if (local.isNotEmpty()) {
                    val (prev, next) = computePrevNext()
                    val chTitle = cachedDetail?.chapters?.firstOrNull { it.id == chapterId }?.title
                    _state.value = ReaderUiState(
                        loading = false,
                        // v27.6：listLocalFiles 现在返回 List<String>（路径或 content:// URI）
                        imageFiles = local,
                        fromLocal = true,
                        title = chTitle ?: chapterId,
                        prevChapterId = prev,
                        nextChapterId = next,
                        initialPage = safeInitial,
                    )
                    com.jmreader.core.Logger.i("Reader", "本地图片 ${local.size} 张")
                    saveEnabled = true
                    // v27.5 #2：预加载下一章图片列表（仅触发章节接口，不下载图片本体）
                    if (settings.preloadNextChapter) preloadNextChapter(next)
                    return@launch
                }
                val serverUrl = settings.serverUrl
                val useBackend = serverUrl.isNotBlank()
                when (val r = container.repository.chapterImages(chapterId)) {
                    is Resource.Success -> {
                        val (prev, next) = computePrevNext()
                        // 直连模式：图片 URL 已带 jm_sid 标记，JmImageFetcher 会自动解密
                        // 后端模式：套后端代理 URL 解密
                        val imgs = if (useBackend) {
                            r.data.images.map { proxiedImageUrl(serverUrl, it) }
                        } else {
                            r.data.images
                        }
                        _state.value = ReaderUiState(
                            loading = false,
                            imageFiles = imgs,
                            fromLocal = false,
                            title = r.data.title ?: chapterId,
                            prevChapterId = prev,
                            nextChapterId = next,
                            initialPage = safeInitial,
                        )
                        com.jmreader.core.Logger.i("Reader", "远程图片 ${r.data.images.size} 张, 模式=${if (useBackend) "后端" else "直连"}, 恢复页=$safeInitial")
                        saveEnabled = true
                        // v27.5 #2：预加载下一章图片列表，避免翻章白屏
                        if (settings.preloadNextChapter) preloadNextChapter(next)
                    }
                    is Resource.Error -> {
                        com.jmreader.core.Logger.e("Reader", "加载章节失败: ${r.message}")
                        _state.value = _state.value.copy(
                            loading = false,
                            error = com.jmreader.core.Logger.friendlyError(r.message),
                        )
                    }
                    Resource.Loading -> {}
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Throwable) {
                com.jmreader.core.Logger.e("Reader", "load 异常", e)
                _state.value = _state.value.copy(
                    loading = false,
                    error = com.jmreader.core.Logger.friendlyError(com.jmreader.core.Logger.brief(e)),
                )
            }
        }
    }

    /**
     * v27.5 #2：预加载下一章图片列表。
     * v27.13：增强为预下载前 N 张图片本体到 Coil 磁盘缓存。
     * 之前只预热 URL 列表，用户翻到下一章时每张图仍要现下载+解密。
     * 现在拿到 URL 后用 Coil enqueue 预取前 5 张，翻章时直接命中磁盘缓存。
     *
     * 失败静默：预加载仅是优化，不应影响当前章节阅读。
     */
    private fun preloadNextChapter(nextId: String?) {
        if (nextId == null) return
        // v27.13：取消上一次预加载，避免连点切章重复请求 chapterImages
        preloadJob?.cancel()
        preloadJob = viewModelScope.launch {
            try {
                com.jmreader.core.Logger.d("Reader", "预加载下一章: $nextId")
                val r = container.repository.chapterImages(nextId)
                val urls = (r as? Resource.Success)?.data?.images ?: return@launch
                if (urls.isEmpty()) return@launch
                // v27.13：预取前 5 张到 Coil 磁盘缓存
                val ctx = container.applicationContext
                val loader = coil.Coil.imageLoader(ctx)
                val prefetchCount = minOf(5, urls.size)
                for (i in 0 until prefetchCount) {
                    val req = coil.request.ImageRequest.Builder(ctx)
                        .data(urls[i])
                        .build()
                    loader.enqueue(req)
                }
                com.jmreader.core.Logger.d("Reader", "预取下一章 $prefetchCount 张图片到磁盘缓存")
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Throwable) {
                // 静默：预加载失败不影响当前阅读
            }
        }
    }

    fun gotoChapter(id: String) {
        if (id == chapterId) return
        // 关键修复（Bug 32）：切章前必须取消挂起的 saveJob。
        // saveProgress 用 delay(800) 做 debounce，闭包内 chapterId / state.value.title 是按引用读取执行时值。
        // 若不取消：用户在章节 A 第 5 页触发 saveJob → 800ms 内切到章节 B → chapterId 变 "B"、
        // state.value.title 还是 A 的标题（load 异步未完成）→ delay 到期 → 写入 historyStore.upsert(brief, "B", "A标题", 5)，
        // 把章节 B 的历史污染成第 5 页 + A 的标题。下次打开 B 被跳到第 5 页。
        saveJob?.cancel()
        chapterId = id
        lastSavedPage = -1
        load()
    }

    private suspend fun computePrevNext(): Pair<String?, String?> {
        val detail = cachedDetail ?: when (val r = container.repository.comicDetail(comicId)) {
            is Resource.Success -> r.data
            else -> return null to null
        }
        cachedDetail = detail
        val idx = detail.chapters.indexOfFirst { it.id == chapterId }
        if (idx < 0) return null to null
        return detail.chapters.getOrNull(idx - 1)?.id to detail.chapters.getOrNull(idx + 1)?.id
    }

    /**
     * 保存阅读进度。
     *
     * 关键修复：
     * 1. 不再重新请求详情（避免每次滚动触发网络请求 → 卡顿/崩溃）。
     * 2. 切章期间 [saveEnabled]=false，避免旧章节 snapshotFlow 末次发射污染新章进度。
     * 3. debounce 800ms：用户快速 fling 经过几十页时不会触发几十次写盘。
     *    新页码来时取消旧 saveJob，仅最后一次落盘。
     */
    fun saveProgress(page: Int) {
        if (!saveEnabled) return
        if (page == lastSavedPage) return
        lastSavedPage = page
        saveJob?.cancel()
        // 关键修复（Bug 32）：捕获快照值，避免 delay 期间 chapterId/title 被切章改写后串写到新章。
        val snapChapterId = chapterId
        val snapTitle = state.value.title
        saveJob = viewModelScope.launch {
            delay(800)
            // v27.5 #30：隐身模式下不记录阅读进度（与 UI 副标题承诺一致）
            // runCatching 容错：DataStore 极端异常时默认 incognito=false（继续保存进度，更安全）
            val incognito = runCatching {
                container.settingsStore.settings.first().incognito
            }.getOrDefault(false)
            if (incognito) return@launch
            try {
                val detail = cachedDetail
                if (detail != null) {
                    val brief = ComicBriefDto(detail.id, detail.name, detail.author, detail.tags, detail.cover)
                    container.historyStore.upsert(brief, snapChapterId, snapTitle, page)
                }
                // 关键修复（Bug 44）：移除 setReadingPosition 调用。
                // lastComicId/lastChapterId/lastPageIndex 三个字段全工程无任何读取方
                // （进度恢复走 HistoryStore 按 comicId+chapterId 双键查），
                // 但每次翻页 debounce 都触发一次 DataStore 写盘 + flow re-emit，
                // 导致订阅 settings 的 HomeScreen/SearchScreen 等重组，浪费电量与性能。
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Throwable) {
                com.jmreader.core.Logger.w("Reader", "保存进度失败: ${com.jmreader.core.Logger.brief(e)}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // VM 销毁时（离开阅读器）立即落盘最后一次进度，不等待 delay
        saveJob?.cancel()
        // 用一个新的同步协程立即写入，避免 debounce 期间 VM 销毁丢失最后一次进度
        val page = lastSavedPage
        val detail = cachedDetail
        val ch = chapterId
        val title = state.value.title
        if (page >= 0 && detail != null) {
            // 用 appScope 替代反模式 GlobalScope：appScope 生命周期与 App 进程一致，
            // 比 viewModelScope 存活更久，进程退出前有机会完成落盘。
            // v27.5 稳定性加固：appScope 已加 CrashHandler.coroutineHandler 兜底，
            // 这里 try/catch 是双重保险，并把异常打日志（之前静默吞无任何记录）。
            container.appScope.launch {
                try {
                    // v27.5 #30：隐身模式下不记录阅读进度
                    // runCatching 容错：异常时默认 false（继续保存，避免丢失进度）
                    val incognito = runCatching {
                        container.settingsStore.settings.first().incognito
                    }.getOrDefault(false)
                    if (incognito) return@launch
                    val brief = ComicBriefDto(detail.id, detail.name, detail.author, detail.tags, detail.cover)
                    container.historyStore.upsert(brief, ch, title, page)
                    // Bug 44：setReadingPosition 已移除（死写入，详见 saveProgress 注释）
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    com.jmreader.core.Logger.w("Reader", "onCleared 保存进度失败: ${com.jmreader.core.Logger.brief(e)}")
                }
            }
        }
    }
}


    @AssistedFactory
    interface Factory {
        fun create(comicId: String, initialChapterId: String): ReaderViewModel
    }
}
