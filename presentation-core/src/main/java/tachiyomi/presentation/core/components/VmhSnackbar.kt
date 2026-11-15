package tachiyomi.presentation.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Aviso de VMH: tarjeta clara con borde, filete azul a la izquierda y acción en el acento.
 *
 * Sustituye al snackbar gris de Material, que era lo último que quedaba con la cara de Mihon.
 * Mantiene la animación y las duraciones del [SnackbarHost] original.
 */
@Composable
fun VmhSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        VmhSnackbar(data = data)
    }
}

@Composable
private fun VmhSnackbar(data: SnackbarData) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    val actionLabel = data.visuals.actionLabel

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = shape)
            .clip(shape)
            .background(colors.surfaceContainerHigh)
            .border(width = 1.dp, color = colors.outlineVariant.copy(alpha = 0.7f), shape = shape)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.primary),
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = data.visuals.message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
        )

        if (actionLabel != null) {
            Spacer(Modifier.width(12.dp))
            Text(
                text = actionLabel.uppercase(),
                modifier = Modifier
                    .clip(VmhPillShape)
                    .clickable(role = Role.Button) { data.performAction() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary,
                maxLines = 1,
            )
        }
    }
}
