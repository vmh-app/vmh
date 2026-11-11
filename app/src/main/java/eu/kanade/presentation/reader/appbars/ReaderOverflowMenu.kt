package eu.kanade.presentation.reader.appbars

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val VmhMenuSurface = Color(0xFF142B4D)
private val VmhMenuText = Color(0xFFF7F9FF)

data class ReaderOverflowAction(
    val title: String,
    val onClick: () -> Unit,
)

@Composable
fun ReaderOverflowMenu(
    actions: List<ReaderOverflowAction>,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        containerColor = VmhMenuSurface,
    ) {
        actions.forEach { action ->
            DropdownMenuItem(
                onClick = action.onClick,
                text = {
                    Text(
                        text = action.title,
                        color = VmhMenuText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
            )
        }
    }
}
