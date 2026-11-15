package eu.kanade.presentation.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Paleta del banner "Destacados de hoy" del Inicio.
 *
 * La version anterior era una tarjeta casi blanca con degradados al 10-20 %: en la captura real se
 * veia como un panel plano, sin contraste contra la pagina. El banner pasa a ser una tarjeta de
 * tinta oscura con luz propia: el contraste alto es lo que le da volumen sobre el fondo claro.
 *
 * Los dos temas comparten la tinta (el banner es siempre oscuro, como una portada), pero en tema
 * oscuro el borde y los brillos suben de intensidad para que la tarjeta se separe del fondo negro.
 */
@Immutable
data class VmhHeroPalette(
    /** Tinta de arriba de la tarjeta. */
    val fondoArriba: Color,
    /** Tinta de abajo de la tarjeta. */
    val fondoAbajo: Color,
    /** Azul del foco principal (esquina superior derecha). */
    val brillo: Color,
    /** Cian del foco secundario (esquina inferior izquierda) y de la flecha. */
    val brilloCian: Color,
    /** Texto principal sobre la tinta. */
    val texto: Color,
    /** Texto secundario sobre la tinta. */
    val textoSuave: Color,
    /** Acento de rotulo, puntos y filetes. */
    val acento: Color,
    /** Borde exterior de la tarjeta. */
    val borde: Color,
    /** Fondo de la pastilla de la fuente. */
    val chipFondo: Color,
    /** Borde de la pastilla de la fuente. */
    val chipBorde: Color,
    /** Inicio del degradado del boton de flecha. */
    val flechaInicio: Color,
    /** Fin del degradado del boton de flecha. */
    val flechaFin: Color,
    /** Glifo sobre el boton de flecha. */
    val flechaIcono: Color,
    /** Filete de luz del borde superior. */
    val luzSuperior: Color,
)

private val TintaArriba = Color(0xFF102C58)
private val TintaAbajo = Color(0xFF06132B)
private val AzulFoco = Color(0xFF3D7BFF)
private val CianFoco = Color(0xFF54C6FF)
private val TextoHeroe = Color(0xFFF5F8FF)
private val TextoHeroeSuave = Color(0xFFAFC2E2)
private val AcentoHeroe = Color(0xFF86B6FF)
private val FlechaIcono = Color(0xFF04122B)

@Composable
@ReadOnlyComposable
fun rememberVmhHeroPalette(): VmhHeroPalette {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return VmhHeroPalette(
        fondoArriba = TintaArriba,
        fondoAbajo = TintaAbajo,
        brillo = AzulFoco,
        brilloCian = CianFoco,
        texto = TextoHeroe,
        textoSuave = TextoHeroeSuave,
        acento = AcentoHeroe,
        borde = Color.White.copy(alpha = if (isDark) 0.18f else 0.10f),
        chipFondo = Color.White.copy(alpha = 0.12f),
        chipBorde = Color.White.copy(alpha = if (isDark) 0.28f else 0.20f),
        flechaInicio = Color(0xFF6C9DFF),
        flechaFin = Color(0xFF3EC8FF),
        flechaIcono = FlechaIcono,
        luzSuperior = Color.White.copy(alpha = if (isDark) 0.22f else 0.16f),
    )
}
