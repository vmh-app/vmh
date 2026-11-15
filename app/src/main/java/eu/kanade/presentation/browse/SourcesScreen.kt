package eu.kanade.presentation.browse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.browse.components.SourceIcon
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.source.SourcesViewModel
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceViewModel.Listing
import eu.kanade.tachiyomi.util.system.LocaleHelper
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ArrowForward
import tachiyomi.domain.source.model.Pin
import tachiyomi.domain.source.model.Source
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyArt
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.source.local.isLocal

@Composable
fun SourcesScreen(
    state: SourcesViewModel.State,
    contentPadding: PaddingValues,
    onClickItem: (Source, Listing) -> Unit,
    onClickPin: (Source) -> Unit,
    onLongClickItem: (Source) -> Unit,
) {
    when {
        state.isLoading -> LoadingScreen(Modifier.padding(contentPadding))
        state.isEmpty -> EmptyScreen(
            stringRes = MR.strings.source_empty_screen,
            modifier = Modifier.padding(contentPadding),
            art = EmptyArt.SEARCH,
        )
        else -> {
            val sections = sectionsOf(state.items)

            ScrollbarLazyColumn(
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = contentPadding.calculateTopPadding() + 2.dp,
                    end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                ),
            ) {
                sections.forEach { section ->
                    item(key = "header-${section.language}") {
                        SourceSectionHeader(section)
                    }

                    if (section.language == SourcesViewModel.LAST_USED_KEY) {
                        item(key = "recent-${section.language}") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(
                                    items = section.sources,
                                    key = { "recent-${it.key()}" },
                                ) { source ->
                                    RecentSourceCard(
                                        source = source,
                                        onClick = { onClickItem(source, Listing.Popular) },
                                        onLongClick = { onLongClickItem(source) },
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = section.sources,
                            key = { "source-${it.key()}" },
                        ) { source ->
                            SourceCard(
                                source = source,
                                onClick = { onClickItem(source, Listing.Popular) },
                                onClickLatest = { onClickItem(source, Listing.Latest) },
                                onClickPin = { onClickPin(source) },
                                onLongClick = { onLongClickItem(source) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class SourceSection(
    val language: String,
    val sources: List<Source>,
)

private fun sectionsOf(items: List<SourceUiModel>): List<SourceSection> {
    val sections = mutableListOf<SourceSection>()
    var language: String? = null
    var sources = mutableListOf<Source>()

    items.forEach { model ->
        when (model) {
            is SourceUiModel.Header -> {
                language?.let { sections += SourceSection(it, sources) }
                language = model.language
                sources = mutableListOf()
            }
            is SourceUiModel.Item -> sources += model.source
        }
    }
    language?.let { sections += SourceSection(it, sources) }
    return sections
}

@Composable
private fun SourceSectionHeader(section: SourceSection) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val title = when (section.language) {
        SourcesViewModel.LAST_USED_KEY -> stringResource(MR.strings.vmh_recent_sources)
        SourcesViewModel.PINNED_KEY -> stringResource(MR.strings.pinned_sources)
        else -> LocaleHelper.getSourceDisplayName(section.language, context)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = colors.onSurfaceVariant,
        )
        if (section.sources.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .height(17.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.primary.copy(alpha = 0.10f))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = section.sources.size.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = colors.primary,
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.outlineVariant),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentSourceCard(
    source: Source,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .width(140.dp)
            .height(58.dp),
        shape = RoundedCornerShape(15.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                SourceIcon(source = source, modifier = Modifier.fillMaxSize())
            }
            Text(
                text = source.visualName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SourceCard(
    source: Source,
    onClick: () -> Unit,
    onClickLatest: () -> Unit,
    onClickPin: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val isPinned = Pin.Pinned in source.pin

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        ) {
            if (isPinned) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(colors.primary),
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 62.dp)
                    .padding(start = 12.dp, end = 10.dp, top = 9.dp, bottom = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    SourceIcon(source = source, modifier = Modifier.fillMaxSize())
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = source.visualName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = LocaleHelper.getSourceDisplayName(source.lang, LocalContext.current),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (source.supportsLatest) {
                    LatestPill(onClick = onClickLatest)
                }

                SourcePinButton(isPinned = isPinned, onClick = onClickPin)
            }
        }
    }
}

@Composable
private fun LatestPill(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .height(28.dp)
                .padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = stringResource(MR.strings.latest),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.primary,
                maxLines = 1,
            )
            Icon(
                imageVector = MaterialSymbols.AutoMirroredRounded.ArrowForward,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun SourcePinButton(
    isPinned: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(if (isPinned) colors.primary.copy(alpha = 0.10f) else colors.surface),
    ) {
        Icon(
            painter = painterResource(
                if (isPinned) R.drawable.ic_vmh_bookmark_filled else R.drawable.ic_vmh_bookmark,
            ),
            contentDescription = stringResource(
                if (isPinned) MR.strings.action_unpin else MR.strings.action_pin,
            ),
            tint = if (isPinned) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.size(17.dp),
        )
    }
}

@Composable
fun SourceOptionsDialog(
    source: Source,
    onClickPin: () -> Unit,
    onClickDisable: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = source.visualName)
        },
        text = {
            Column {
                val textId = if (Pin.Pinned in source.pin) MR.strings.action_unpin else MR.strings.action_pin
                Text(
                    text = stringResource(textId),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClickPin)
                        .padding(vertical = 16.dp),
                )
                if (!source.isLocal()) {
                    Text(
                        text = stringResource(MR.strings.action_disable),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onClickDisable)
                            .padding(vertical = 16.dp),
                    )
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {},
    )
}

sealed interface SourceUiModel {
    data class Item(val source: Source) : SourceUiModel
    data class Header(val language: String) : SourceUiModel
}
