package tachiyomi.presentation.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Piezas compartidas del sistema visual de VMH.
 *
 * El fork no reutiliza la composición de Mihon: cada cabecera lleva un
 * antetítulo en ámbar con guion, el título en peso alto y un contador en
 * pastilla con borde. Son las mismas piezas del onboarding, la biblioteca,
 * el detalle de serie y el lector.
 */

/** Radio propio de VMH para portadas y tarjetas. */
val VmhCoverShape = RoundedCornerShape(10.dp)

/** Radio de las tarjetas de contenido. */
val VmhCardShape = RoundedCornerShape(22.dp)

/** Radio de las pastillas de acción. */
val VmhPillShape = CircleShape

/**
 * Antetítulo de pantalla: guion corto + texto en mayúsculas muy espaciado.
 *
 * Es la firma tipográfica de VMH; sustituye a los títulos planos de Mihon.
 */
@Composable
fun VmhEyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    showDash: Boolean = true,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showDash) {
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Contador en pastilla con borde del acento. Reemplaza a la pastilla rellena de Mihon.
 */
@Composable
fun VmhCountPill(
    text: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(VmhPillShape)
            .background(if (highlighted) colors.primary else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (highlighted) colors.primary else colors.primary.copy(alpha = 0.45f),
                shape = VmhPillShape,
            )
            .padding(horizontal = 9.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            color = if (highlighted) colors.onPrimary else colors.primary,
            maxLines = 1,
        )
    }
}

/**
 * Cabecera única de pantalla: barra de acento, antetítulo, título y accesorio opcional.
 *
 * Es la misma pieza en Inicio, Biblioteca, Fuentes y Historial. Antes cada pantalla armaba su
 * cabecera por su cuenta y los títulos salían con tres tamaños y tres pesos distintos.
 *
 * @param eyebrow antetítulo en mayúsculas espaciadas (por ejemplo el nombre de la app o la sección).
 * @param title título de la pantalla. Siempre `headlineSmall` en negrita.
 * @param subtitle línea de apoyo opcional bajo el título.
 * @param trailing acciones al final de la fila (pastilla de conteo, botones redondos).
 */
@Composable
fun VmhScreenHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .width(3.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.primary),
        )
        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = colors.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

/**
 * Etiqueta de sección: mayúsculas espaciadas en el color secundario.
 */
@Composable
fun VmhSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.4.sp,
        color = MaterialTheme.colorScheme.primary,
    )
}

/**
 * Tarjeta de contenido de VMH: superficie del tema, borde tenue y radio grande.
 */
@Composable
fun VmhCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = VmhCardShape,
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.7f)),
        content = content,
    )
}

/**
 * Fila de metadatos en pastillas: usado en el detalle de serie.
 */
@Composable
fun VmhMetaPill(
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(VmhPillShape)
            .border(1.dp, colors.outlineVariant, VmhPillShape)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Contenedor de una fila de pastillas de metadatos.
 */
@Composable
fun VmhMetaRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
