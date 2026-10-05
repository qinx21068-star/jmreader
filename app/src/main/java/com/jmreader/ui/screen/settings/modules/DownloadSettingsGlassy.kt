package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.ui.components.GlassyTopAppBar
import com.jmreader.ui.components.RikkaGradientBackground
import com.jmreader.ui.components.RikkaSettingsGroup
import com.jmreader.ui.components.RikkaSettingsItem
import com.jmreader.ui.components.RikkaSliderItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadSettingsGlassy(container: AppContainer, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val settings by container.settingsStore.settings.collectAsState(initial = container.settingsStore.cachedSnapshot)
    val scope = rememberCoroutineScope()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { GlassyTopAppBar("下载设置", navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack, onNavigationClick = onBack) },
    ) { padding ->
        RikkaGradientBackground(modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    RikkaSettingsGroup("下载性能") {
                        RikkaSliderItem("并发下载数", settings.downloadConcurrency.toFloat(), 1f..4f, "${settings.downloadConcurrency} 个") { value -> scope.launch { container.settingsStore.setDownloadConcurrency(value.toInt()) } }
                    }
                }
                item {
                    RikkaSettingsGroup("下载管理") {
                        RikkaSettingsItem(
                            "本地搜索", "在已下载内容中搜索",
                            trailingContent = { Switch(checked = settings.localSearchEnabled, onCheckedChange = { value -> scope.launch { container.settingsStore.setLocalSearchEnabled(value) } }) },
                        )
                    }
                }
            }
        }
    }
}
