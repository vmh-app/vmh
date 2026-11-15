package eu.kanade.presentation.reader.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import eu.kanade.presentation.components.AdaptiveSheet
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.reader.appbars.rememberVmhLectorPalette
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.MoreVert
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

/**
 * Dialogo con pestanas del lector, con identidad VMH.
 *
 * Sustituye a `eu.kanade.presentation.components.TabbedDialog` **solo dentro del lector**:
 * ese componente es generico y lo usan varios dialogos de la app, asi que no se toca.
 *
 * Cambios frente a Mihon:
 * - Sin la barra gris `surfaceContainerHigh`: el fondo de las pestanas es el de la superficie.
 * - Texto de la pestana en **mayusculas con tracking**, en color de acento cuando esta activa y
 *   en `onSurface` cuando no.
 * - Indicador de la pestana activa como **filete vertical** de acento en el borde izquierdo, en
 *   vez de la barra horizontal inferior de Material.
 */
@Composable
fun VmhTabbedDialog(
    onDismissRequest: () -> Unit,
    tabTitles: List<String>,
    modifier: Modifier = Modifier,
    tabOverflowMenuContent: (@Composable ColumnScope.(() -> Unit) -> Unit)? = null,
    pagerState: PagerState,
    content: @Composable (Int) -> Unit,
) {
    val palette = rememberVmhLectorPalette()

    AdaptiveSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        val scope = rememberCoroutineScope()

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                ) {
                    tabTitles.fastForEachIndexed { index, tab ->
                        VmhTab(
                            title = tab,
                            selected = pagerState.currentPage == index,
                            accent = palette.acento,
                            textoSeleccionado = palette.acento,
                            textoNoSeleccionado = MaterialTheme.colorScheme.onSurface,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        )
                    }
                }

                tabOverflowMenuContent?.let { VmhMoreMenu(it) }
            }

            HorizontalPager(
                modifier = Modifier.animateContentSize(),
                state = pagerState,
                verticalAlignment = Alignment.Top,
                pageContent = { page -> content(page) },
            )
        }
    }
}

/**
 * Pestana de VMH: el indicador de seleccion es un filete vertical de acento a la izquierda,
 * con el mismo lenguaje que los encabezados de seccion.
 */
@Composable
private fun VmhTab(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean,
    accent: Color,
    textoSeleccionado: Color,
    textoNoSeleccionado: Color,
    onClick: () -> Unit,
) {
    val colorTexto = if (selected) textoSeleccionado else textoNoSeleccionado

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) accent else Color.Transparent),
        )
        Text(
            text = title.uppercase(),
            modifier = Modifier.padding(start = 6.dp, end = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                color = colorTexto,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun VmhMoreMenu(
    content: @Composable ColumnScope.(() -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = MaterialSymbols.Rounded.MoreVert,
                contentDescription = stringResource(MR.strings.label_more),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            content { expanded = false }
        }
    }
}
