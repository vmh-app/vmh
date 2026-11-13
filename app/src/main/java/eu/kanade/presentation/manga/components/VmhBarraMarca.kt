package eu.kanade.presentation.manga.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.UpIcon
import tachiyomi.presentation.core.components.rememberVmhMangaPalette

/**
 * Barra superior de marca para el detalle de manga.
 *
 * Sustituye a la `AppBar` de Mihon: superficie de color primario con el filete de acento abajo.
 * Es **fija**: el titulo de la serie no aparece al hacer scroll, porque sobre la imagen de portada
 * perderia contraste y el color de fondo de la serie es arbitrario.
 *
 * @param titulo texto del titulo.
 * @param mostrarTitulo si el titulo debe verse; si es falso se oculta con una transicion de opacidad.
 * @param onNavigateUp callback del boton de retroceso.
 * @param onCancelActionMode callback para salir del modo seleccion.
 * @param contenidoAcciones acciones de la derecha.
 */
@Composable
fun VmhBarraMarca(
    titulo: String,
    mostrarTitulo: Boolean,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    onCancelActionMode: () -> Unit = {},
    contenidoAcciones: @Composable RowScope.() -> Unit = {},
) {
    val palette = rememberVmhMangaPalette()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.superficie)
            // La barra de marca ocupa la franja de la barra de estado del sistema: sin este
            // padding, los iconos de descarga, filtro y overflow quedan debajo del reloj y la
            // bateria.
            .statusBarsPadding(),
    ) {
        CompositionLocalProvider(LocalContentColor provides palette.texto) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateUp) {
                    UpIcon()
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = titulo,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(if (mostrarTitulo) 1f else 0f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.texto,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                contenidoAcciones()
            }
        }
        // Filete de acento: la firma de la barra de marca de VMH.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(palette.acento),
        )
    }
}
