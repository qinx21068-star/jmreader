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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.ui.components.GlassyTopAppBar
import com.jmreader.ui.components.RikkaGradientBackground
import com.jmreader.ui.components.RikkaSettingsGroup
import com.jmreader.ui.components.RikkaSettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsGlassy(container: AppContainer, onBack: () -> Unit, onOpenLogs: () -> Unit = {}, modifier: Modifier = Modifier) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { GlassyTopAppBar("关于", navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack, onNavigationClick = onBack) }) { padding ->
        RikkaGradientBackground(modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    RikkaSettingsGroup("应用信息") {
                        RikkaSettingsItem("JMReader", "v29.0 · Material 3 Edition")
                        RikkaSettingsItem("查看日志", "导出和检查运行日志", onClick = onOpenLogs)
                    }
                }
                item {
                    RikkaSettingsGroup("开源许可") {
                        RikkaSettingsItem("项目技术栈", "Jetpack Compose · Material 3 · Coil · DataStore")
                        RikkaSettingsItem("免责声明", "本应用仅用于技术学习和研究")
                    }
                }
            }
        }
    }
}
