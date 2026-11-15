package eu.kanade.presentation.more.settings.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.VmhSliderThumb
import tachiyomi.presentation.core.components.material.VmhSliderTrack

// Piezas compartidas por las pantallas de Ajustes de VMH. Viven aqui para que cada pantalla no
// las redeclare (las clases top-level chocan entre archivos del mismo paquete).

internal data class ListDialogData<T>(
    val title: String,
    val entries: Map<T, String>,
    val selectedValue: T,
    val onSelect: (T) -> Unit,
)

internal data class MultiListDialogData<T>(
    val title: String,
    val entries: Map<T, String>,
    val selectedValues: Set<T>,
    val onSelect: (Set<T>) -> Unit,
)

@Composable
internal fun VmhSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        modifier = Modifier.padding(start = 32.dp, top = 16.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.6.sp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VmhSettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    ) {
        BoxWithConstraints {
            val isWide = maxWidth > 500.dp
            CompositionLocalProvider(LocalIsWide provides isWide) {
                FlowRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    maxItemsInEachRow = if (isWide) 2 else 1,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
internal fun VmhSettingsCardColumn(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp), content = content)
    }
}

@Composable
internal fun VmhInfoText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun VmhTextPreference(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    val isWide = LocalIsWide.current
    Row(
        modifier = Modifier
            .fillMaxWidth(if (isWide) 0.5f else 1f)
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(16.dp))
            trailing()
        }
    }
}

@Composable
internal fun VmhSwitchPreference(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val isWide = LocalIsWide.current
    Row(
        modifier = Modifier
            .fillMaxWidth(if (isWide) 0.5f else 1f)
            .heightIn(min = 64.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                checkedTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
            ),
        )
    }
}

@Composable
internal fun VmhSliderPreference(
    title: String,
    value: Int,
    valueRange: ClosedRange<Int>,
    steps: Int = 0,
    valueText: (Int) -> String = { it.toString() },
    onValueChange: (Int) -> Unit,
) {
    val isWide = LocalIsWide.current
    Column(
        modifier = Modifier
            .fillMaxWidth(if (isWide) 0.5f else 1f)
            .heightIn(min = 64.dp)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                valueText(value),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        val sliderColors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
        )
        val sliderRange = valueRange.start.toFloat()..valueRange.endInclusive.toFloat()
        val sliderState = rememberSliderState(
            value = value.toFloat(),
            steps = steps,
            trackRange = sliderRange,
        )
        // El estado se sincroniza en un efecto y no durante el dibujado: escribir el valor del
        // slider en la fase de composicion hace que el control pelee con el arrastre.
        LaunchedEffect(value) {
            if (sliderState.value != value.toFloat()) {
                sliderState.value = value.toFloat()
            }
        }
        Slider(
            state = sliderState,
            onValueChange = { onValueChange(it.toInt()) },
            colors = sliderColors,
            thumb = { VmhSliderThumb(colors = sliderColors) },
            track = { state ->
                VmhSliderTrack(
                    state = state,
                    steps = steps,
                    valueRange = sliderRange,
                    colors = sliderColors,
                )
            },
        )
    }
}

@Composable
internal fun <T> VmhListDialog(
    title: String,
    entries: Map<T, String>,
    selectedValue: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    warning: String? = null,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            LazyColumn {
                items(entries.keys.toList()) { key ->
                    val label = entries[key]!!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(key) }
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = key == selectedValue,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(context.stringResource(MR.strings.action_cancel), color = MaterialTheme.colorScheme.error)
            }
        },
    )
}

@Composable
internal fun <T> VmhMultiSelectDialog(
    title: String,
    entries: Map<T, String>,
    selectedValues: Set<T>,
    onSelect: (Set<T>) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            LazyColumn {
                items(entries.keys.toList()) { key ->
                    val label = entries[key]!!
                    val checked = selectedValues.contains(key)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newSet = if (checked) selectedValues - key else selectedValues + key
                                onSelect(newSet)
                            }
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(context.stringResource(MR.strings.action_ok), color = MaterialTheme.colorScheme.primary)
            }
        },
    )
}
