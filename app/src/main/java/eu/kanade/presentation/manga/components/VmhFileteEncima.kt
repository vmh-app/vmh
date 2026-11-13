package eu.kanade.presentation.manga.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Filete de acento de la portada del detalle de manga: el mismo trazo curvo de
 * [eu.kanade.presentation.reader.appbars.fileteLomo], pero **dibujado encima del contenido**.
 *
 * **Por que hace falta una variante.** `fileteLomo` es un `drawBehind`: se dibuja ANTES del
 * contenido del nodo. Sobre un contenedor con superficie (la banda «Empezar», la capsula
 * flotante) eso funciona, porque el `background` y el `border` se pintan antes y el filete
 * queda encima. Pero la **portada es una imagen**: la imagen se pinta despues y tapa el
 * filete por completo. Medido en la captura real (b40): la portada mostraba **1 px** de borde
 * (`#8EA6EB`, x=57) y ningun filete.
 *
 * Esta variante usa `drawWithContent` y dibuja el trazo **despues** de `drawContent()`, de modo
 * que el filete queda por encima de la portada.
 *
 * @param radio radio de esquina en dp; debe coincidir con el `RoundedCornerShape` del contenedor.
 * @param color color del filete.
 * @param grosor ancho del filete en dp.
 * @param bordeBorde ancho en dp de la linea de `border`; el filete se apoya sobre su mitad interior.
 */
fun Modifier.fileteLomoEncima(
    radio: Dp,
    color: Color,
    grosor: Dp = 3.dp,
    bordeBorde: Dp = 1.dp,
) = drawWithContent {
    drawContent()

    val r = radio.toPx()
    val dentroBorde = bordeBorde.toPx() / 2f
    val radioFilete = (r - dentroBorde).coerceAtLeast(0f)

    val trazo = Path().apply {
        // Arco superior: de A = (rf, d) (angulo 270) a B = (d, rf) (angulo 180), sweep = -90.
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
        // Tramo recto vertical del lado izquierdo.
        lineTo(dentroBorde, size.height - radioFilete)
        // Arco inferior: de C (angulo 180) a D = (rf, alto - d) (angulo 90), sweep = -90.
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
