package eu.kanade.presentation.browse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.components.RadioMenuItem
import eu.kanade.presentation.components.VmhBackCircle
import eu.kanade.presentation.components.VmhCircleAction
import eu.kanade.presentation.components.VmhFilterPill
import eu.kanade.presentation.components.VmhSearchField
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.util.system.LocaleHelper
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredroundedfilled.ViewList
import mihon.icons.materialsymbols.rounded.FilterList
import mihon.icons.materialsymbols.rounded.MoreVert
import mihon.icons.materialsymbols.rounded.Schedule
import mihon.icons.materialsymbols.rounded.Search
import mihon.icons.materialsymbols.roundedfilled.Favorite
import mihon.icons.materialsymbols.roundedfilled.ViewCompact
import mihon.icons.materialsymbols.roundedfilled.ViewModule
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.VmhLabel
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.source.local.LocalSource

@Composable
fun BrowseSourceToolbar(
    searchQuery: String?,
    onSearchQueryChange: (String?) -> Unit,
    source: Source?,
    displayMode: LibraryDisplayMode,
    onDisplayModeChange: (LibraryDisplayMode) -> Unit,
    navigateUp: () -> Unit,
    onWebViewClick: () -> Unit,
    onHelpClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearch: (String) -> Unit,
    popularSelected: Boolean,
    latestSelected: Boolean,
    filterSelected: Boolean,
    showFilter: Boolean,
    onPopularClick: () -> Unit,
    onLatestClick: () -> Unit,
    onFilterClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val title = source?.name
    val language = source?.lang
        ?.takeIf { it.isNotBlank() }
        ?.let { LocaleHelper.getLocalizedDisplayName(it) }
    val isLocalSource = source is LocalSource
    val isConfigurableSource = source is ConfigurableSource

    var searching by remember { mutableStateOf(searchQuery != null) }
    var selectingDisplayMode by remember { mutableStateOf(false) }
    var overflowOpen by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (searchQuery == null) {
            searching = false
        }
    }

    val displayModeIcon = when (displayMode) {
        LibraryDisplayMode.List -> MaterialSymbols.AutoMirroredRoundedFilled.ViewList
        LibraryDisplayMode.ComfortableGrid -> MaterialSymbols.RoundedFilled.ViewModule
        LibraryDisplayMode.CompactGrid,
        LibraryDisplayMode.CoverOnlyGrid,
        -> MaterialSymbols.RoundedFilled.ViewCompact
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VmhBackCircle(onClick = navigateUp)

            Spacer(Modifier.width(12.dp))

            if (searching) {
                VmhSearchField(
                    query = searchQuery.orEmpty(),
                    onQueryChange = onSearchQueryChange,
                    onSearch = onSearch,
                    modifier = Modifier.weight(1f),
                    placeholderRes = MR.strings.vmh_source_search_hint,
                    autoFocus = true,
                )
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    if (language != null) {
                        VmhLabel(
                            text = language,
                            color = colors.primary,
                        )
                    }
                    Text(
                        text = title.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.width(12.dp))

                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.Search,
                    contentDescription = stringResource(MR.strings.action_search),
                    onClick = {
                        searching = true
                        onSearchQueryChange("")
                    },
                )

                Spacer(Modifier.width(8.dp))

                Box {
                    VmhCircleAction(
                        icon = displayModeIcon,
                        contentDescription = stringResource(MR.strings.action_display_mode),
                        onClick = { selectingDisplayMode = true },
                    )
                    DropdownMenu(
                        expanded = selectingDisplayMode,
                        onDismissRequest = { selectingDisplayMode = false },
                    ) {
                        RadioMenuItem(
                            text = { Text(stringResource(MR.strings.action_display_comfortable_grid)) },
                            isChecked = displayMode == LibraryDisplayMode.ComfortableGrid,
                        ) {
                            selectingDisplayMode = false
                            onDisplayModeChange(LibraryDisplayMode.ComfortableGrid)
                        }
                        RadioMenuItem(
                            text = { Text(stringResource(MR.strings.action_display_grid)) },
                            isChecked = displayMode == LibraryDisplayMode.CompactGrid,
                        ) {
                            selectingDisplayMode = false
                            onDisplayModeChange(LibraryDisplayMode.CompactGrid)
                        }
                        RadioMenuItem(
                            text = { Text(stringResource(MR.strings.action_display_list)) },
                            isChecked = displayMode == LibraryDisplayMode.List,
                        ) {
                            selectingDisplayMode = false
                            onDisplayModeChange(LibraryDisplayMode.List)
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                Box {
                    VmhCircleAction(
                        icon = MaterialSymbols.Rounded.MoreVert,
                        contentDescription = stringResource(MR.strings.vmh_more_actions),
                        onClick = { overflowOpen = true },
                    )
                    DropdownMenu(
                        expanded = overflowOpen,
                        onDismissRequest = { overflowOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(
                                        if (isLocalSource) {
                                            MR.strings.label_help
                                        } else {
                                            MR.strings.action_open_in_web_view
                                        },
                                    ),
                                )
                            },
                            onClick = {
                                overflowOpen = false
                                if (isLocalSource) onHelpClick() else onWebViewClick()
                            },
                        )
                        if (isConfigurableSource) {
                            DropdownMenuItem(
                                text = { Text(stringResource(MR.strings.action_settings)) },
                                onClick = {
                                    overflowOpen = false
                                    onSettingsClick()
                                },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VmhFilterPill(
                text = stringResource(MR.strings.popular),
                selected = popularSelected,
                onClick = onPopularClick,
                icon = MaterialSymbols.RoundedFilled.Favorite,
            )
            if (source?.supportsLatest == true) {
                VmhFilterPill(
                    text = stringResource(MR.strings.latest),
                    selected = latestSelected,
                    onClick = onLatestClick,
                    icon = MaterialSymbols.Rounded.Schedule,
                )
            }
            if (showFilter) {
                VmhFilterPill(
                    text = stringResource(MR.strings.action_filter),
                    selected = filterSelected,
                    onClick = onFilterClick,
                    icon = MaterialSymbols.Rounded.FilterList,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        HorizontalDivider(color = colors.outlineVariant)
    }
}
