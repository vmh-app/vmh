package eu.kanade.presentation.reader.appbars

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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

/**
 * Filete vertical de acento pegado al borde izquierdo de un contenedor redondeado.
 *
 * El filete es un **trazo (stroke)** sobre el perimetro redondeado del contenedor, recortado a
 * la mitad izquierda. Ese trazo tiene grosor constante y recorre las dos esquinas de ese lado,
 * asi que la curva se lee limpia de punta a punta.
 *
 * **Por que no una franja vertical rellena.** La version anterior dibujaba el path completo y lo
 * recortaba con `clipRect` a una franja vertical de ancho constante. Medido en la captura real de
 * la capsula de progreso: el ancho del acento caia de 12 px a 4 px al entrar en la esquina y
 * saltaba de golpe al salir de ella. El resultado era una **columna recta de ancho completo** en
 * el tramo recto del lado izquierdo, separada del arco, que se leia como una barra pegada al
 * borde y no como parte de la curva. Un stroke no tiene ese escalon.
 *
 * **Por que el path es solo del lado izquierdo.** Dibujar el perimetro completo y recortarlo con
 * `clipRect` dejaba pasar tambien el trazo del borde **horizontal** de la capsula, y ese trazo se
 * sumaba al arco en las esquinas. Medido: el grosor horizontal del acento iba de 7 px en el tramo
 * recto a 11 px en la esquina y 26 px en la punta del arco. Las esquinas se veian de distinto
 * peso que el centro: el filete «disparejo». Con un path que recorre solo la izquierda (arco
 * superior + tramo recto + arco inferior), el trazo es una sola linea continua de grosor igual.
 *
 * **Por que no `drawRect`.** El rectángulo es recto y sobresale por fuera de la curva de la
 * `RoundedCornerShape`, dejando el borde cortado a la izquierda. Medido en la capsula de acciones:
 * el filete arrancaba 3 px antes del borde real de la tinta.
 *
 * @param radio radio de esquina en dp; debe ser el mismo que usa el `border` del contenedor.
 * @param color color del filete.
 * @param grosor ancho del filete en dp.
 * @param bordeBorde ancho en dp de la linea de `border` del contenedor. Compose dibuja esa linea
 *   centrada en el perimetro, con la mitad exterior por fuera del layout; el filete se dibuja
 *   desplazado la misma mitad hacia afuera, para que el borde quede por dentro y el filete por
 *   fuera, sin invadir la superficie.
 */
fun Modifier.fileteLomo(
    radio: Dp,
    color: Color,
    grosor: Dp = 4.dp,
    bordeBorde: Dp = 1.dp,
) = drawBehind {
    val r = radio.toPx()
    // El filete es un TRAZO (stroke) sobre el perimetro redondeado del contenedor, no una franja
    // vertical rellena.
    // Un stroke tiene grosor constante y sigue la curva de punta a punta; una franja
    // vertical recortada dejaba una columna recta de ancho completo en el tramo recto del
    // lado izquierdo, que se leia como una barra pegada y no como parte de la curva.
    //
    // El `border` de Compose es centrado en el perimetro: ocupa media linea por dentro y media
    // por fuera. Para que el filete quede pegado al borde SIN taparlo, su trazo se apoya sobre
    // la mitad interior del `border`: el path del filete se desplaza hacia adentro la mitad
    // del grosor del borde y reduce su radio en la misma medida, de modo que el filete corre
    // justo por dentro de la linea, sin invadir la superficie ni quedar por fuera del limite.
    val dentroBorde = bordeBorde.toPx() / 2f
    val radioFilete = (r - dentroBorde).coerceAtLeast(0f)

    // Path del SOLO lado izquierdo: arco superior + tramo recto + arco inferior, unidos.
    //
    // No se recorta el perimetro completo con un `clipRect`: el recorte dejaba pasar tambien el
    // trazo del borde HORIZONTAL de la capsula, y ese trazo se sumaba al arco en las esquinas.
    // Medido en la captura real: el grosor horizontal del acento pasaba de 7 px en el tramo
    // recto a 11 px en la esquina, y a 26 px en la punta del arco. Las dos esquinas se veian de
    // distinto peso que el centro: el filete «disparejo».
    //
    // Con este path el trazo corre solo por la izquierda: la curva de una sola linea continua,
    // de grosor igual en todo su recorrido.
    val trazo = Path().apply {
        // Geometria EXACTA (simulada y medida antes de portar a Kotlin):
        //   centro del arco superior = (d, d), radio = r - d
        //   centro del arco inferior = (d, alto - d), radio = r - d
        //   el tramo recto vertical corre en x = d
        // En Android los angulos crecen en sentido horario: 0 = este, 90 = sur (abajo),
        // 180 = oeste, 270 = norte (arriba).
        //
        // Arco superior: de A = (rf, d) (angulo 270 del centro) a B = (d, rf) (angulo 180),
        // pasando por arriba-izquierda: sweep = -90.
        moveTo(radioFilete, dentroBorde)
        arcTo(
            rect = Rect(
                left = dentroBorde,
                top = dentroBorde,
                right = dentroBorde + radioFilete * 2f,
                bottom = dentroBorde + radioFilete * 2f,
            ),
            startAngleDegrees = 270f,
            sweepAngleDegrees = -90f,
            forceMoveTo = false,
        )
        // Tramo recto vertical del lado izquierdo: de B = (d, rf) a C = (d, alto - rf).
        lineTo(dentroBorde, size.height - radioFilete)
        // Arco inferior: de C (angulo 180 del centro inferior) a D = (rf, alto - d) (angulo 90),
        // pasando por abajo-izquierda: sweep = -90.
        arcTo(
            rect = Rect(
                left = dentroBorde,
                top = size.height - dentroBorde - radioFilete * 2f,
                right = dentroBorde + radioFilete * 2f,
                bottom = size.height - dentroBorde,
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -90f,
            forceMoveTo = false,
        )
    }

    drawPath(
        path = trazo,
        color = color,
        style = Stroke(
            width = grosor.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}
