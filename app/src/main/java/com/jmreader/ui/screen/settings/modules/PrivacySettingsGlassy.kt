package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.ui.components.*
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsGlassy(
    container: AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by container.settingsStore.settings.collectAsState()
    val hazeState = remember { HazeState() }
    
    var showPinDialog by remember { mutableStateOf(false) }
    var pinText by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            GlassyTopAppBar(
                title = "隐私设置",
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
            // 应用锁
            item {
                Text(
                    text = "应用锁",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "启用应用锁",
                    subtitle = "从后台返回需验证身份",
                    checked = settings.appLockEnabled,
                    onCheckedChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setAppLockEnabled(it)
                        }
                    },
                    hazeState = hazeState,
                )
            }
            
            if (settings.appLockEnabled) {
                item {
                    GlassySettingsCard(
                        title = "设置 PIN 码",
                        subtitle = if (settings.appLockPin == null) {
                            "使用生物识别"
                        } else {
                            "已设置 PIN (${settings.appLockPin!!.length} 位)"
                        },
                        onClick = { showPinDialog = true },
                        hazeState = hazeState,
                    )
                }
            }
            
            // 浏览记录
            item {
                Text(
                    text = "浏览记录",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "隐身模式",
                    subtitle = "不记录浏览历史和阅读进度",
                    checked = settings.incognito,
                    onCheckedChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setIncognito(it)
                        }
                    },
                    hazeState = hazeState,
                )
            }
            
            item {
                GlassySwitch(
                    title = "保存搜索历史",
                    subtitle = "记录搜索关键词",
                    checked = settings.saveSearchHistory,
                    onCheckedChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setSaveSearchHistory(it)
                        }
                    },
                    hazeState = hazeState,
                )
            }
            
            // 屏幕安全
            item {
                Text(
                    text = "屏幕安全",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySwitch(
                    title = "屏蔽截图",
                    subtitle = "防止截图和录屏 (FLAG_SECURE)",
                    checked = settings.blockScreenshots,
                    onCheckedChange = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setBlockScreenshots(it)
                        }
                    },
                    hazeState = hazeState,
                )
            }
        }
    }
    
    // PIN 设置对话框
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("设置 PIN 码") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "留空使用生物识别（指纹/面容）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { 
                            if (it.length <= 8 && it.all { c -> c.isDigit() }) {
                                pinText = it
                            }
                        },
                        label = { Text("PIN (4-8位数字)") },
                        placeholder = { Text("留空使用生物识别") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        kotlinx.coroutines.MainScope().launch {
                            container.settingsStore.setAppLockPin(
                                pinText.ifBlank { null }
                            )
                        }
                        showPinDialog = false
                        pinText = ""
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showPinDialog = false
                        pinText = ""
                    }
                ) {
                    Text("取消")
                }
            },
        )
    }
}
