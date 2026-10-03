package com.jmreader.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jmreader.ui.components.GlassySettingsCard
import com.jmreader.ui.components.GlassyTopAppBar
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

/**
 * RikkaHub 同款 - 毛玻璃风格设置主入口
 * 
 * 6 个模块入口：
 * 1. 外观设置（主题、配色、圆角、阴影）
 * 2. 阅读器设置（方向、翻页、滤镜、缩放）
 * 3. 列表设置（样式、字号、封面比例）
 * 4. 网络设置（域名、代理、质量）
 * 5. 隐私设置（应用锁、截图）
 * 6. 下载设置（路径、并发）
 * 7. 关于（版本、协议）
 */
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
    val hazeState = remember { HazeState() }
    
    Scaffold(
        topBar = {
            GlassyTopAppBar(
                title = "设置",
                hazeState = hazeState,
                navigationIcon = Icons.Outlined.Settings,
                onNavigationClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .haze(state = hazeState)
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            
            // 标题
            Text(
                text = "应用设置",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            Text(
                text = "自定义您的阅读体验",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            Spacer(Modifier.height(8.dp))
            
            // 1. 外观设置
            GlassySettingsCard(
                title = "外观设置",
                subtitle = "主题、配色、圆角、阴影",
                icon = Icons.Outlined.Palette,
                hazeState = hazeState,
                onClick = onNavigateToAppearance,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            // 2. 阅读器设置
            GlassySettingsCard(
                title = "阅读器设置",
                subtitle = "阅读方向、翻页、滤镜、缩放",
                icon = Icons.Outlined.MenuBook,
                hazeState = hazeState,
                onClick = onNavigateToReader,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            // 3. 列表设置
            GlassySettingsCard(
                title = "列表设置",
                subtitle = "列表样式、字号、封面比例",
                icon = Icons.Outlined.ViewList,
                hazeState = hazeState,
                onClick = onNavigateToList,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            // 4. 网络设置
            GlassySettingsCard(
                title = "网络设置",
                subtitle = "API域名、代理、图片质量",
                icon = Icons.Outlined.Cloud,
                hazeState = hazeState,
                onClick = onNavigateToNetwork,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            // 5. 隐私设置
            GlassySettingsCard(
                title = "隐私设置",
                subtitle = "应用锁、隐身模式、截图屏蔽",
                icon = Icons.Outlined.Lock,
                hazeState = hazeState,
                onClick = onNavigateToPrivacy,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            // 6. 下载设置
            GlassySettingsCard(
                title = "下载设置",
                subtitle = "存储路径、并发数、本地搜索",
                icon = Icons.Outlined.Download,
                hazeState = hazeState,
                onClick = onNavigateToDownload,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            // 7. 关于
            GlassySettingsCard(
                title = "关于",
                subtitle = "版本信息、开源协议、反馈",
                icon = Icons.Outlined.Info,
                hazeState = hazeState,
                onClick = onNavigateToAbout,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            Spacer(Modifier.height(16.dp))
            
            // 版本信息
            Text(
                text = "JMReader v29.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
