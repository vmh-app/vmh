package eu.kanade.presentation.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import eu.kanade.domain.manga.model.readingMode
import eu.kanade.presentation.components.AdaptiveSheet
import eu.kanade.presentation.reader.appbars.rememberVmhLectorPalette
import eu.kanade.presentation.reader.components.ModeSelectionDialog
import eu.kanade.presentation.reader.settings.VmhSeccionTitulo
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsViewModel
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

private val ReadingModesWithoutDefault = ReadingMode.entries - ReadingMode.DEFAULT

@Composable
fun ReadingModeSelectDialog(
    onDismissRequest: () -> Unit,
    viewModel: ReaderSettingsViewModel,
    onChange: (StringResource) -> Unit,
) {
    val manga by viewModel.mangaFlow.collectAsState()
    val readingMode = remember(manga) { ReadingMode.fromPreference(manga?.readingMode?.toInt()) }

    AdaptiveSheet(onDismissRequest = onDismissRequest) {
        DialogContent(
            readingMode = readingMode,
            onChangeReadingMode = {
                viewModel.setReadingMode(it)
                onChange(it.stringRes)
                onDismissRequest()
            },
        )
    }
}

@Composable
private fun DialogContent(
    readingMode: ReadingMode,
    onChangeReadingMode: (ReadingMode) -> Unit,
) {
    var selected by remember { mutableStateOf(readingMode) }

    ModeSelectionDialog(
        onUseDefault = { onChangeReadingMode(ReadingMode.DEFAULT) }.takeIf { readingMode != ReadingMode.DEFAULT },
        onApply = { onChangeReadingMode(selected) },
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            VmhSeccionTitulo(MR.strings.pref_category_reading_mode)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ReadingModesWithoutDefault.forEach { mode ->
                    VmhReaderModeButton(
                        checked = mode == selected,
                        onClick = { selected = mode },
                        imageVector = mode.icon,
                        title = stringResource(mode.stringRes),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun VmhReaderModeButton(
    checked: Boolean,
    onClick: () -> Unit,
    imageVector: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
) {
    val palette = rememberVmhLectorPalette()
    val containerColor = if (checked) {
        palette.superficie
    } else {
        Color.Transparent
    }
    val contentColor = if (checked) {
        palette.texto
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val border = if (checked) {
        BorderStroke(1.dp, palette.superficie)
    } else {
        BorderStroke(1.dp, palette.acento)
    }

    Surface(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = contentColor,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun DialogContentPreview() {
    TachiyomiPreviewTheme {
        Surface {
            Column {
                DialogContent(
                    readingMode = ReadingMode.DEFAULT,
                    onChangeReadingMode = {},
                )

                DialogContent(
                    readingMode = ReadingMode.LEFT_TO_RIGHT,
                    onChangeReadingMode = {},
                )
            }
        }
    }
}
