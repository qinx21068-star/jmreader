package com.jmreader.ui.screen.author

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jmreader.data.AppContainer
import com.jmreader.data.dto.ComicBriefDto
import com.jmreader.data.repository.Resource
import com.jmreader.ui.viewmodel.BaseListViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 * 作者页 ViewModel - 显示特定作者的所有作品
 * 
 * v28.0 已迁移到 Hilt，使用 Assisted Injection 支持运行时参数
 */
@HiltViewModel(assistedFactory = AuthorViewModel.Factory::class)
class AuthorViewModel @AssistedInject constructor(
    private val container: AppContainer,
    @Assisted val author: String,
) : BaseListViewModel(container) {
    
    var order by mutableStateOf("latest")
        private set

    init {
        // 构造完成即开始加载首页
        refresh()
    }

    fun onOrderChange(o: String) {
        order = o
        refresh()
    }

    override suspend fun loadPage(page: Int): Resource<Pair<List<ComicBriefDto>, Int?>> {
        val r = container.repository.search(author, page, order)
        return when (r) {
            is Resource.Success -> Resource.Success(r.data.items to r.data.total)
            is Resource.Error -> r
            Resource.Loading -> Resource.Loading
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(author: String): AuthorViewModel
    }
}
