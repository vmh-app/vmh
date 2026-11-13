package eu.kanade.presentation.manga.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import coil3.compose.AsyncImage
import eu.kanade.presentation.util.rememberResourceBitmapPainter
import eu.kanade.tachiyomi.R
import tachiyomi.presentation.core.components.VmhCoverShape

enum class MangaCover(val ratio: Float) {
    Square(1f / 1f),
    Book(2f / 3f),
    ;

    /**
     * Dibuja la portada.
     *
     * @param filete contenido que se dibuja **encima** de la imagen y **dentro** del mismo
     *   `clip` de la [shape]. Sirve para el filete Lomo de la vista de detalle de manga.
     *
     *   **Por que va dentro del `Box` y no en el modificador de la imagen.** Un `drawBehind`
     *   se pinta ANTES del contenido, asi que la imagen lo tapa (0 px). Un `drawWithContent`
     *   en el modificador de la imagen se pinta despues, pero el `.clip(shape)` que lleva el
     *   propio `AsyncImage` recorta el trazo justo en el borde (0 px). Por eso el `clip` se
     *   mueve al `Box` contenedor: el `Box` recorta la imagen **y** el filete, y el filete
     *   queda por encima porque es el ultimo hijo que se dibuja.
     */
    @Composable
    operator fun invoke(
        data: Any?,
        modifier: Modifier = Modifier,
        contentDescription: String = "",
        shape: Shape = VmhCoverShape,
        onClick: (() -> Unit)? = null,
        filete: (@Composable BoxScope.() -> Unit)? = null,
    ) {
        Box(
            modifier = modifier
                .aspectRatio(ratio)
                .clip(shape),
        ) {
            AsyncImage(
                model = data,
                placeholder = ColorPainter(CoverPlaceholderColor),
                error = rememberResourceBitmapPainter(id = R.drawable.cover_error),
                contentDescription = contentDescription,
                modifier = Modifier
                    .matchParentSize()
                    .then(
                        if (onClick != null) {
                            Modifier.clickable(
                                role = Role.Button,
                                onClick = onClick,
                            )
                        } else {
                            Modifier
                        },
                    ),
                contentScale = ContentScale.Crop,
            )
            filete?.invoke(this)
        }
    }
}

private val CoverPlaceholderColor = Color(0x1F888888)
