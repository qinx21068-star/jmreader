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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.data.local.ListStyle
import com.jmreader.ui.components.GlassyTopAppBar
import com.jmreader.ui.components.RikkaChoiceItem
import com.jmreader.ui.components.RikkaGradientBackground
import com.jmreader.ui.components.RikkaSettingsGroup
import com.jmreader.ui.components.RikkaSliderItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListSettingsGlassy(container: AppContainer, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val settings by container.settingsStore.settings.collectAsState(initial = container.settingsStore.cachedSnapshot)
    val scope = rememberCoroutineScope()
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { GlassyTopAppBar("列表设置", navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack, onNavigationClick = onBack) }) { padding ->
        RikkaGradientBackground(modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    RikkaSettingsGroup("列表样式") {
                        RikkaChoiceItem("布局模式", settings.listStyle.name, listOf(
                            "列表" to (settings.listStyle == ListStyle.LIST), 
                            "网格" to (settings.listStyle == ListStyle.GRID), 
                            "紧凑" to (settings.listStyle == ListStyle.COMPACT_GRID), 
                            "卡片" to (settings.listStyle == ListStyle.CARD), 
                            "杂志" to (settings.listStyle == ListStyle.MAGAZINE),
                        )) { label -> scope.launch { container.settingsStore.setListStyle(when (label) { 
                            "网格" -> ListStyle.GRID
                            "紧凑" -> ListStyle.COMPACT_GRID
                            "卡片" -> ListStyle.CARD
                            "杂志" -> ListStyle.MAGAZINE
                            else -> ListStyle.LIST 
                        }) } }
                    }
                }
                item {
                    RikkaSettingsGroup("封面与字体") {
                        RikkaChoiceItem("封面比例", settings.coverAspectRatio, listOf(
                            "3:4" to (settings.coverAspectRatio == "3:4"), 
                            "2:3" to (settings.coverAspectRatio == "2:3"), 
                            "1:1" to (settings.coverAspectRatio == "1:1"), 
                            "16:9" to (settings.coverAspectRatio == "16:9")
                        )) { scope.launch { container.settingsStore.setCoverAspectRatio(it) } }
                        RikkaSliderItem("标题字号", settings.listTitleFontSize, 10f..18f, "${settings.listTitleFontSize.toInt()} sp") { scope.launch { container.settingsStore.setListTitleFontSize(it) } }
                        RikkaSliderItem("正文字号", settings.listBodyFontSize, 10f..16f, "${settings.listBodyFontSize.toInt()} sp") { scope.launch { container.settingsStore.setListBodyFontSize(it) } }
                    }
                }
                item {
                    RikkaSettingsGroup("间距与圆角") {
                        RikkaSliderItem("项目间距", settings.listItemSpacing, 4f..24f, "${settings.listItemSpacing.toInt()} dp") { scope.launch { container.settingsStore.setListItemSpacing(it) } }
                        RikkaSliderItem("封面圆角", settings.listCoverRadius, 0f..24f, "${settings.listCoverRadius.toInt()} dp") { scope.launch { container.settingsStore.setListCoverRadius(it) } }
                        RikkaSliderItem("卡片圆角", settings.listCardRadius, 0f..24f, "${settings.listCardRadius.toInt()} dp") { scope.launch { container.settingsStore.setListCardRadius(it) } }
                    }
                }
            }
        }
    }
}
