package com.jmreader.ui.screen.settings

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmreader.core.Logger
import com.jmreader.data.AppContainer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DomainItem(
    val domain: String,
    val isBuiltin: Boolean,
    val latency: Long? = null,
    val testing: Boolean = false,
)

@HiltViewModel
class DomainViewModel @Inject constructor(
    private val container: AppContainer,
) : ViewModel() {

    private val _items = mutableStateListOf<DomainItem>()
    val items: List<DomainItem> get() = _items

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val builtin: List<String> get() = container.directClient.apiDomainList()

    init { reload() }

    private fun launchSafe(block: suspend () -> Unit) = viewModelScope.launch {
        try {
            block()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.e("DomainVM", "操作失败", e)
            _events.emit("操作失败: ${Logger.brief(e)}")
        }
    }

    fun reload() = launchSafe {
        val custom = container.settingsStore.settings.first().customDomains
        val all = (builtin + custom).distinct()
        _items.clear()
        _items.addAll(all.map { DomainItem(it, it in builtin) })
    }

    fun add(domain: String) = launchSafe {
        if (domain.isBlank()) return@launchSafe
        val clean = domain.trim().lowercase()
        if (clean in builtin) {
            _events.emit("$clean 已在内置列表中")
            return@launchSafe
        }
        val current = container.settingsStore.settings.first().customDomains
        if (clean in current) {
            _events.emit("$clean 已存在")
            return@launchSafe
        }
        container.settingsStore.updateSettings { it.copy(customDomains = current + clean) }
        reload()
        _events.emit("已添加 $clean")
    }

    fun remove(domain: String) = launchSafe {
        if (domain in builtin) {
            _events.emit("内置域名不可删除")
            return@launchSafe
        }
        val current = container.settingsStore.settings.first().customDomains
        container.settingsStore.updateSettings { it.copy(customDomains = current - domain) }
        reload()
        _events.emit("已删除 $domain")
    }

    fun test(domain: String) = launchSafe {
        val idx = _items.indexOfFirst { it.domain == domain }
        if (idx < 0) return@launchSafe
        _items[idx] = _items[idx].copy(testing = true)
        val start = System.currentTimeMillis()
        val result = container.repository.testDomain(domain)
        val latency = System.currentTimeMillis() - start
        _items[idx] = when (result) {
            is com.jmreader.data.repository.Resource.Success -> 
                _items[idx].copy(testing = false, latency = latency)
            else -> {
                _items[idx].copy(testing = false, latency = null)
            }
        }
        val msg = when (result) {
            is com.jmreader.data.repository.Resource.Success -> "$domain 可用 (${latency}ms)"
            is com.jmreader.data.repository.Resource.Error -> "$domain 失败: ${result.message}"
            else -> "$domain 超时"
        }
        _events.emit(msg)
    }

    fun testAll() = launchSafe {
        _items.indices.forEach { idx ->
            _items[idx] = _items[idx].copy(testing = true)
        }
        kotlinx.coroutines.coroutineScope {
            _items.map { item ->
                kotlinx.coroutines.async {
                    val start = System.currentTimeMillis()
                    val result = container.repository.testDomain(item.domain)
                    val latency = System.currentTimeMillis() - start
                    item.copy(
                        testing = false,
                        latency = if (result is com.jmreader.data.repository.Resource.Success) latency else null
                    )
                }
            }.awaitAll().also { results ->
                _items.clear()
                _items.addAll(results)
            }
        }
        val available = _items.count { it.latency != null }
        _events.emit("测试完成: $available/${_items.size} 可用")
    }
}
