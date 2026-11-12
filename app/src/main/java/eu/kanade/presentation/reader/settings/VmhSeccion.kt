package eu.kanade.presentation.reader.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.reader.appbars.rememberVmhLectorPalette
import tachiyomi.presentation.core.i18n.stringResource
import java.util.Locale

/**
 * Identidad visual de las secciones del dialogo del lector (VMH).
 *
 * Mismo lenguaje que la barra superior del lector: superficie azul tinta con filete vertical
 * de acento, texto en mayusculas con tracking. Los chips seleccionados se pintan de la
 * superficie de marca con texto de alto contraste; los no seleccionados llevan borde de acento
 * y fondo neutro, para que la seccion pertenezca a la app y no al material por defecto.
 */
private val vmhChipShape = RoundedCornerShape(8.dp)

/**
 * Titulo de seccion del dialogo del lector.
 *
 * Filete vertical de acento a la izquierda + texto en mayusculas con tracking, igual que el
 * titulo del manga sobre la barra del lector.
 */
@Composable
fun VmhSeccionTitulo(labelRes: StringResource) {
    VmhSeccionTitulo(text = stringResource(labelRes))
}

@Composable
fun VmhSeccionTitulo(text: String) {
    val palette = rememberVmhLectorPalette()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(palette.acento, RoundedCornerShape(2.dp)),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelMedium,
            color = palette.acento,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Chip de opcion del dialogo del lector.
 *
 * - Seleccionado: superficie de marca (tinta en oscuro) con texto de alto contraste.
 * - No seleccionado: fondo transparente, texto en el color del tema, borde de acento.
 */
@Composable
fun VmhChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
) {
    val palette = rememberVmhLectorPalette()
    val bgColor = if (selected) palette.superficie else Color.Transparent
    val textColor = if (selected) palette.texto else MaterialTheme.colorScheme.onSurface
    val borderColor = if (selected) palette.superficie else palette.acento

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(bgColor, vmhChipShape)
            .border(1.dp, borderColor, vmhChipShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        androidx.compose.material3.ProvideTextStyle(
            MaterialTheme.typography.labelMedium.copy(color = textColor),
        ) {
            label()
        }
    }
}

/**
 * Fila de titulo + chips, equivalente visual de [SettingsChipRow] pero con identidad VMH.
 */
@Composable
fun VmhChipRow(
    labelRes: StringResource,
    content: @Composable FlowRowScope.() -> Unit,
) {
    VmhChipRow(label = stringResource(labelRes), content = content)
}

@Composable
fun VmhChipRow(
    label: String,
    content: @Composable FlowRowScope.() -> Unit,
) {
    Column {
        VmhSeccionTitulo(text = label)
        FlowRow(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

/**
 * Boton de opcion con icono, para los dialogos de seleccion del lector.
 *
 * Reemplaza al `IconToggleButton` de Mihon: en vez del relleno tonal del tema, usa la superficie de
 * marca cuando esta seleccionado y un borde de acento cuando no.
 */
@Composable
fun VmhIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    imageVector: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
) {
    val palette = rememberVmhLectorPalette()
    val containerColor = if (checked) palette.superficie else Color.Transparent
    val contentColor = if (checked) palette.texto else MaterialTheme.colorScheme.onSurface
    val borderColor = if (checked) palette.superficie else palette.acento

    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = contentColor,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}