package eu.kanade.presentation.more.settings.screen.browse.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.VmhAlertDialog
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ExtensionStoreCreateDialog(
    onDismissRequest: () -> Unit,
    onCreate: (String) -> Unit,
    storeIndexUrls: Set<String>,
    processing: Boolean,
    errorMessage: String?,
) {
    val state = rememberTextFieldState()
    val storeAlreadyExists by remember(storeIndexUrls) {
        derivedStateOf {
            val indexUrl = state.text.toString()
            storeIndexUrls.contains(indexUrl)
        }
    }

    val focusRequester = remember { FocusRequester() }

    VmhAlertDialog(
        title = stringResource(MR.strings.extensionStoresScreen_addStore_title),
        onDismissRequest = onDismissRequest,
        confirmLabel = stringResource(
            resource = if (processing) {
                MR.strings.extensionStoresScreen_addStore_processing
            } else {
                MR.strings.action_add
            },
        ),
        onConfirm = { onCreate(state.text.toString()) },
        dismissLabel = stringResource(MR.strings.action_cancel),
        destructive = false,
        confirmEnabled = !processing && state.text.isNotEmpty() && !storeAlreadyExists,
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            state = state,
            label = {
                Text(text = stringResource(MR.strings.extensionStoresScreen_addStoreInput_inputLabel))
            },
            supportingText = {
                val msgRes = if (storeAlreadyExists) {
                    MR.strings.extensionStoresScreen_addStore_alreadyExists
                } else {
                    MR.strings.information_required_plain
                }
                Text(text = errorMessage ?: stringResource(msgRes))
            },
            isError = errorMessage != null || storeAlreadyExists,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            lineLimits = TextFieldLineLimits.SingleLine,
        )
    }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
}

@Composable
fun ExtensionStoreDeleteDialog(
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    storeName: String,
    storeIndexUrl: String,
) {
    VmhAlertDialog(
        title = stringResource(MR.strings.extensionStoresScreen_deleteStore_title),
        onDismissRequest = onDismissRequest,
        confirmLabel = stringResource(MR.strings.action_ok),
        onConfirm = {
            onDelete()
            onDismissRequest()
        },
        dismissLabel = stringResource(MR.strings.action_cancel),
        destructive = true,
    ) {
        Text(text = stringResource(MR.strings.extensionStoresScreen_deleteStore_body, storeName, storeIndexUrl))
    }
}

@Composable
fun ExtensionStoreConfirmDialog(
    onDismissRequest: () -> Unit,
    onCreate: () -> Unit,
    storeIndexUrl: String,
    storeAlreadyExists: Boolean,
    processing: Boolean,
    errorMessage: String?,
) {
    val state = rememberTextFieldState(initialText = storeIndexUrl)
    VmhAlertDialog(
        title = stringResource(MR.strings.extensionStoresScreen_addStore_title),
        onDismissRequest = onDismissRequest,
        confirmLabel = stringResource(
            resource = if (processing) {
                MR.strings.extensionStoresScreen_addStore_processing
            } else {
                MR.strings.action_add
            },
        ),
        onConfirm = onCreate,
        dismissLabel = stringResource(MR.strings.action_cancel),
        destructive = false,
        confirmEnabled = !storeAlreadyExists && !processing,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = stringResource(MR.strings.extensionStoresScreen_addStoreDeeplink_bodyText))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                readOnly = true,
                supportingText = when {
                    storeAlreadyExists -> {
                        {
                            Text(text = stringResource(MR.strings.extensionStoresScreen_addStore_alreadyExists))
                        }
                    }
                    errorMessage != null -> {
                        {
                            Text(text = errorMessage)
                        }
                    }
                    else -> null
                },
                isError = errorMessage != null || storeAlreadyExists,
            )
        }
    }
}
