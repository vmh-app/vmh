package eu.kanade.presentation.library

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.components.TabbedDialog
import eu.kanade.presentation.components.TabbedDialogPaddings
import eu.kanade.tachiyomi.ui.library.LibrarySettingsViewModel
import eu.kanade.tachiyomi.util.system.isReleaseBuildType
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.ArrowDownward
import mihon.icons.materialsymbols.rounded.ArrowUpward
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.Close
import mihon.icons.materialsymbols.rounded.Refresh
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.TriState
import tachiyomi.core.common.preference.toggle
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.model.sort
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.SliderItem
import tachiyomi.presentation.core.components.VmhSectionTitle
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

private val FilaHorizontal = TabbedDialogPaddings.Horizontal
private val CajaCheck = 22.dp
private val CajaCheckShape = RoundedCornerShape(7.dp)

@Composable
fun LibrarySettingsDialog(
    onDismissRequest: () -> Unit,
    viewModel: LibrarySettingsViewModel,
    category: Category?,
) {
    TabbedDialog(
        onDismissRequest = onDismissRequest,
        tabTitles = listOf(
            stringResource(MR.strings.action_filter),
            stringResource(MR.strings.action_sort),
            stringResource(MR.strings.action_display),
        ),
    ) { page ->
        Column(
            modifier = Modifier
                .padding(vertical = TabbedDialogPaddings.Vertical)
                .verticalScroll(rememberScrollState()),
        ) {
            when (page) {
                0 -> FilterPage(
                    viewModel = viewModel,
                )
                1 -> SortPage(
                    category = category,
                    viewModel = viewModel,
                )
                2 -> DisplayPage(
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.FilterPage(
    viewModel: LibrarySettingsViewModel,
) {
    val filterDownloaded by viewModel.libraryPreferences.filterDownloaded.collectAsState()
    val downloadedOnly by viewModel.preferences.downloadedOnly.collectAsState()
    val autoUpdateMangaRestrictions by viewModel.libraryPreferences.autoUpdateMangaRestrictions.collectAsState()

    VmhRowLabel(MR.strings.pref_category_library)
    VmhTriRow(
        label = stringResource(MR.strings.label_downloaded),
        state = if (downloadedOnly) TriState.ENABLED_IS else filterDownloaded,
        enabled = !downloadedOnly,
        onClick = { viewModel.toggleFilter(LibraryPreferences::filterDownloaded) },
    )
    val filterUnread by viewModel.libraryPreferences.filterUnread.collectAsState()
    VmhTriRow(
        label = stringResource(MR.strings.action_filter_unread),
        state = filterUnread,
        onClick = { viewModel.toggleFilter(LibraryPreferences::filterUnread) },
    )
    val filterStarted by viewModel.libraryPreferences.filterStarted.collectAsState()
    VmhTriRow(
        label = stringResource(MR.strings.label_started),
        state = filterStarted,
        onClick = { viewModel.toggleFilter(LibraryPreferences::filterStarted) },
    )
    val filterBookmarked by viewModel.libraryPreferences.filterBookmarked.collectAsState()
    VmhTriRow(
        label = stringResource(MR.strings.action_filter_bookmarked),
        state = filterBookmarked,
        onClick = { viewModel.toggleFilter(LibraryPreferences::filterBookmarked) },
    )
    val filterCompleted by viewModel.libraryPreferences.filterCompleted.collectAsState()
    VmhTriRow(
        label = stringResource(MR.strings.completed),
        state = filterCompleted,
        onClick = { viewModel.toggleFilter(LibraryPreferences::filterCompleted) },
    )
    // TODO: re-enable when custom intervals are ready for stable
    if ((!isReleaseBuildType) && LibraryPreferences.MANGA_OUTSIDE_RELEASE_PERIOD in autoUpdateMangaRestrictions) {
        val filterIntervalCustom by viewModel.libraryPreferences.filterIntervalCustom.collectAsState()
        VmhTriRow(
            label = stringResource(MR.strings.action_filter_interval_custom),
            state = filterIntervalCustom,
            onClick = { viewModel.toggleFilter(LibraryPreferences::filterIntervalCustom) },
        )
    }

    val trackers by viewModel.trackersFlow.collectAsState()
    when (trackers.size) {
        0 -> {
            // No trackers
        }
        1 -> {
            val service = trackers[0]
            val filterTracker by viewModel.libraryPreferences.filterTracking(service.id.toInt()).collectAsState()
            VmhTriRow(
                label = stringResource(MR.strings.action_filter_tracked),
                state = filterTracker,
                onClick = { viewModel.toggleTracker(service.id.toInt()) },
            )
        }
        else -> {
            VmhRowLabel(MR.strings.action_filter_tracked)
            trackers.map { service ->
                val filterTracker by viewModel.libraryPreferences.filterTracking(service.id.toInt()).collectAsState()
                VmhTriRow(
                    label = service.name,
                    state = filterTracker,
                    onClick = { viewModel.toggleTracker(service.id.toInt()) },
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.SortPage(
    category: Category?,
    viewModel: LibrarySettingsViewModel,
) {
    val trackers by viewModel.trackersFlow.collectAsState()
    val sortingMode = category.sort.type
    val sortDescending = !category.sort.isAscending

    val options = remember(trackers.isEmpty()) {
        val trackerMeanPair = if (trackers.isNotEmpty()) {
            MR.strings.action_sort_tracker_score to LibrarySort.Type.TrackerMean
        } else {
            null
        }
        listOfNotNull(
            MR.strings.action_sort_alpha to LibrarySort.Type.Alphabetical,
            MR.strings.action_sort_total to LibrarySort.Type.TotalChapters,
            MR.strings.action_sort_last_read to LibrarySort.Type.LastRead,
            MR.strings.action_sort_last_manga_update to LibrarySort.Type.LastUpdate,
            MR.strings.action_sort_unread_count to LibrarySort.Type.UnreadCount,
            MR.strings.action_sort_latest_chapter to LibrarySort.Type.LatestChapter,
            MR.strings.action_sort_chapter_fetch_date to LibrarySort.Type.ChapterFetchDate,
            MR.strings.action_sort_date_added to LibrarySort.Type.DateAdded,
            trackerMeanPair,
            MR.strings.action_sort_random to LibrarySort.Type.Random,
        )
    }

    VmhRowLabel(MR.strings.action_sort)
    options.map { (titleRes, mode) ->
        VmhSortRow(
            label = stringResource(titleRes),
            selected = sortingMode == mode,
            descending = sortDescending,
            icon = if (mode == LibrarySort.Type.Random) MaterialSymbols.Rounded.Refresh else null,
            onClick = {
                if (mode == LibrarySort.Type.Random) {
                    viewModel.setSort(category, mode, LibrarySort.Direction.Ascending)
                    return@VmhSortRow
                }
                val isTogglingDirection = sortingMode == mode
                val direction = when {
                    isTogglingDirection -> if (sortDescending) {
                        LibrarySort.Direction.Ascending
                    } else {
                        LibrarySort.Direction.Descending
                    }
                    else -> if (sortDescending) {
                        LibrarySort.Direction.Descending
                    } else {
                        LibrarySort.Direction.Ascending
                    }
                }
                viewModel.setSort(category, mode, direction)
            },
        )
    }
}

private val displayModes = listOf(
    MR.strings.action_display_grid to LibraryDisplayMode.CompactGrid,
    MR.strings.action_display_comfortable_grid to LibraryDisplayMode.ComfortableGrid,
    MR.strings.action_display_cover_only_grid to LibraryDisplayMode.CoverOnlyGrid,
    MR.strings.action_display_list to LibraryDisplayMode.List,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.DisplayPage(
    viewModel: LibrarySettingsViewModel,
) {
    val displayMode by viewModel.libraryPreferences.displayMode.collectAsState()

    VmhRowLabel(MR.strings.action_display_mode)
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FilaHorizontal),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        displayModes.map { (titleRes, mode) ->
            VmhModePill(
                text = stringResource(titleRes),
                selected = displayMode == mode,
                onClick = { viewModel.setDisplayMode(mode) },
            )
        }
    }

    if (displayMode != LibraryDisplayMode.List) {
        val configuration = LocalConfiguration.current
        val columnPreference = remember {
            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                viewModel.libraryPreferences.landscapeColumns
            } else {
                viewModel.libraryPreferences.portraitColumns
            }
        }

        val columns by columnPreference.collectAsState()
        SliderItem(
            value = columns,
            valueRange = 0..10,
            label = stringResource(MR.strings.pref_library_columns),
            valueString = if (columns > 0) {
                columns.toString()
            } else {
                stringResource(MR.strings.label_auto)
            },
            onChange = columnPreference::set,
            pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
    }

    VmhRowLabel(MR.strings.overlay_header)
    VmhCheckRow(
        label = stringResource(MR.strings.action_display_download_badge),
        pref = viewModel.libraryPreferences.downloadBadge,
    )
    VmhCheckRow(
        label = stringResource(MR.strings.action_display_unread_badge),
        pref = viewModel.libraryPreferences.unreadBadge,
    )
    VmhCheckRow(
        label = stringResource(MR.strings.action_display_local_badge),
        pref = viewModel.libraryPreferences.localBadge,
    )
    VmhCheckRow(
        label = stringResource(MR.strings.action_display_language_badge),
        pref = viewModel.libraryPreferences.languageBadge,
    )
    VmhCheckRow(
        label = stringResource(MR.strings.action_display_show_continue_reading_button),
        pref = viewModel.libraryPreferences.showContinueReadingButton,
    )

    VmhRowLabel(MR.strings.tabs_header)
    VmhCheckRow(
        label = stringResource(MR.strings.action_display_show_tabs),
        pref = viewModel.libraryPreferences.categoryTabs,
    )
    VmhCheckRow(
        label = stringResource(MR.strings.action_display_show_number_of_items),
        pref = viewModel.libraryPreferences.categoryNumberOfItems,
    )
}

/** Rótulo de grupo dentro de la hoja: versalitas con filete, como el resto de VMH. */
@Composable
private fun VmhRowLabel(stringRes: StringResource) {
    VmhSectionTitle(
        text = stringResource(stringRes),
        modifier = Modifier.padding(start = FilaHorizontal, end = FilaHorizontal, top = 16.dp, bottom = 6.dp),
    )
}

/** Casilla de 22 dp con esquinas suaves: el check del sistema de VMH. */
@Composable
private fun VmhCheckBox(
    activo: Boolean,
    parcial: Boolean = false,
    habilitado: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(CajaCheck)
            .clip(CajaCheckShape)
            .background(if (activo) colors.primary else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (activo) colors.primary else colors.outlineVariant,
                shape = CajaCheckShape,
            )
            .alpha(if (habilitado) 1f else 0.38f),
        contentAlignment = Alignment.Center,
    ) {
        if (activo) {
            Icon(
                imageVector = if (parcial) MaterialSymbols.Rounded.Close else MaterialSymbols.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = colors.onPrimary,
            )
        }
    }
}

@Composable
private fun VmhTriRow(
    label: String,
    state: TriState,
    enabled: Boolean = true,
    onClick: ((TriState) -> Unit)?,
) {
    val colors = MaterialTheme.colorScheme
    val alfa = if (enabled && onClick != null) 1f else 0.45f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled && onClick != null) {
                when (state) {
                    TriState.DISABLED -> onClick?.invoke(TriState.ENABLED_IS)
                    TriState.ENABLED_IS -> onClick?.invoke(TriState.ENABLED_NOT)
                    TriState.ENABLED_NOT -> onClick?.invoke(TriState.DISABLED)
                }
            }
            .padding(horizontal = FilaHorizontal, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VmhCheckBox(
            activo = state != TriState.DISABLED,
            parcial = state == TriState.ENABLED_NOT,
            habilitado = enabled,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = alfa),
        )
    }
}

@Composable
private fun VmhCheckRow(
    label: String,
    pref: Preference<Boolean>,
) {
    val checked by pref.collectAsState()
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { pref.toggle() })
            .padding(horizontal = FilaHorizontal, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VmhCheckBox(activo = checked)
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
        )
    }
}

@Composable
private fun VmhSortRow(
    label: String,
    selected: Boolean,
    descending: Boolean,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = FilaHorizontal, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.primary else colors.onSurface,
        )
        when {
            selected && icon == null -> Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = if (descending) {
                        MaterialSymbols.Rounded.ArrowDownward
                    } else {
                        MaterialSymbols.Rounded.ArrowUpward
                    },
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = colors.onPrimaryContainer,
                )
            }
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (selected) colors.primary else colors.onSurfaceVariant,
            )
            else -> Spacer(Modifier.width(18.dp))
        }
    }
}

@Composable
private fun VmhModePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) colors.primary else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) colors.primary else colors.outlineVariant,
                shape = CircleShape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.onPrimary else colors.onSurface,
        )
    }
}
