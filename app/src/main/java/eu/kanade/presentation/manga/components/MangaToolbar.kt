package eu.kanade.presentation.manga.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import eu.kanade.presentation.components.DownloadDropdownMenu
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.components.VmhCircleAction
import eu.kanade.presentation.manga.DownloadAction
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.FilterList
import mihon.icons.materialsymbols.rounded.FlipToBack
import mihon.icons.materialsymbols.rounded.MoreVert
import mihon.icons.materialsymbols.rounded.SelectAll
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun MangaToolbar(
    title: String,
    hasFilters: Boolean,
    navigateUp: () -> Unit,
    onClickFilter: () -> Unit,
    onClickShare: (() -> Unit)?,
    onClickDownload: ((DownloadAction) -> Unit)?,
    onClickEditCategory: (() -> Unit)?,
    onClickRefresh: () -> Unit,
    onClickMigrate: (() -> Unit)?,
    onClickEditNotes: () -> Unit,

    // For action mode
    actionModeCounter: Int,
    onCancelActionMode: () -> Unit,
    onSelectAll: () -> Unit,
    onInvertSelection: () -> Unit,

    titleAlphaProvider: () -> Float,
    backgroundAlphaProvider: () -> Float,
    modifier: Modifier = Modifier,
) {
    val isActionMode = actionModeCounter > 0

    VmhBarraMarca(
        modifier = modifier,
        titulo = if (isActionMode) actionModeCounter.toString() else title,
        mostrarTitulo = isActionMode || titleAlphaProvider() > 0.05f,
        onNavigateUp = navigateUp,
        onCancelActionMode = onCancelActionMode,
        enModoAccion = isActionMode,
        contenidoAcciones = {
            var downloadExpanded by remember { mutableStateOf(false) }
            var overflowExpanded by remember { mutableStateOf(false) }
            if (onClickDownload != null) {
                DownloadDropdownMenu(
                    expanded = downloadExpanded,
                    onDismissRequest = { downloadExpanded = false },
                    onDownloadClicked = onClickDownload,
                )
            }

            if (isActionMode) {
                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.SelectAll,
                    contentDescription = stringResource(MR.strings.action_select_all),
                    onClick = onSelectAll,
                )
                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.FlipToBack,
                    contentDescription = stringResource(MR.strings.action_select_inverse),
                    onClick = onInvertSelection,
                )
            } else {
                if (onClickDownload != null) {
                    VmhCircleAction(
                        icon = MaterialSymbols.Rounded.Download,
                        contentDescription = stringResource(MR.strings.manga_download),
                        onClick = { downloadExpanded = !downloadExpanded },
                    )
                }
                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.FilterList,
                    contentDescription = stringResource(MR.strings.action_filter),
                    onClick = onClickFilter,
                    activo = hasFilters,
                )
                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.MoreVert,
                    contentDescription = stringResource(MR.strings.action_menu_overflow_description),
                    onClick = { overflowExpanded = !overflowExpanded },
                )
                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = { overflowExpanded = false },
                ) {
                    val cerrar = { overflowExpanded = false }
                    DropdownMenuItem(
                        text = { Text(stringResource(MR.strings.action_webview_refresh)) },
                        onClick = {
                            cerrar()
                            onClickRefresh()
                        },
                    )
                    if (onClickEditCategory != null) {
                        DropdownMenuItem(
                            text = { Text(stringResource(MR.strings.action_edit_categories)) },
                            onClick = {
                                cerrar()
                                onClickEditCategory()
                            },
                        )
                    }
                    if (onClickMigrate != null) {
                        DropdownMenuItem(
                            text = { Text(stringResource(MR.strings.action_migrate)) },
                            onClick = {
                                cerrar()
                                onClickMigrate()
                            },
                        )
                    }
                    if (onClickShare != null) {
                        DropdownMenuItem(
                            text = { Text(stringResource(MR.strings.action_share)) },
                            onClick = {
                                cerrar()
                                onClickShare()
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(MR.strings.action_notes)) },
                        onClick = {
                            cerrar()
                            onClickEditNotes()
                        },
                    )
                }
            }
        },
    )
}
