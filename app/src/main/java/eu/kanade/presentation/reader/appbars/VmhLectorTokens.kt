package eu.kanade.presentation.reader.appbars

import androidx.compose.ui.graphics.Color

/**
 * Tokens de identidad visual del lector de VMH.
 *
 * Una sola fuente de verdad para la barra superior y las dos capsulas flotantes inferiores.
 * Antes cada pieza definiaba sus colores por separado y las capsulas salian en gris neutro
 * (`MaterialTheme.colorScheme.surfaceContainer`), sin ninguna relacion con la barra superior.
 *
 * El patron de la barra superior es:
 *  - superficie azul tinta
 *  - filete/acento lomo azul claro
 *  - texto blanco frio
 *  - coral para el estado activo
 */
object VmhLectorTokens {
    /** Superficie azul tinta. Fondo de la barra superior y de las dos capsulas. */
    val LomoAzul = Color(0xFF0B1F3A)

    /** Acento lomo azul claro. Filete vertical de la barra y color de acento de las capsulas. */
    val LomoAzulClaro = Color(0xFF8AB4FF)

    /** Texto blanco frio. */
    val TextoClaro = Color(0xFFF7F9FF)

    /** Coral del estado activo (marcador). */
    val CoralActivo = Color(0xFFFF8A70)

    /** Azul profundo para el glifo relleno sobre coral. */
    val IconoActivo = Color(0xFF06214F)

    /** Separador vertical entre los grupos de la barra inferior. */
    val Separador = Color(0x33FFFFFF)

    /** Relleno del slider de progreso dentro de la capsula superior. */
    val ProgresoActivo = LomoAzulClaro

    /** Relleno de la pista del slider. */
    val ProgresoPista = Color(0x4DFFFFFF)
}
