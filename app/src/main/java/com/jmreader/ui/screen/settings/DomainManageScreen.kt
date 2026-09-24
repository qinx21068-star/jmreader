@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.jmreader.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmreader.core.Logger
import com.jmreader.data.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 单个域名的测速/选择状态。
 */
data class DomainItem(
    val host: String,
    val latency: Long? = null,        // ms；null 表示未测或失败
    val error: String? = null,        // 非 null 表示失败原因
    val testing: Boolean = false,
    val isCurrent: Boolean = false,
    val isCustom: Boolean = false,    // 用户自定义（可删）
)

    val vm: DomainViewModel = hiltViewModel()
    val vm: DomainViewModel = viewModel(factory = DomainVMFactory(container))
    val items = vm.items
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.events.collect { snackbar.showSnackbar(it) } }

    var showAdd by remember { mutableStateOf(false) }
    var newHost by remember { mutableStateOf("") }
    val anyTesting = items.any { it.testing }
    // 删除确认：记录待删除的域名，确认后才真正删除
    var deleteTarget by remember { mutableStateOf<DomainItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API 域名管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.refreshFromServer() }, enabled = !anyTesting) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "拉取最新域名")
                    }
                    IconButton(onClick = { showAdd = true; newHost = "" }, enabled = !anyTesting) {
                        Icon(Icons.Outlined.Add, contentDescription = "添加域名")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Button(
                    onClick = { vm.testAll() },
                    enabled = !anyTesting,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    if (anyTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp).width(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("测速中…")
                    } else {
                        Icon(Icons.Outlined.Speed, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("全部测速")
                    }
                }
            }
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
        ) {
            item {
                Text(
                    "点底部「全部测速」检测每个域名的延迟与可用性；点某行的勾设为当前域名。" +
                    "若全部失败，可点右上角刷新从服务器拉最新域名，或手动添加。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            itemsIndexed(items, key = { i, item -> item.host.lowercase().ifBlank { "idx_$i" } }) { _, item ->
                // v27.5 性能修复：lambda 用 remember 缓存
                val onTest = remember(item, vm) { { vm.testOne(item.host); Unit } }
                val onSelect = remember(item, vm) { { vm.select(item.host); Unit } }
                val onRemove = if (item.isCustom) {
                    remember(item) { { deleteTarget = item } }
                } else null
                DomainRow(
                    item = item,
                    onTest = onTest,
                    onSelect = onSelect,
                    onRemove = onRemove,
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false; newHost = "" },
            title = { Text("添加自定义域名") },
            text = {
                OutlinedTextField(
                    value = newHost,
                    onValueChange = { newHost = it },
                    singleLine = true,
                    placeholder = { Text("www.cdnhjk.net") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newHost.isNotBlank()) {
                        vm.addCustom(newHost)
                        newHost = ""
                    }
                    showAdd = false
                }) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false; newHost = "" }) { Text("取消") }
            },
        )
    }

    // 删除域名二次确认：删除当前生效域名会静默切换到其它域名，需告知用户
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除域名") },
            text = {
                Text(
                    buildString {
                        append("确认删除「${target.host}」？\n\n")
                        if (target.isCurrent) {
                            append("这是当前生效的域名，删除后将自动切换到其它可用域名。\n\n")
                        }
                        append("此操作不可撤销。")
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val host = target.host
                    deleteTarget = null
                    vm.removeCustom(host)
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun DomainRow(
    item: DomainItem,
    onTest: () -> Unit,
    onSelect: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCurrent) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.host,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (item.isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    )
                    if (item.isCurrent) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) { Text("当前", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall) }
                    }
                    if (item.isCustom) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) { Text("自定义", style = MaterialTheme.typography.labelSmall) }
                    }
                }
                Spacer(Modifier.height(4.dp))
                when {
                    item.testing -> Text("测速中…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    item.error != null -> Text("失败：${item.error}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    item.latency != null -> Text("${item.latency} ms", style = MaterialTheme.typography.bodySmall, color = if (item.latency < 500) Color(0xFF2E7D32) else Color(0xFFEF6C00))
                    else -> Text("未测速", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onTest) {
                Icon(Icons.Outlined.Speed, contentDescription = "测速")
            }
            IconButton(onClick = onSelect) {
                Icon(Icons.Outlined.Check, contentDescription = "设为当前")
            }
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Outlined.Delete, contentDescription = "删除")
                }
            }
        }
    }
}
