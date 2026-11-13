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
 * Filete de acento dibujado **encima** del contenido, sobre el borde izquierdo de un
 * contenedor: el mismo trazo curvo de
 * [eu.kanade.presentation.reader.appbars.fileteLomo], pero **por encima** de la imagen.
 *
 * **Por que hace falta una variante.** `fileteLomo` es un `drawBehind`: se dibuja ANTES del
 * contenido del nodo. Sobre un contenedor con superficie (la banda «Empezar», la capsula
 * flotante) eso funciona, porque el `background` y el `border` se pintan antes y el filete
 * queda encima. Pero la **portada es una imagen**: la imagen se pinta despues y tapa el
 * filete por completo.
 *
 * Esta variante usa `drawWithContent` y dibuja el trazo **despues** de `drawContent()`, de modo
 * que el filete queda por encima de la portada.
 *
 * **La geometria importa (vault §37).** El trazo se apoya **hacia adentro** del borde, con su
 * centro a `grosor / 2` del borde. Si se centrara en el borde, la mitad exterior quedaria fuera
 * del `clip` del contenedor y se perderia; el `clip` recorta exactamente en el borde.
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

    // El trazo se apoya **pegado al borde interior** del contenedor: su CENTRO queda a
    // `grosor` px del borde, de modo que todo el ancho del trazo queda DENTRO del `clip`.
    //
    // La version anterior ponia el centro a `grosor / 2` (trazo centrado en el borde): la mitad
    // exterior se perdia contra el recorte y solo quedaba la mitad interior. Con el trazo
    // completamente dentro, el filete sale con su grosor completo.
    //
    // Medido en b53 (prueba con rojo de 20 dp): el arco y el tramo vertical se dibujan
    // completos; lo que faltaba era que el trazo quedara dentro del recorte.
    val grosorPx = grosor.toPx()
    val inicioX = dentroBorde + grosorPx
    val inicioY = dentroBorde + grosorPx

    val trazo = Path().apply {
        // Arco superior: de A = (rf + x, y) (angulo 270) a B = (x, rf + y) (angulo 180).
        moveTo(radioFilete + inicioX, inicioY)
        arcTo(
            rect = Rect(
                left = inicioX,
                top = inicioY,
                right = inicioX + radioFilete * 2f,
                bottom = inicioY + radioFilete * 2f,
            ),
            startAngleDegrees = 270f,
            sweepAngleDegrees = -90f,
            forceMoveTo = false,
        )
        // Tramo recto vertical del lado izquierdo.
        lineTo(inicioX, size.height - radioFilete - inicioY)
        // Arco inferior: de C (angulo 180) a D = (rf + x, alto - y) (angulo 90).
        arcTo(
            rect = Rect(
                left = inicioX,
                top = size.height - inicioY - radioFilete * 2f,
                right = inicioX + radioFilete * 2f,
                bottom = size.height - inicioY,
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
            width = grosorPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}
