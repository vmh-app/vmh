package eu.kanade.presentation.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.components.VmhBackCircle
import eu.kanade.presentation.components.VmhFilterPill
import eu.kanade.presentation.components.VmhSearchField
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.SearchViewModel
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.VmhLabel
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyArt
import tachiyomi.presentation.core.screens.EmptyScreen

@Composable
fun MigrateSearchScreen(
    state: SearchViewModel.State,
    fromSourceId: Long?,
    navigateUp: () -> Unit,
    onChangeSearchQuery: (String?) -> Unit,
    onSearch: (String) -> Unit,
    onToggleResults: () -> Unit,
    getManga: @Composable (Manga) -> State<Manga>,
    onClickSource: (Source) -> Unit,
    onClickItem: (Manga) -> Unit,
    onLongClickItem: (Manga) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        VmhMigrateSearchHeader(
            query = state.searchQuery.orEmpty(),
            originTitle = state.from?.title,
            progress = state.progress,
            total = state.total,
            hasResultsOnly = state.onlyShowHasResults,
            onBack = navigateUp,
            onQueryChange = onChangeSearchQuery,
            onSearch = onSearch,
            onToggleResults = onToggleResults,
        )

        if (state.filteredItems.isEmpty()) {
            EmptyScreen(
                stringRes = MR.strings.no_results_found,
                modifier = Modifier.weight(1f),
                art = EmptyArt.SEARCH,
            )
        } else {
            GlobalSearchContent(
                fromSourceId = fromSourceId,
                items = state.filteredItems,
                contentPadding = PaddingValues(bottom = 8.dp),
                getManga = getManga,
                onClickSource = onClickSource,
                onClickItem = onClickItem,
                onLongClickItem = onLongClickItem,
            )
        }
    }
}

@Composable
private fun VmhMigrateSearchHeader(
    query: String,
    originTitle: String?,
    progress: Int,
    total: Int,
    hasResultsOnly: Boolean,
    onBack: () -> Unit,
    onQueryChange: (String?) -> Unit,
    onSearch: (String) -> Unit,
    onToggleResults: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VmhBackCircle(onClick = onBack)

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VmhLabel(
                        text = stringResource(MR.strings.label_migration),
                        color = colors.primary,
                    )
                    if (total > 0) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(colors.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = stringResource(
                                    MR.strings.vmh_migration_sources_progress,
                                    progress,
                                    total,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(MR.strings.vmh_migration_matches),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (!originTitle.isNullOrBlank()) {
                    Text(
                        text = originTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        VmhSearchField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            placeholderRes = MR.strings.vmh_migration_search_hint,
        )

        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            VmhFilterPill(
                text = stringResource(MR.strings.has_results),
                selected = hasResultsOnly,
                onClick = onToggleResults,
            )
        }

        Spacer(Modifier.height(6.dp))
    }
}
