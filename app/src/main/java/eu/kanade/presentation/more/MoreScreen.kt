package eu.kanade.presentation.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.ui.more.DownloadQueueState
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.Help
import mihon.icons.materialsymbols.automirroredrounded.Label
import mihon.icons.materialsymbols.rounded.CloudOff
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Eyeglasses2
import mihon.icons.materialsymbols.rounded.Info
import mihon.icons.materialsymbols.rounded.QueryStats
import mihon.icons.materialsymbols.rounded.Settings
import mihon.icons.materialsymbols.rounded.Storage
import tachiyomi.core.common.Constants
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.VmhScreenHeader
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickAbout: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Scaffold { contentPadding ->
        ScrollbarLazyColumn(contentPadding = contentPadding) {
            item {
                VmhScreenHeader(
                    eyebrow = stringResource(MR.strings.app_name),
                    title = stringResource(MR.strings.label_more),
                    modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StateCard(
                        title = stringResource(MR.strings.label_downloaded_only),
                        isActive = downloadedOnly,
                        icon = MaterialSymbols.Rounded.CloudOff,
                        onClick = { onDownloadedOnlyChange(!downloadedOnly) },
                        modifier = Modifier.weight(1f),
                    )
                    StateCard(
                        title = stringResource(MR.strings.pref_incognito_mode),
                        isActive = incognitoMode,
                        icon = MaterialSymbols.Rounded.Eyeglasses2,
                        onClick = { onIncognitoModeChange(!incognitoMode) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }

            item {
                val downloadQueueState = downloadQueueStateProvider()
                val queueStateText = when (downloadQueueState) {
                    DownloadQueueState.Stopped -> null
                    is DownloadQueueState.Paused -> {
                        val pending = downloadQueueState.pending
                        if (pending == 0) {
                            stringResource(MR.strings.paused)
                        } else {
                            "${stringResource(MR.strings.paused)} • ${
                                pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                            }"
                        }
                    }
                    is DownloadQueueState.Downloading -> {
                        val pending = downloadQueueState.pending
                        pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                    }
                }

                val destItems = listOf(
                    DestItem(
                        MR.strings.label_download_queue,
                        queueStateText,
                        MaterialSymbols.Rounded.Download,
                        onClickDownloadQueue,
                    ),
                    DestItem(MR.strings.categories, null, MaterialSymbols.AutoMirroredRounded.Label, onClickCategories),
                    DestItem(MR.strings.label_stats, null, MaterialSymbols.Rounded.QueryStats, onClickStats),
                    DestItem(
                        MR.strings.label_data_storage,
                        null,
                        MaterialSymbols.Rounded.Storage,
                        onClickDataAndStorage,
                    ),
                    DestItem(MR.strings.label_settings, null, MaterialSymbols.Rounded.Settings, onClickSettings),
                )

                BoxWithConstraints(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val columns = if (maxWidth < 300.dp) 1 else 2
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        destItems.chunked(columns).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowItems.forEach { item ->
                                    DestinationCard(item, modifier = Modifier.weight(1f))
                                }
                                if (columns == 2 && rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onClickAbout) {
                        Icon(MaterialSymbols.Rounded.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(MR.strings.pref_category_about))
                    }
                }
            }
        }
    }
}

@Composable
private fun StateCard(
    title: String,
    isActive: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isActive) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = 0.4f,
        )
    }
    val contentColor = if (isActive) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
    val stateTextRes = if (isActive) MR.strings.on else MR.strings.off

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        modifier = modifier.height(130.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(icon, tint = contentColor, contentDescription = null, modifier = Modifier.size(28.dp))
                Text(
                    text = stringResource(stateTextRes).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.8f),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}

private data class DestItem(
    val titleRes: StringResource,
    val stateText: String?,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
private fun DestinationCard(item: DestItem, modifier: Modifier = Modifier) {
    Surface(
        onClick = item.onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = modifier.height(116.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            if (item.stateText != null) {
                Text(
                    text = item.stateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Spacer(Modifier.height(16.dp))
            }

            Column {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(item.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
