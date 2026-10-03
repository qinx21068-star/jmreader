package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.data.local.ImageQuality
import com.jmreader.ui.components.*
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsGlassy(
    container: AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by container.settingsStore.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    
    var proxyText by remember { mutableStateOf(settings.proxy ?: "") }
    var showProxyDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            GlassyTopAppBar(
                title = "网络设置",
                hazeState = hazeState,
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                onNavigationClick = onBack,
            )
        },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            // 图片质量
            item {
                Text(
                    text = "图片质量",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassyCard(hazeState = hazeState) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "加载质量",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        
                        ImageQuality.entries.forEach { quality ->
                            FilterChip(
                                selected = settings.imageQuality == quality,
                                onClick = {
                                    kotlinx.coroutines.MainScope().launch {
                                        container.settingsStore.setImageQuality(quality)
                                    }
                                },
                                label = {
                                    Text(
                                        when (quality) {
                                            ImageQuality.ORIGINAL -> "原图 (最清晰，最慢)"
                                            ImageQuality.HIGH -> "高清 (推荐)"
                                            ImageQuality.MEDIUM -> "中等 (省流量)"
                                            ImageQuality.LOW -> "低画质 (最快)"
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            
            // 代理设置
            item {
                Text(
                    text = "代理设置",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySettingsCard(
                    icon = Icons.Outlined.VpnKey,
                    title = "HTTP/SOCKS5 代理",
                    subtitle = if (settings.proxy.isNullOrBlank()) {
                        "未设置"
                    } else {
                        settings.proxy!!
                    },
                    onClick = { showProxyDialog = true },
                    hazeState = hazeState,
                )
            }
            
            // CDN 设置
            item {
                Text(
                    text = "CDN 设置",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySettingsCard(
                    icon = Icons.Outlined.Lock,
                    title = "锁定图片 CDN",
                    subtitle = if (settings.pinnedImageCdn.isNullOrBlank()) {
                        "自动轮换 (推荐)"
                    } else {
                        settings.pinnedImageCdn!!
                    },
                    onClick = {
                        // TODO: 实现 CDN 选择对话框
                    },
                    hazeState = hazeState,
                )
            }
        }
    }
    
    // 代理设置对话框
    if (showProxyDialog) {
        AlertDialog(
            onDismissRequest = { showProxyDialog = false },
            title = { Text("设置代理") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "格式: host:port 或 socks5://host:port",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = proxyText,
                        onValueChange = { proxyText = it },
                        label = { Text("代理地址") },
                        placeholder = { Text("例: 127.0.0.1:7890") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setProxy(
                                proxyText.ifBlank { null }
                            )
                        }
                        showProxyDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProxyDialog = false }) {
                    Text("取消")
                }
            },
        )
    }
}
