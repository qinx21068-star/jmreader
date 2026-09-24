package com.jmreader.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmreader.data.AppContainer
import com.jmreader.data.dto.ComicBriefDto
import com.jmreader.data.dto.ComicDetailDto
import com.jmreader.data.repository.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * DetailViewModel - 漫画详情页 ViewModel
 * 
 * 使用 Hilt Assisted Injection 支持运行时参数 (comicId)
 * 
 * Phase 1.1 ViewModel 迁移: 4/13 完成
 * - 从 DetailScreen.kt 提取为独立文件
 * - 添加 @HiltViewModel 注解
 * - 使用 @AssistedInject 支持 comicId 参数注入
 * - 保留所有业务逻辑完整性
 */
@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    private val container: AppContainer,
    @Assisted private val comicId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    /** 当前已屏蔽的 tag 集合（实时同步 DataStore，用于标签 chip 显示已屏蔽状态）。 */
    val blockedTags: StateFlow<Set<String>> = container.blockedTagsStore.tags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptySet())

    /** 当前已屏蔽的作者集合（实时同步 DataStore，用于作者名旁显示屏蔽状态）。 */
    val blockedAuthors: StateFlow<Set<String>> = container.blockedTagsStore.authors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptySet())

    // v27.5 #12 相似推荐：用首个 tag 作为种子搜索，结果过滤掉当前本子
    private val _relatedComics = MutableStateFlow<List<ComicBriefDto>>(emptyList())
    val relatedComics: StateFlow<List<ComicBriefDto>> = _relatedComics.asStateFlow()

    // v27.5 #13 作者其它作品：用作者名搜索，结果过滤掉当前本子
    private val _authorWorks = MutableStateFlow<List<ComicBriefDto>>(emptyList())
    val authorWorks: StateFlow<List<ComicBriefDto>> = _authorWorks.asStateFlow()

    init {
        load()
        // 关键修复：监听 HistoryStore 变化，用户从阅读器返回详情页时
        // lastReadChapterId 实时更新，按钮从"开始阅读"变为"继续阅读"。
        // v27.5 稳定性加固：collect 内任何异常（DataStore IOException 等）若不捕获会冒泡到
        // Thread.uncaughtExceptionHandler → CrashHandler → 杀进程。整个 collect 包 try-catch，
        // 单次 emit 失败不影响后续 emit 处理。
        viewModelScope.launch {
            try {
                container.historyStore.items.collect { list ->
                    try {
                        val last = list.firstOrNull { it.comic.id == comicId }?.chapterId
                        if (last != _state.value.lastReadChapterId) {
                            _state.value = _state.value.copy(lastReadChapterId = last)
                        }
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        com.jmreader.core.Logger.w("Detail", "historyStore collect 处理失败: ${com.jmreader.core.Logger.brief(e)}")
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Throwable) {
                com.jmreader.core.Logger.w("Detail", "historyStore collect 失败: ${com.jmreader.core.Logger.brief(e)}")
            }
        }
    }

    // v27.13：跟踪 load Job，重试时取消旧请求，避免叠加多批 comicDetail + relatedComics + authorWorks
    private var loadJob: kotlinx.coroutines.Job? = null

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                // v27.13：详情请求与本地 store 加载并行，减少串行等待。
                // 之前 /album 完成后才串行 ensureLoaded + isFavoriteAwait，
                // 冷启动时本地 JSON 解析几十~几百 ms 叠加在后面。
                val detailDeferred = async { container.repository.comicDetail(comicId) }
                val historyDeferred = async {
                    container.historyStore.ensureLoaded()
                    container.historyStore.items.value
                }
                val favDeferred = async { container.favoritesStore.isFavoriteAwait(comicId) }
                val settingsDeferred = async {
                    runCatching { container.settingsStore.settings.first() }.getOrNull()
                }

                when (val r = detailDeferred.await()) {
                    is Resource.Success -> {
                        val historyList = historyDeferred.await()
                        val lastChapter = historyList
                            .firstOrNull { it.comic.id == comicId }?.chapterId
                        _state.value = DetailUiState(
                            loading = false,
                            detail = r.data,
                            isFavorite = favDeferred.await(),
                            lastReadChapterId = lastChapter,
                        )
                        // 记录浏览历史：打开详情页即记录，不需阅读
                        val brief = ComicBriefDto(
                            r.data.id, r.data.name, r.data.author,
                            r.data.tags, r.data.cover,
                        )
                        // v27.5 #30：隐身模式下不记录浏览历史（与 UI 副标题承诺一致）
                        val incognito = settingsDeferred.await()?.incognito ?: false
                        if (!incognito) {
                            container.browseHistoryStore.upsert(brief)
                        }
                        // v27.5 #12 #13：加载相似推荐 + 作者其它作品（失败不阻塞详情页）
                        loadRelatedComics(r.data)
                        loadAuthorWorks(r.data)
                    }
                    is Resource.Error -> {
                        com.jmreader.core.Logger.e("Detail", "加载详情失败: ${r.message}")
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
                com.jmreader.core.Logger.e("Detail", "load 异常", e)
                _state.value = _state.value.copy(
                    loading = false,
                    error = com.jmreader.core.Logger.friendlyError(com.jmreader.core.Logger.brief(e)),
                )
            }
        }
    }

    fun toggleFavorite(onResult: (String) -> Unit = {}) {
        val detail = _state.value.detail ?: return
        val brief = ComicBriefDto(detail.id, detail.name, detail.author, detail.tags, detail.cover)
        viewModelScope.launch {
            try {
                val now = container.favoritesStore.toggle(brief)
                _state.value = _state.value.copy(isFavorite = now)
                onResult(if (now) "已加入收藏" else "已取消收藏")
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Throwable) {
                com.jmreader.core.Logger.e("Detail", "收藏操作失败", e)
                onResult("收藏操作失败")
            }
        }
    }

    /** 屏蔽指定 tag（长按标签触发）。 */
    fun blockTag(tag: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                container.blockedTagsStore.addTag(tag)
                onResult("已屏蔽标签：$tag")
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Throwable) {
                onResult("屏蔽失败：${com.jmreader.core.Logger.brief(e)}")
            }
        }
    }

    /** 取消屏蔽指定 tag（长按已屏蔽标签触发）。 */
    fun unblockTag(tag: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                container.blockedTagsStore.removeTag(tag)
                onResult("已取消屏蔽：$tag")
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Throwable) {
                onResult("取消屏蔽失败：${com.jmreader.core.Logger.brief(e)}")
            }
        }
    }

    /** 屏蔽指定作者（详情页作者名旁按钮触发）。 */
    fun blockAuthor(author: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                container.blockedTagsStore.addAuthor(author)
                onResult("已屏蔽作者：$author")
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Throwable) {
                onResult("屏蔽失败：${com.jmreader.core.Logger.brief(e)}")
            }
        }
    }

    /** 取消屏蔽指定作者。 */
    fun unblockAuthor(author: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                container.blockedTagsStore.removeAuthor(author)
                onResult("已取消屏蔽作者：$author")
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Throwable) {
                onResult("取消屏蔽失败：${com.jmreader.core.Logger.brief(e)}")
            }
        }
    }

    fun download(onResult: (String) -> Unit) {
        val detail = _state.value.detail ?: return
        val brief = ComicBriefDto(detail.id, detail.name, detail.author, detail.tags, detail.cover)
        try {
            container.downloadManager.enqueue(brief, detail)
            onResult("已加入下载队列")
        } catch (e: Throwable) {
            com.jmreader.core.Logger.e("Detail", "加入下载失败", e)
            onResult("下载失败：${com.jmreader.core.Logger.brief(e)}")
        }
    }

    /**
     * v27.5 #12 相似推荐：用首个非空 tag 作为种子调 search 接口。
     * 取前 12 条，过滤掉当前本子。失败静默（不阻塞详情页）。
     *
     * v27.7：列表 API 返回粗 tags，有 tag 屏蔽规则时拉取详情页真实 tags 再过滤，
     * 确保被屏蔽的具体 tag 不泄漏（如"NTR"被屏蔽但列表只返回"同人"）。
     */
    private fun loadRelatedComics(detail: ComicDetailDto) {
        val seed = detail.tags.firstOrNull { it.isNotBlank() } ?: return
        viewModelScope.launch {
            try {
                when (val r = container.repository.search(seed, page = 1, order = "latest")) {
                    is Resource.Success -> {
                        val (tags, names, authors) = container.blockedTagsStore.allRules.first()
                        val rules = container.blockedTagsStore.normalizeRules(tags, names, authors)
                        _relatedComics.value = filterWithRealTags(
                            r.data.items.filter { it.id != detail.id },
                            rules,
                        ).take(12)
                    }
                    else -> {}
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Throwable) { /* 静默：相似推荐失败不影响详情页 */ }
        }
    }

    /**
     * v27.5 #13 作者其它作品：用作者名调 search 接口。
     * 取前 12 条，过滤掉当前本子。失败静默。
     *
     * v27.7：同 [loadRelatedComics]，有 tag 规则时拉取真实 tags 再过滤。
     */
    private fun loadAuthorWorks(detail: ComicDetailDto) {
        val author = detail.author?.takeIf { it.isNotBlank() } ?: return
        viewModelScope.launch {
            try {
                when (val r = container.repository.search(author, page = 1, order = "latest")) {
                    is Resource.Success -> {
                        val (tags, names, authors) = container.blockedTagsStore.allRules.first()
                        val rules = container.blockedTagsStore.normalizeRules(tags, names, authors)
                        _authorWorks.value = filterWithRealTags(
                            r.data.items.filter { it.id != detail.id },
                            rules,
                        ).take(12)
                    }
                    else -> {}
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Throwable) { /* 静默：作者其它作品失败不影响详情页 */ }
        }
    }

    /**
     * v27.7：用详情页真实 tags 过滤列表条目。
     * - 无 tag 规则：name/author 立即判定，无需补全
     * - 有 tag 规则：并发拉取详情页真实 tags 再判定，补全失败的条目 fail-closed 隐藏
     */
    private suspend fun filterWithRealTags(
        items: List<ComicBriefDto>,
        rules: com.jmreader.data.local.BlockedTagsStore.NormalizedRules,
    ): List<ComicBriefDto> {
        if (rules.tags.isEmpty()) {
            // 无 tag 规则：name/author 可立即准确判定
            return items.filterNot {
                container.blockedTagsStore.isBlocked(it.tags, it.name, it.author, rules)
            }
        }
        // 有 tag 规则：并发拉取详情页真实 tags（最多 20 条候选）
        val candidates = items.take(20)
        val enriched = coroutineScope {
            candidates.map { comic ->
                async {
                    try {
                        val detail = kotlinx.coroutines.withTimeoutOrNull(5000L) {
                            container.repository.comicDetail(comic.id)
                                .let { (it as? Resource.Success)?.data }
                        }
                        val realTags = detail?.tags?.takeIf { it.isNotEmpty() } ?: comic.tags
                        comic.copy(tags = realTags) to
                            !container.blockedTagsStore.isBlocked(realTags, comic.name, comic.author, rules)
                    } catch (_: Throwable) {
                        // fail-closed：补全失败则隐藏
                        comic to false
                    }
                }
            }.awaitAll()
        }
        return enriched.filter { it.second }.map { it.first }
    }

    @AssistedFactory
    interface Factory {
        fun create(comicId: String): DetailViewModel
    }
}

/**
 * DetailUiState - 详情页 UI 状态
 */
data class DetailUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val detail: ComicDetailDto? = null,
    val isFavorite: Boolean = false,
    val lastReadChapterId: String? = null,
)
