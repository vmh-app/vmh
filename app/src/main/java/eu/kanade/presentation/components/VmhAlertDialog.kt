package eu.kanade.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Dialogo de confirmacion de VMH: tarjeta blanca de esquinas 20 dp con borde fino, titulo en
 * negrita, contenido libre y dos acciones (cancelar en texto, confirmar en pastilla).
 */
@Composable
fun VmhAlertDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String,
    destructive: Boolean,
    confirmEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outlineVariant),
            shadowElevation = 8.dp,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                Spacer(Modifier.height(10.dp))
                content()
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(
                            text = dismissLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        onClick = onConfirm,
                        enabled = confirmEnabled,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(17.dp),
                        color = when {
                            !confirmEnabled && destructive -> colors.error.copy(alpha = 0.45f)
                            !confirmEnabled -> colors.primary.copy(alpha = 0.45f)
                            destructive -> colors.error
                            else -> colors.primary
                        },
                        contentColor = if (destructive) colors.onError else colors.onPrimary,
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = confirmLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}
