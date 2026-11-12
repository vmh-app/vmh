package eu.kanade.presentation.reader.appbars

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Tokens de identidad visual del lector de VMH.
 *
 * Una sola fuente de verdad para la barra superior y las dos capsulas flotantes inferiores.
 * Antes cada pieza definiia sus colores por separado y las capsulas salian en gris neutro
 * (`MaterialTheme.colorScheme.surfaceContainer`), sin ninguna relacion con la barra superior.
 *
 * El patron de la barra superior es:
 *  - superficie de marca
 *  - filete/acento
 *  - texto de alto contraste
 *  - coral para el estado activo
 *
 * Hay dos paletas, una por tema:
 *  - **Oscuro**: la identidad Lomo original. Superficie azul tinta (#0B1F3A) con acento azul claro
 *    (#8AB4FF). Se conserva tal cual porque es la firma del lector.
 *  - **Claro**: la misma estructura pero con la superficie y el acento tomados del tema claro de la
 *    app (`surfaceContainer` y `primary`). Una barra azul tinta sobre un fondo de lectura blanco
 *    quedaba como un bloque foreign: la barra es de la app, no del manga.
 *
 * La paleta se elige por [MaterialTheme.colorScheme], que dentro del lector ya refleja el tema
 * efectivo de la app (ver `BaseActivity.applyAppTheme` y `ThemeMode.setAppCompatDelegateThemeMode`).
 * No se lee el modo noche del sistema: si la app esta forzada a oscuro con el dispositivo en claro,
 * el lector tambien va oscuro.
 */
@Immutable
data class VmhLectorPalette(
    /** Superficie de la barra superior y de las dos capsulas. */
    val superficie: Color,
    /** Acento de marca. Filete vertical de la barra y color de acento de las capsulas. */
    val acento: Color,
    /** Texto de alto contraste sobre [superficie]. */
    val texto: Color,
    /** Coral del estado activo (marcador). */
    val coralActivo: Color,
    /** Color del glifo relleno sobre [coralActivo]. */
    val iconoActivo: Color,
    /** Separador vertical entre los grupos de la barra inferior. */
    val separador: Color,
    /** Relleno de la pista del slider de progreso. */
    val progresoPista: Color,
) {
    /** Relleno del slider de progreso dentro de la capsula superior. */
    val progresoActivo: Color get() = acento
}

/**
 * Superficie azul tinta. Fondo de la barra superior y de las dos capsulas en tema oscuro.
 */
private val LomoAzul = Color(0xFF0B1F3A)

/**
 * Acento lomo azul claro. Filete vertical de la barra y color de acento de las capsulas.
 */
private val LomoAzulClaro = Color(0xFF8AB4FF)

/**
 * Texto blanco frio.
 */
private val TextoClaro = Color(0xFFF7F9FF)

/**
 * Coral del estado activo (marcador). Se mantiene igual en los dos temas: es el color de estado,
 * no de superficie.
 */
private val CoralActivo = Color(0xFFFF8A70)

/**
 * Azul profundo para el glifo relleno sobre coral.
 */
private val IconoActivo = Color(0xFF06214F)

/**
 * Paleta de la barra superior y las capsulas para el tema actual.
 *
 * En tema oscuro devuelve la identidad Lomo original. En tema claro toma la superficie y el
 * acento del tema de la app, para que la barra pertenezca a la app y no al manga.
 */
@Composable
@ReadOnlyComposable
fun rememberVmhLectorPalette(): VmhLectorPalette {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f
    return if (isDark) {
        VmhLectorPalette(
            superficie = LomoAzul,
            acento = LomoAzulClaro,
            texto = TextoClaro,
            coralActivo = CoralActivo,
            iconoActivo = IconoActivo,
            separador = Color(0x33FFFFFF),
            progresoPista = Color(0x4DFFFFFF),
        )
    } else {
        VmhLectorPalette(
            superficie = colorScheme.surfaceContainer,
            acento = colorScheme.primary,
            texto = colorScheme.onSurface,
            coralActivo = CoralActivo,
            iconoActivo = colorScheme.onPrimary,
            separador = Color(0x33000000),
            progresoPista = colorScheme.onSurface.copy(alpha = 0.18f),
        )
    }
}
