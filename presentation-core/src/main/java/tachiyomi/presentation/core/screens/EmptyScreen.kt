package tachiyomi.presentation.core.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import dev.icerock.moko.resources.StringResource
import tachiyomi.presentation.core.R
import tachiyomi.presentation.core.i18n.stringResource

data class EmptyScreenAction(
    val stringRes: StringResource,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

/** Poses de la mascota de VMH. Cada estado vacío muestra la que le corresponde. */
enum class EmptyArt(val resId: Int) {
    BOOK(R.drawable.vmh_empty_libro),
    SEARCH(R.drawable.vmh_empty_lupa),
    SLEEP(R.drawable.vmh_empty_sueno),
    BOX(R.drawable.vmh_empty_caja),
    SAD(R.drawable.vmh_empty_triste),
    HAPPY(R.drawable.vmh_empty_alegre),
}

@Composable
fun EmptyScreen(
    stringRes: StringResource,
    modifier: Modifier = Modifier,
    actions: List<EmptyScreenAction>? = null,
    art: EmptyArt = EmptyArt.BOOK,
) {
    EmptyScreen(
        message = stringResource(stringRes),
        modifier = modifier,
        actions = actions,
        art = art,
    )
}

/**
 * Estado vacío de VMH.
 *
 * La mascota de la app ocupa el lugar del viejo libro dibujado: una polilla
 * azul con alas blancas, en la pose que corresponda al contexto (leyendo,
 * buscando, dormida, con una caja vacía o triste). Abajo va el mensaje en dos
 * niveles y, si hay, la acción principal.
 */
@Composable
fun EmptyScreen(
    message: String,
    modifier: Modifier = Modifier,
    actions: List<EmptyScreenAction>? = null,
    art: EmptyArt = EmptyArt.BOOK,
) {
    val colors = MaterialTheme.colorScheme
    val (title, subtitle) = splitMessage(message)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            EmptyMascot(art = art)

            Spacer(Modifier.height(18.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )

            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            if (!actions.isNullOrEmpty()) {
                Spacer(Modifier.height(22.dp))
                actions.fastForEach { action ->
                    Button(
                        onClick = action.onClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                        ),
                        modifier = Modifier
                            .widthIn(min = 200.dp, max = 280.dp)
                            .height(50.dp),
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(action.stringRes),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

/** Parte el mensaje en título y subtítulo por el primer punto seguido de espacio. */
private fun splitMessage(message: String): Pair<String, String?> {
    val idx = message.indexOf(". ")
    return if (idx > 0 && idx < message.length - 2) {
        val titulo = message.substring(0, idx + 1)
        val resto = message.substring(idx + 2).trim()
        titulo to resto.ifEmpty { null }
    } else {
        message to null
    }
}

/** Mascota de VMH con un halo suave detrás para separar el trazo blanco del fondo. */
@Composable
fun EmptyMascot(art: EmptyArt, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier.size(width = 240.dp, height = 216.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(216.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colors.primary.copy(alpha = 0.12f), Color.Transparent),
                ),
                radius = size.minDimension / 2f,
            )
        }

        Image(
            painter = painterResource(art.resId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
