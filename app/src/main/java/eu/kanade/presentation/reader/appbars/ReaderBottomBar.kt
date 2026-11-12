package eu.kanade.presentation.reader.appbars

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import mihon.icons.custommaterialsymbols.CustomMaterialSymbols
import mihon.icons.custommaterialsymbols.rounded.CropOff
import mihon.icons.custommaterialsymbols.rounded.CropOn
import mihon.icons.custommaterialsymbols.rounded.SettingsSliders
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ReaderBottomBar(
    readingMode: ReadingMode,
    onClickReadingMode: () -> Unit,
    orientation: ReaderOrientation,
    onClickOrientation: () -> Unit,
    cropEnabled: Boolean,
    onClickCropBorder: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = rememberVmhLectorPalette()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onClickReadingMode),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = readingMode.icon,
                contentDescription = stringResource(MR.strings.viewer),
                modifier = Modifier.size(20.dp),
                tint = palette.acento,
            )
        }
        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(palette.separador))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onClickOrientation),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = orientation.icon,
                contentDescription = stringResource(MR.strings.rotation_type),
                modifier = Modifier.size(20.dp),
                tint = palette.acento,
            )
        }
        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(palette.separador))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onClickCropBorder),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (cropEnabled) {
                    CustomMaterialSymbols.Rounded.CropOn
                } else {
                    CustomMaterialSymbols.Rounded.CropOff
                },
                contentDescription = stringResource(MR.strings.pref_crop_borders),
                modifier = Modifier.size(20.dp),
                tint = if (cropEnabled) {
                    palette.coralActivo
                } else {
                    palette.acento
                },
            )
            if (cropEnabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(palette.coralActivo),
                )
            }
        }
        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(palette.separador))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onClickSettings),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = CustomMaterialSymbols.Rounded.SettingsSliders,
                contentDescription = stringResource(MR.strings.action_settings),
                modifier = Modifier.size(20.dp),
                tint = palette.acento,
            )
        }
    }
}
