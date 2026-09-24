package com.jmreader.ui.screen.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.jmreader.core.Logger
import com.jmreader.data.AppContainer
import com.jmreader.data.dto.ComicBriefDto
import com.jmreader.data.repository.Resource
import com.jmreader.ui.viewmodel.BaseListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 搜索页 ViewModel
 * 
 * 支持：
 * - 普通关键词搜索（带分页）
 * - 批量 JM 号搜索（多个 ID 用空格/逗号分隔）
 * - 排序切换（最新/观看/评论/图片数）
 * - 搜索历史记录
 * 
 * v28.0 Phase 1.1 已迁移到 Hilt：
 * - 使用 @HiltViewModel 注解
 * - 构造函数注入 AppContainer
 * - 保持继承 BaseListViewModel(container)（渐进式策略）
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val container: AppContainer,
) : BaseListViewModel(container) {
    
    var query by mutableStateOf("")
        private set
    var order by mutableStateOf("latest")
        private set

    private var debounce: Job? = null
    /** v27.14：当前是否处于批量搜索模式（用于阻止 loadMore 触发分页）。 */
    private var batchMode: Boolean = false
    /**
     * v27.14：批量搜索的当前 Job。新搜索启动前取消旧 Job，
     * 避免旧 searchBatch 完成后调用 setBatchResult 覆盖新结果（竞态）。
     */
    private var searchBatchJob: Job? = null

    fun onQueryChange(q: String) {
        query = q
        debounce?.cancel()
        // 空关键词不发起请求，直接清空列表回到初始态，避免无意义转圈
        if (q.isBlank()) {
            batchMode = false
            // 取消尚未完成的批量搜索，避免清空后又写回旧批量结果
            searchBatchJob?.cancel()
            _state.value = _state.value.copy(
                items = emptyList(),
                refreshing = false,
                loadingMore = false,
                error = null,
                page = 1,
                endReached = false,
                coverHiddenIds = emptySet(),
            )
            return
        }
        debounce = viewModelScope.launch {
            delay(400)
            // 关键修复：debounce 自动搜索不写入历史，避免输入中途停顿污染历史
            // （输入 "abc" 中途停顿会产生 "a"、"ab"、"abc" 三个历史项）
            // 只有用户明确触发搜索（searchDirect：点历史词 / IME 搜索键）才记录历史
            val batchIds = parseBatchIds(q)
            if (batchIds != null) {
                searchBatch(batchIds, saveHistory = false)
            } else {
                // v27.14：从批量模式切回普通搜索时取消尚未完成的批量搜索，
                // 避免其 setBatchResult 在 refresh 之后覆盖普通搜索结果。
                if (batchMode) {
                    batchMode = false
                    searchBatchJob?.cancel()
                }
                refresh()
            }
        }
    }

    /** 直接用历史词搜索（点击历史词条触发，立即搜索并存历史）。 */
    fun searchDirect(q: String) {
        query = q
        debounce?.cancel()
        // v27.14：批量 ID 搜索（输入多个 JM 号，空格/逗号/句号分隔）
        val batchIds = parseBatchIds(q)
        if (batchIds != null) {
            searchBatch(batchIds, saveHistory = true)
            return
        }
        // v27.14：从批量模式切回普通搜索时取消尚未完成的批量搜索
        if (batchMode) {
            batchMode = false
            searchBatchJob?.cancel()
        }
        viewModelScope.launch {
            // v27.5 稳定性加固：DataStore 读盘/写盘可能抛 IOException，未捕获会冒泡到
            // Thread.uncaughtExceptionHandler → CrashHandler → 杀进程。整段包 try-catch，
            // 失败仍触发 refresh，让用户至少能看到搜索结果（只是没存进历史）。
            try {
                // v27.5 #10 #30：仅在用户开启"记录搜索历史"且未启用隐身模式时才写历史
                val s = container.settingsStore.settings.first()
                if (s.saveSearchHistory && !s.incognito) {
                    container.searchHistoryStore.add(q.trim())
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.w("Search", "记录搜索历史失败: ${Logger.brief(e)}")
            }
            refresh()
        }
    }

    fun onOrderChange(o: String) {
        // v27.14：批量模式下排序无意义（结果固定无分页），直接忽略
        if (batchMode) return
        order = o
        debounce?.cancel()   // 取消尚未触发的输入 debounce，避免与本次 refresh 并发
        refresh()
    }

    /**
     * v27.14：通用重试入口（错误页"重试"按钮调用）。
     * - 批量模式下：解析当前 query 重新触发批量搜索（refresh 在 batchMode 下返回空，无效）。
     * - 普通模式下：等价于 refresh()。
     */
    fun retrySearch() {
        if (batchMode) {
            val ids = parseBatchIds(query) ?: run {
                // 异常状态：batchMode=true 但 query 不是批量格式，回退到普通搜索
                batchMode = false
                refresh()
                return
            }
            searchBatch(ids, saveHistory = false)
        } else {
            refresh()
        }
    }

    /**
     * v27.14：解析输入是否为批量 JM 号搜索。
     *
     * 触发条件：输入含分隔符（空格/逗号/句号/分号，中英文皆可），且拆分后≥2 个 token，
     * 且每个 token 均为纯字母数字（JM ID 特征），长度 1..30。
     *
     * @return 按输入顺序去重后的 ID 列表，或 null 表示不是批量搜索（按普通关键词走 search API）。
     */
    fun parseBatchIds(input: String): List<String>? {
        val tokens = input.split(Regex("[\\s,，。;；]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (tokens.size < 2) return null
        val idPattern = Regex("^[A-Za-z0-9]+$")
        if (!tokens.all { idPattern.matches(it) && it.length <= 30 }) return null
        // 按输入顺序去重（用户可能重复输入同一个 ID）
        val seen = HashSet<String>(tokens.size)
        val ordered = ArrayList<String>(tokens.size)
        for (t in tokens) {
            if (seen.add(t)) ordered.add(t)
        }
        return ordered
    }

    /**
     * v27.14：批量按 JM 号搜索。
     *
     * 并发拉取每个 ID 的详情（comicDetail），按用户输入顺序排列结果。
     * 详情接口返回的 tags/likes/views 即真实数据，无需 enrich。
     * 批量结果无分页（endReached=true），且忽略排序变化。
     *
     * @param ids 按用户输入顺序的 ID 列表（已去重）
     * @param saveHistory true=记录搜索历史（用户主动触发时）
     */
    private fun searchBatch(ids: List<String>, saveHistory: Boolean) {
        query = ids.joinToString(" ")
        batchMode = true
        debounce?.cancel()
        // v27.14：取消上一次未完成的批量搜索，避免旧结果覆盖新结果
        searchBatchJob?.cancel()
        // 可选：记录搜索历史（批量搜索词即原始输入）
        if (saveHistory) {
            viewModelScope.launch {
                try {
                    val s = container.settingsStore.settings.first()
                    if (s.saveSearchHistory && !s.incognito) {
                        container.searchHistoryStore.add(ids.joinToString(" "))
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Logger.w("Search", "记录批量搜索历史失败: ${Logger.brief(e)}")
                }
            }
        }
        searchBatchJob = viewModelScope.launch {
            _state.value = _state.value.copy(
                refreshing = true,
                error = null,
                page = 1,
                endReached = true,
            )
            try {
                // 并发拉取所有 ID 的详情，超时 10s 避免卡死
                val details = coroutineScope {
                    ids.map { id ->
                        async {
                            try {
                                kotlinx.coroutines.withTimeoutOrNull(10_000L) {
                                    container.repository.comicDetail(id).let {
                                        (it as? Resource.Success)?.data
                                    }
                                }
                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e
                            } catch (e: Throwable) {
                                null
                            }
                        }
                    }.awaitAll()
                }
                // 按输入顺序排列，失败的 ID 排在后面（保留占位）
                val validDetails = mutableListOf<ComicBriefDto>()
                val failedIds = mutableListOf<String>()
                ids.forEachIndexed { idx, id ->
                    val detail = details[idx]
                    if (detail != null) {
                        validDetails.add(detail)
                    } else {
                        failedIds.add(id)
                    }
                }
                if (validDetails.isEmpty() && failedIds.isNotEmpty()) {
                    // 全部失败
                    _state.value = _state.value.copy(
                        refreshing = false,
                        error = "批量搜索失败：${failedIds.joinToString(", ")} 未找到或加载失败",
                    )
                } else {
                    // 至少有部分成功，用 setBatchResult 写入（自动应用屏蔽规则 + 写持久化缓存）
                    setBatchResult(validDetails, totalForState = validDetails.size)
                    if (failedIds.isNotEmpty()) {
                        Logger.w("Search", "批量搜索部分失败: ${failedIds.joinToString(", ")}")
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e("Search", "批量搜索异常", e)
                _state.value = _state.value.copy(
                    refreshing = false,
                    error = Logger.friendlyError(Logger.brief(e)),
                )
            }
        }
    }

    override suspend fun loadPage(page: Int): Resource<Pair<List<ComicBriefDto>, Int?>> {
        // v27.14：批量模式下禁用分页（批量结果已全部加载，endReached=true）
        if (batchMode) {
            return Resource.Success(emptyList<ComicBriefDto>() to 0)
        }
        if (query.isBlank()) {
            return Resource.Success(emptyList<ComicBriefDto>() to 0)
        }
        val r = container.repository.search(query, page, order)
        return when (r) {
            is Resource.Success -> {
                // 关键修复（Bug 48）：首页为空时自动重试一次（与 RandomListViewModel 同款逻辑）。
                // 搜索偶尔命中空页（禁漫搜索算法不稳定，部分词首页空但深页有结果），
                // 首页空时重试避免用户误以为"没有结果"。
                if (page == 1 && r.data.items.isEmpty()) {
                    delay(500)
                    when (val r2 = container.repository.search(query, page, order)) {
                        is Resource.Success -> Resource.Success(r2.data.items to r2.data.total)
                        is Resource.Error -> r2
                        Resource.Loading -> Resource.Loading
                    }
                } else {
                    Resource.Success(r.data.items to r.data.total)
                }
            }
            is Resource.Error -> r
            Resource.Loading -> Resource.Loading
        }
    }
}
