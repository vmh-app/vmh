package tachiyomi.presentation.core.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Paleta de la vista de detalle de manga.
 *
 * Todos los colores salen del `colorScheme` del tema: la pantalla de detalle tiene que
 * responder al tema igual que el resto de VMH, y el azul tinta fijo (`#0B1F3A`) es
 * exclusivo de la barra del lector.
 *
 * - [superficie] es la superficie de marca (banda de accion y barra superior).
 * - [acento] es el filete Lomo que dibuja el gesto de la marca.
 * - [texto] es el texto sobre la superficie de marca.
 */
@Immutable
data class VmhMangaPalette(
    val superficie: Color,
    val acento: Color,
    val texto: Color,
    val coral: Color,
    val tarjeta: Color,
    val borde: Color,
    val textoTenue: Color,
)

/**
 * Paleta del detalle de manga derivada del tema activo.
 */
@Composable
fun rememberVmhMangaPalette(): VmhMangaPalette {
    val colors = MaterialTheme.colorScheme
    return VmhMangaPalette(
        superficie = colors.primary,
        acento = colors.onPrimary,
        texto = colors.onPrimary,
        coral = colors.secondary,
        tarjeta = colors.surfaceContainer,
        borde = colors.outlineVariant,
        textoTenue = colors.onSurfaceVariant,
    )
}
