package com.jmreader.ui.screen.settings.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jmreader.data.AppContainer
import com.jmreader.ui.components.*
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsGlassy(
    container: AppContainer,
    onBack: () -> Unit,
    onOpenLogs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hazeState = remember { HazeState() }
    
    Scaffold(
        topBar = {
            GlassyTopAppBar(
                title = "关于",
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
            // 应用信息
            item {
                Text(
                    text = "应用信息",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassyCard(hazeState = hazeState) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "版本号",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "29.0",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        
                        Divider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "版本代码",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "29",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            
            // 调试工具
            item {
                Text(
                    text = "调试工具",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySettingsCard(
                    title = "查看日志",
                    subtitle = "应用运行日志和错误记录",
                    icon = Icons.Outlined.Info,
                    onClick = onOpenLogs,
                    hazeState = hazeState,
                )
            }
            
            // 法律信息
            item {
                Text(
                    text = "法律信息",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassySettingsCard(
                    title = "开源协议",
                    subtitle = "MIT License",
                    icon = Icons.Outlined.Code,
                    onClick = { /* TODO: 显示开源协议 */ },
                    hazeState = hazeState,
                )
            }
            
            item {
                GlassySettingsCard(
                    title = "隐私政策",
                    subtitle = "我们如何处理您的数据",
                    icon = Icons.Outlined.Security,
                    onClick = { /* TODO: 显示隐私政策 */ },
                    hazeState = hazeState,
                )
            }
            
            // 免责声明
            item {
                Text(
                    text = "免责声明",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                GlassyCard(hazeState = hazeState) {
                    Text(
                        text = "本应用仅供学习交流使用，请勿用于非法用途。使用本应用所产生的一切后果由用户自行承担。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
