package com.jmreader.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jmreader.data.AppContainer
import com.jmreader.data.dto.ComicBriefDto
import com.jmreader.data.repository.JMRepository
import com.jmreader.data.repository.Resource
import com.jmreader.ui.viewmodel.BaseListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 通用分类列表 VM：支持 分类 + 时间 + 排序 任意组合。
 * 参数变化时自动 refresh。直连模式调 categoriesFilter。
 * 
 * 已迁移到 Hilt：通过构造函数注入依赖。
 * v28.0 Phase 1.1：渐进式迁移 - 暂时保持接收 AppContainer，
 * 后续 Phase 会完全重构 BaseListViewModel 去除 AppContainer 依赖。
 */
@HiltViewModel
class CategoryListViewModel @Inject constructor(
    container: AppContainer,
) : BaseListViewModel(container) {
    var time by mutableStateOf("a")
        private set
    var category by mutableStateOf("")
        private set
    var order by mutableStateOf("mr")
        private set

    fun updateTime(t: String) { if (time != t) { time = t; refresh() } }
    fun updateCategory(c: String) { if (category != c) { category = c; refresh() } }
    fun updateOrder(o: String) { if (order != o) { order = o; refresh() } }

    /**
     * 关键修复（Bug 46）：一次性设置 order + time 再 refresh，避免连续 updateOrder + updateTime
     * 触发两次 refresh。第二次 refresh 虽 cancel了 refreshJob，但 OkHttp call 已在 IO 调度器
     * 上发出，协程 cancel 不会自动 cancel OkHttp call，导致首次启动多发一次请求，
     * 结果被丢弃；禁漫有限流时可能首次进排行 tab 就被 429。
     */
    fun setDefaults(o: String, t: String) {
        if (order != o) order = o
        if (time != t) time = t
        refresh()
    }

    override suspend fun loadPage(page: Int): Resource<Pair<List<ComicBriefDto>, Int?>> =
        when (val r = container.repository.categoriesFilter(page, time, category, order)) {
            is Resource.Success -> Resource.Success(r.data.items to r.data.total)
            is Resource.Error -> r
            Resource.Loading -> Resource.Loading
        }
}

/**
 * 随机推荐 VM：调 [JMRepository.randomComics] 获取随机本子。
 *
 * 行为：
 * - refresh()：换一批，清空原列表重新拉取
 * - loadMore()：追加一批新的随机本子（按 id 去重，避免与已有重复）
 *
 * 首次返回空时自动重试一次（与 SearchViewModel 同款 Bug 48 修复逻辑）。
 * 
 * 已迁移到 Hilt：通过构造函数注入依赖。
 * v28.0 Phase 1.1：渐进式迁移 - 暂时保持接收 AppContainer。
 */
@HiltViewModel
class RandomListViewModel @Inject constructor(
    container: AppContainer,
) : BaseListViewModel(container) {
    override suspend fun loadPage(page: Int): Resource<Pair<List<ComicBriefDto>, Int?>> {
        val r = container.repository.randomComics()
        return when (r) {
            is Resource.Success -> {
                // 关键修复：随机 page+分类偶尔会命中空页（深 page 或冷门分类），
                // 首页为空时自动重试一次，避免用户看到"没有数据"误以为功能坏了。
                if (page == 1 && r.data.items.isEmpty()) {
                    kotlinx.coroutines.delay(500)
                    when (val r2 = repository.randomComics()) {
                        is Resource.Success -> Resource.Success(r2.data.items to null)
                        is Resource.Error -> r2
                        Resource.Loading -> Resource.Loading
                    }
                } else {
                    // total=null：随机模式下"已到底"无意义，让 loadMore 始终可触发，
                    // 用户可以无限"换一批"。endReached 由 items.isEmpty() 控制。
                    Resource.Success(r.data.items to null)
                }
            }
            is Resource.Error -> r
            Resource.Loading -> Resource.Loading
        }
    }
}
