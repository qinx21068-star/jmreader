package com.jmreader.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.FilterChip
import com.jmreader.ui.theme.LocalCardElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jmreader.ui.theme.PresetScheme

@Composable
fun RikkaGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val background = if (LocalAppSettings.current?.glassBackgroundEnabled != false) {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.surfaceContainerLowest,
            )
        )
    } else {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background)
        )
    }
    Box(modifier = modifier.background(background)) { content() }
}

@Composable
fun RikkaSettingsGroup(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = LocalCardElevation.current),
            content = content,
        )
    }
}

@Composable
fun RikkaSettingsItem(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = leadingContent,
        trailingContent = trailingContent ?: if (onClick != null) {
            { Icon(Icons.Outlined.ChevronRight, contentDescription = null) }
        } else null,
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
fun RikkaSliderItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
        },
        trailingContent = { Text(valueLabel) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
fun RikkaChoiceItem(
    title: String,
    subtitle: String,
    options: List<Pair<String, Boolean>>,
    onSelect: (String) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { (label, selected) ->
                    FilterChip(
                        selected = selected,
                        onClick = { onSelect(label) },
                        label = { Text(label) },
                    )
                }
            }
        },
        trailingContent = { Text(subtitle) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
fun RikkaThemePreview(
    scheme: PresetScheme,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(52.dp).clip(CircleShape)) {
                drawRect(scheme.swatches.getOrElse(0) { scheme.light.primaryContainer }, size = size)
                drawRect(
                    scheme.swatches.getOrElse(1) { scheme.light.secondaryContainer },
                    size = size,
                    topLeft = Offset(size.width / 2f, 0f),
                )
                drawRect(
                    scheme.swatches.getOrElse(2) { scheme.light.tertiaryContainer },
                    size = size,
                    topLeft = Offset(size.width / 2f, size.height / 2f),
                )
                drawCircle(
                    color = scheme.swatches.getOrElse(0) { scheme.light.primary },
                    radius = if (selected) 12.dp.toPx() else 8.dp.toPx(),
                    center = Offset(size.width / 2f, size.height / 2f),
                )
            }
            if (selected) {
                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White)
            }
        }
        Text(
            text = scheme.name,
            style = MaterialTheme.typography.labelMedium,
            color = scheme.swatches.getOrElse(0) { MaterialTheme.colorScheme.primary },
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RikkaThemeGrid(
    schemes: List<PresetScheme>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.padding(12.dp),
        maxItemsInEachRow = 4,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        schemes.forEach { scheme ->
            RikkaThemePreview(
                scheme = scheme,
                selected = scheme.id == selectedId,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(scheme.id) },
            )
        }
    }
}
