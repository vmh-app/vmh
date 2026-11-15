package eu.kanade.presentation.more.settings.screen.browse.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.VmhCircleAction
import mihon.domain.extension.model.ExtensionStore
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.Label
import mihon.icons.materialsymbols.rounded.ContentCopy
import mihon.icons.materialsymbols.rounded.Delete
import mihon.icons.materialsymbols.rounded.Public
import mihon.icons.simpleicons.Discord
import mihon.icons.simpleicons.SimpleIcons
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.VmhMetaPill
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ExtensionStoresContent(
    repos: List<ExtensionStore>,
    lazyListState: LazyListState,
    paddingValues: PaddingValues,
    onCopy: (ExtensionStore) -> Unit,
    onOpenWebsite: (ExtensionStore) -> Unit,
    onOpenDiscord: (ExtensionStore) -> Unit,
    onClickDelete: (ExtensionStore) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = lazyListState,
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
        modifier = modifier,
    ) {
        repos.forEach {
            item(key = it.indexUrl) {
                ExtensionStoresListItem(
                    modifier = Modifier.animateItem(),
                    store = it,
                    onOpenWebsite = { onOpenWebsite(it) },
                    onOpenDiscord = { onOpenDiscord(it) },
                    onCopy = { onCopy(it) },
                    onDelete = { onClickDelete(it) },
                )
            }
        }
    }
}

@Composable
private fun ExtensionStoresListItem(
    store: ExtensionStore,
    onOpenWebsite: () -> Unit,
    onOpenDiscord: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f)),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.AutoMirroredRounded.Label,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = store.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (store.badgeLabel.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            VmhMetaPill(text = store.badgeLabel, tint = colors.primary)
                        }
                    }
                    Text(
                        text = store.indexUrl,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            ) {
                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.Public,
                    contentDescription = stringResource(MR.strings.action_open_in_browser),
                    onClick = onOpenWebsite,
                    enabled = store.contact.website.isNotBlank(),
                )

                if (store.contact.discord != null) {
                    VmhCircleAction(
                        icon = SimpleIcons.Discord,
                        contentDescription = null,
                        onClick = onOpenDiscord,
                    )
                }

                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.ContentCopy,
                    contentDescription = stringResource(MR.strings.action_copy_to_clipboard),
                    onClick = onCopy,
                )

                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.Delete,
                    contentDescription = stringResource(MR.strings.action_delete),
                    onClick = onDelete,
                    tint = colors.error,
                )
            }
        }
    }
}
