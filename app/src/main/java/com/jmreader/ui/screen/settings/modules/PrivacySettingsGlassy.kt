package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.ui.components.GlassyTopAppBar
import com.jmreader.ui.components.RikkaGradientBackground
import com.jmreader.ui.components.RikkaSettingsGroup
import com.jmreader.ui.components.RikkaSettingsItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsGlassy(container: AppContainer, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val settings by container.settingsStore.settings.collectAsState(initial = container.settingsStore.cachedSnapshot)
    val scope = rememberCoroutineScope()
    var showPinDialog by remember { mutableStateOf(false) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { GlassyTopAppBar("隐私设置", navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack, onNavigationClick = onBack) }) { padding ->
        RikkaGradientBackground(modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    RikkaSettingsGroup("应用锁") {
                        RikkaSettingsItem(
                            "启用应用锁", "从后台返回时验证指纹或密码",
                            trailingContent = { Switch(checked = settings.appLockEnabled, onCheckedChange = { value -> scope.launch { container.settingsStore.setAppLockEnabled(value) } }) },
                        )
                        if (settings.appLockEnabled) RikkaSettingsItem("设置 PIN 码", if (settings.appLockPin == null) "使用生物识别" else "已设置 PIN (${settings.appLockPin!!.length} 位)", onClick = { showPinDialog = true })
                    }
                }
                item {
                    RikkaSettingsGroup("浏览记录") {
                        RikkaSettingsItem(
                            "隐身模式", "不记录浏览历史、阅读进度和搜索历史",
                            trailingContent = { Switch(checked = settings.incognito, onCheckedChange = { value -> scope.launch { container.settingsStore.setIncognito(value) } }) },
                        )
                        RikkaSettingsItem(
                            "记录搜索历史", "保存搜索关键词",
                            trailingContent = { Switch(checked = settings.saveSearchHistory, enabled = !settings.incognito, onCheckedChange = { value -> scope.launch { container.settingsStore.setSaveSearchHistory(value) } }) },
                        )
                    }
                }
                item {
                    RikkaSettingsGroup("屏幕安全") {
                        RikkaSettingsItem(
                            "屏蔽截图", "防止截图和录屏",
                            trailingContent = { Switch(checked = settings.blockScreenshots, onCheckedChange = { value -> scope.launch { container.settingsStore.setBlockScreenshots(value) } }) },
                        )
                    }
                }
            }
        }
    }
    if (showPinDialog) AlertDialog(
        onDismissRequest = { showPinDialog = false },
        title = { Text("设置 PIN 码") },
        text = { Text("PIN 码设置功能待实现") },
        confirmButton = { TextButton(onClick = { showPinDialog = false }) { Text("确定") } },
    )
}
