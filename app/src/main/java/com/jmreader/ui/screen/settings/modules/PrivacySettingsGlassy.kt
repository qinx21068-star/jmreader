package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.ui.components.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.launch

/**
 * 隐私设置模块 - Material 3 设计风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsGlassy(
    container: AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by container.settingsStore.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    var showPinDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            GlassyTopAppBar(
                title = "隐私设置",
                hazeState = hazeState,
                onNavigationClick = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .haze(state = hazeState)
                .padding(padding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ============= 应用锁 =============
            item {
                GlassySectionTitle(
                    title = "应用锁",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "启用应用锁",
                    subtitle = "从后台返回时需要验证指纹/密码",
                    checked = settings.appLockEnabled,
                    onCheckedChange = {
                        scope.launch {
                            container.settingsStore.setAppLockEnabled(it)
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            if (settings.appLockEnabled) {
                item {
                    GlassySettingsCard(
                        icon = Icons.Outlined.Pin,
                        title = "设置 PIN 码",
                        subtitle = if (settings.appLockPin == null) {
                            "使用生物识别"
                        } else {
                            "已设置 PIN (${settings.appLockPin!!.length} 位)"
                        },
                        onClick = { showPinDialog = true },
                        hazeState = hazeState,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
            
            // ============= 浏览记录 =============
            item {
                GlassySectionTitle(
                    title = "浏览记录",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "隐身模式",
                    subtitle = "不记录浏览历史、阅读进度、搜索历史",
                    checked = settings.incognito,
                    onCheckedChange = {
                        scope.launch {
                            container.settingsStore.setIncognito(it)
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "记录搜索历史",
                    subtitle = "保存搜索关键词以便快速输入",
                    checked = settings.saveSearchHistory,
                    enabled = !settings.incognito,
                    onCheckedChange = {
                        scope.launch {
                            container.settingsStore.setSaveSearchHistory(it)
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            // ============= 屏幕安全 =============
            item {
                GlassySectionTitle(
                    title = "屏幕安全",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "屏蔽截图",
                    subtitle = "防止截图和录屏（FLAG_SECURE）",
                    checked = settings.blockScreenshots,
                    onCheckedChange = {
                        scope.launch {
                            container.settingsStore.setBlockScreenshots(it)
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
    
    // PIN 码设置对话框（这里简化处理）
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("设置 PIN 码") },
            text = { Text("PIN 码设置功能待实现") },
            confirmButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
}
