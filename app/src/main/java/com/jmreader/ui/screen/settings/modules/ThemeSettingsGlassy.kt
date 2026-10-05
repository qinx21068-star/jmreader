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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import com.jmreader.ui.components.RikkaThemeGrid
import com.jmreader.ui.screen.settings.ColorSchemePicker
import com.jmreader.ui.theme.PresetSchemes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsGlassy(
    container: AppContainer,
    onBack: () -> Unit,
) {
    val settings by container.settingsStore.settings.collectAsState(initial = container.settingsStore.cachedSnapshot)
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GlassyTopAppBar(
                title = "主题设置",
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                onNavigationClick = onBack,
            )
        },
    ) { padding ->
        RikkaGradientBackground(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    RikkaSettingsGroup("预设主题") {
                        RikkaThemeGrid(
                            schemes = PresetSchemes,
                            selectedId = settings.colorSchemeId,
                            onSelect = { id ->
                                scope.launch {
                                    container.settingsStore.setColorSchemeId(id)
                                    container.settingsStore.setDynamicColor(false)
                                }
                            },
                        )
                    }
                }
                item {
                    RikkaSettingsGroup("自定义主题") {
                        ColorSchemePicker(
                            currentId = settings.colorSchemeId,
                            customColors = settings.customColors,
                            enabled = true,
                            onPickPreset = { id -> scope.launch { container.settingsStore.setColorSchemeId(id) } },
                            onPickCustom = { colors ->
                                scope.launch {
                                    container.settingsStore.setCustomColors(colors)
                                    container.settingsStore.setColorSchemeId("custom")
                                    container.settingsStore.setDynamicColor(false)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
