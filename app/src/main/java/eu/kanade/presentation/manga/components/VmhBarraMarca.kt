package eu.kanade.presentation.manga.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.VmhBackCircle
import eu.kanade.presentation.components.VmhCircleAction
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Close
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

/**
 * Barra superior del detalle de manga.
 *
 * Sigue la misma piel que las demas cabeceras de VMH (Fuentes, Historial): superficie clara,
 * circulos de 40 dp para volver y para las acciones, y el filete de marca abajo en color primario.
 * Se diferencia de la barra de Mihon en que no se rellena con el color de la serie ni aparece al
 * hacer scroll: el titulo se desvanece hacia la derecha cuando la cabecera de la obra ya salio de
 * pantalla, para no repetirlo debajo.
 *
 * @param titulo texto del titulo; en modo seleccion recibe el contador.
 * @param mostrarTitulo si el titulo debe verse.
 * @param onNavigateUp callback del boton de retroceso.
 * @param onCancelActionMode callback del circulo de cerrar en modo seleccion.
 * @param enModoAccion cambia el circulo de retroceso por el de cerrar seleccion.
 * @param contenidoAcciones acciones de la derecha.
 */
@Composable
fun VmhBarraMarca(
    titulo: String,
    mostrarTitulo: Boolean,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    onCancelActionMode: () -> Unit = {},
    enModoAccion: Boolean = false,
    contenidoAcciones: @Composable RowScope.() -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val alfaTitulo by animateFloatAsState(
        targetValue = if (mostrarTitulo) 1f else 0f,
        label = "vmh_barra_titulo",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            // La barra ocupa la franja de la barra de estado del sistema: sin este padding los
            // circulos de accion quedan debajo del reloj y la bateria.
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (enModoAccion) {
                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.Close,
                    contentDescription = stringResource(MR.strings.action_cancel),
                    onClick = onCancelActionMode,
                )
            } else {
                VmhBackCircle(onClick = onNavigateUp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = titulo,
                modifier = Modifier
                    .weight(1f)
                    .alpha(alfaTitulo),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                contenidoAcciones()
            }
        }
        // Filete de marca: la firma de las cabeceras de VMH.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(colors.primary),
        )
    }
}
