package eu.kanade.presentation.more.settings.screen.browse.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.more.settings.screen.browse.ExtensionStoreScreenState
import mihon.domain.extension.model.ExtensionStore
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Add
import mihon.icons.materialsymbols.rounded.Refresh
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.components.material.topSmallPaddingValues
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyArt
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.EmptyScreenAction
import tachiyomi.presentation.core.util.plus

@Composable
fun ExtensionStoresScreen(
    state: ExtensionStoreScreenState.Success,
    onClickCreate: () -> Unit,
    onCopy: (ExtensionStore) -> Unit,
    onOpenWebsite: (ExtensionStore) -> Unit,
    onOpenDiscord: (ExtensionStore) -> Unit,
    onClickDelete: (ExtensionStore) -> Unit,
    onClickRefresh: () -> Unit,
    navigateUp: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                navigateUp = navigateUp,
                title = stringResource(MR.strings.extensionStores),
                scrollBehavior = scrollBehavior,
                actions = {
                    AppBarActions(
                        listOf(
                            AppBar.Action(
                                title = stringResource(MR.strings.action_webview_refresh),
                                icon = MaterialSymbols.Rounded.Refresh,
                                onClick = onClickRefresh,
                            ),
                        ),
                    )
                },
            )
        },
        floatingActionButton = {
            AddStorePill(onClick = onClickCreate)
        },
    ) { paddingValues ->
        if (state.isEmpty) {
            EmptyScreen(
                MR.strings.extensionStoresScreen_emptyLabel,
                modifier = Modifier.padding(paddingValues),
                art = EmptyArt.SEARCH,
                actions = listOf(
                    EmptyScreenAction(
                        stringRes = MR.strings.action_add,
                        icon = MaterialSymbols.Rounded.Add,
                        onClick = onClickCreate,
                    ),
                ),
            )
            return@Scaffold
        }

        ExtensionStoresContent(
            repos = state.stores,
            lazyListState = lazyListState,
            paddingValues = paddingValues + topSmallPaddingValues +
                PaddingValues(horizontal = MaterialTheme.padding.medium),
            onCopy = onCopy,
            onOpenWebsite = onOpenWebsite,
            onOpenDiscord = onOpenDiscord,
            onClickDelete = onClickDelete,
        )
    }
}

/**
 * Pastilla "Añadir" de la marca. Va en el hueco del botón flotante en vez del FAB de Mihon, que
 * era un cuadrado redondeado con un signo suelto.
 */
@Composable
private fun AddStorePill(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = colors.primary,
        contentColor = colors.onPrimary,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(MR.strings.action_add),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
