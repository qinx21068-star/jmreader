package com.jmreader.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.ui.components.RikkaSettingsGroup
import com.jmreader.ui.components.RikkaSettingsItem
import com.jmreader.ui.components.RikkaGradientBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainGlassy(
    onNavigateToAppearance: () -> Unit,
    onNavigateToReader: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToDownload: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("设置") },
                navigationIcon = { androidx.compose.material3.IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回") } },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        RikkaGradientBackground(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding + PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    RikkaSettingsGroup("通用设置") {
                        RikkaSettingsItem("外观设置", "主题、配色、圆角、阴影", leadingContent = { Icon(Icons.Outlined.Palette, null) }, onClick = onNavigateToAppearance)
                        RikkaSettingsItem("阅读器设置", "阅读方向、翻页、滤镜、缩放", leadingContent = { Icon(Icons.Outlined.MenuBook, null) }, onClick = onNavigateToReader)
                        RikkaSettingsItem("列表设置", "列表样式、字号、封面比例", leadingContent = { Icon(Icons.Outlined.ViewList, null) }, onClick = onNavigateToList)
                        RikkaSettingsItem("隐私设置", "应用锁、截图与隐私模式", leadingContent = { Icon(Icons.Outlined.Lock, null) }, onClick = onNavigateToPrivacy)
                    }
                }
                item {
                    RikkaSettingsGroup("服务与数据") {
                        RikkaSettingsItem("网络设置", "域名、代理与图片质量", leadingContent = { Icon(Icons.Outlined.Cloud, null) }, onClick = onNavigateToNetwork)
                        RikkaSettingsItem("下载设置", "存储路径、并发与通知", leadingContent = { Icon(Icons.Outlined.Download, null) }, onClick = onNavigateToDownload)
                        RikkaSettingsItem("关于", "版本、日志与开源信息", leadingContent = { Icon(Icons.Outlined.Info, null) }, onClick = onNavigateToAbout)
                    }
                }
            }
        }
    }
}
