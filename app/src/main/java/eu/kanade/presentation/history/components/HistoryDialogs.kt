package eu.kanade.presentation.history.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.VmhAlertDialog
import eu.kanade.presentation.components.VmhCheckRow
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun HistoryDeleteDialog(
    onDismissRequest: () -> Unit,
    onDelete: (Boolean) -> Unit,
) {
    var removeEverything by remember { mutableStateOf(false) }

    VmhAlertDialog(
        title = stringResource(MR.strings.action_remove),
        onDismissRequest = onDismissRequest,
        confirmLabel = stringResource(MR.strings.action_remove),
        onConfirm = {
            onDelete(removeEverything)
            onDismissRequest()
        },
        dismissLabel = stringResource(MR.strings.action_cancel),
        destructive = true,
    ) {
        Text(
            text = stringResource(MR.strings.dialog_with_checkbox_remove_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        VmhCheckRow(
            label = stringResource(MR.strings.dialog_with_checkbox_reset),
            checked = removeEverything,
            onClick = { removeEverything = !removeEverything },
            modifier = Modifier.padding(vertical = 2.dp),
        )
    }
}

@Composable
fun HistoryDeleteAllDialog(
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
) {
    VmhAlertDialog(
        title = stringResource(MR.strings.action_remove_everything),
        onDismissRequest = onDismissRequest,
        confirmLabel = stringResource(MR.strings.action_ok),
        onConfirm = {
            onDelete()
            onDismissRequest()
        },
        dismissLabel = stringResource(MR.strings.action_cancel),
        destructive = true,
    ) {
        Text(
            text = stringResource(MR.strings.clear_history_confirmation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@PreviewLightDark
@Composable
private fun HistoryDeleteDialogPreview() {
    TachiyomiPreviewTheme {
        HistoryDeleteDialog(
            onDismissRequest = {},
            onDelete = {},
        )
    }
}
