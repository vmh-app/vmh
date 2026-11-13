package eu.kanade.presentation.manga.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.reader.appbars.fileteLomo
import tachiyomi.presentation.core.components.rememberVmhMangaPalette


/**
 * Banda de accion principal del detalle de manga.
 *
 * Sustituye al `ElevatedButton` azul de Mihon: una banda de ancho completo sobre la superficie de
 * marca del tema, con el filete Lomo en el borde izquierdo.
 *
 * El filete se dibuja con la **misma cadena de modificadores que usan las capsulas del lector**
 * (`clip` -> `background` -> `border` -> `fileteLomo`) sobre un `Box`. No puede ir sobre un
 * `Surface`: la `Surface` pinta su propio fondo por encima del `drawBehind` del filete y lo
 * tapa. Medido en la captura real: la banda salia 100 % `#1D4ED8`, sin un solo pixel de acento.
 *
 * @param texto texto del boton.
 * @param icono vector del icono de accion.
 * @param onClick callback del boton.
 */
@Composable
fun VmhBandaAccion(
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = rememberVmhMangaPalette()
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.superficie)
            .border(1.dp, palette.acento.copy(alpha = 0.55f), shape)
            .fileteLomo(
                radio = 22.dp,
                color = palette.acento,
                grosor = 4.dp,
                bordeBorde = 1.dp,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(start = 28.dp, end = 20.dp, top = 13.dp, bottom = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = palette.texto,
            )
            Text(
                text = texto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.texto,
            )
        }
    }
}

/**
 * Capsula flotante de accion del detalle de manga.
 *
 * Sustituye al `SmallExtendedFloatingActionButton` de Mihon por la misma capsula con filete Lomo
 * que usan las barras flotantes del lector.
 *
 * @param texto texto de la capsula.
 * @param icono vector del icono de accion.
 * @param onClick callback de la capsula.
 */
@Composable
fun VmhCapsulaAccion(
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = rememberVmhMangaPalette()
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .shadow(12.dp, shape, clip = false)
            .clip(shape)
            .background(palette.superficie)
            .border(1.dp, palette.acento.copy(alpha = 0.55f), shape)
            .fileteLomo(
                radio = 22.dp,
                color = palette.acento,
                grosor = 4.dp,
                bordeBorde = 1.dp,
            ),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(start = 26.dp, end = 18.dp, top = 11.dp, bottom = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = palette.texto,
            )
            Text(
                text = texto,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = palette.texto,
            )
        }
    }
}

/**
 * Pastilla circular con borde para los iconos de la fila de acciones.
 *
 * El icono va dentro de un circulo con borde, no en un recuadro: es el mismo gesto de las
 * metacapsas de VMH, y separa el icono de la etiqueta que va debajo.
 *
 * @param color color del borde y del icono.
 */
@Composable
fun VmhPastillaIcono(
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .border(1.dp, color, CircleShape),
        contentAlignment = Alignment.Center,
        content = content,
    )
}
