package eu.kanade.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.components.VmhBackCircle
import eu.kanade.presentation.components.VmhCircleAction
import eu.kanade.presentation.components.VmhSearchField
import eu.kanade.presentation.components.relativeDateText
import eu.kanade.presentation.history.components.HistoryItem
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.presentation.util.rememberIs24HourSystem
import eu.kanade.tachiyomi.ui.history.HistoryViewModel
import kotlinx.datetime.LocalDate
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.DeleteSweep
import mihon.icons.materialsymbols.rounded.Schedule
import mihon.icons.materialsymbols.rounded.Search
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.VmhLabel
import tachiyomi.presentation.core.components.VmhScreenHeader
import tachiyomi.presentation.core.components.VmhSnackbarHost
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyArt
import tachiyomi.presentation.core.screens.EmptyMascot
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.collectAsState

@Composable
fun HistoryScreen(
    state: HistoryViewModel.State,
    snackbarHostState: SnackbarHostState,
    onSearchQueryChange: (String?) -> Unit,
    onClickCover: (mangaId: Long) -> Unit,
    onClickResume: (mangaId: Long, chapterId: Long) -> Unit,
    onClickFavorite: (mangaId: Long) -> Unit,
    onDialogChange: (HistoryViewModel.Dialog?) -> Unit,
) {
    Scaffold(
        topBar = {
            VmhHistoryHeader(
                searchQuery = state.searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onClearHistory = { onDialogChange(HistoryViewModel.Dialog.DeleteAll) },
            )
        },
        snackbarHost = { VmhSnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        state.list.let {
            if (it == null) {
                LoadingScreen(Modifier.padding(contentPadding))
            } else if (it.isEmpty()) {
                val msg = if (!state.searchQuery.isNullOrEmpty()) {
                    MR.strings.no_results_found
                } else {
                    MR.strings.information_no_recent_manga
                }
                VmhHistoryEmpty(
                    text = stringResource(msg),
                    art = if (!state.searchQuery.isNullOrEmpty()) EmptyArt.SEARCH else EmptyArt.SLEEP,
                    modifier = Modifier.padding(contentPadding),
                )
            } else {
                HistoryScreenContent(
                    history = it,
                    contentPadding = contentPadding,
                    onClickCover = { history -> onClickCover(history.mangaId) },
                    onClickResume = { history -> onClickResume(history.mangaId, history.chapterId) },
                    onClickDelete = { item -> onDialogChange(HistoryViewModel.Dialog.Delete(item)) },
                    onClickFavorite = { history -> onClickFavorite(history.mangaId) },
                )
            }
        }
    }
}

@Composable
private fun VmhHistoryHeader(
    searchQuery: String?,
    onSearchQueryChange: (String?) -> Unit,
    onClearHistory: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var searching by remember { mutableStateOf(searchQuery != null) }

    LaunchedEffect(searchQuery) {
        if (searchQuery == null) {
            searching = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (searching) {
                VmhSearchField(
                    query = searchQuery.orEmpty(),
                    onQueryChange = onSearchQueryChange,
                    onSearch = {},
                    modifier = Modifier.weight(1f),
                    placeholderRes = MR.strings.vmh_history_search_hint,
                    autoFocus = true,
                )
            } else {
                VmhScreenHeader(
                    eyebrow = stringResource(MR.strings.app_name),
                    title = stringResource(MR.strings.history),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.width(8.dp))

            if (searching) {
                VmhBackCircle(onClick = { onSearchQueryChange(null) })
            } else {
                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.Search,
                    contentDescription = stringResource(MR.strings.action_search),
                    onClick = { searching = true },
                )
                Spacer(Modifier.width(8.dp))
                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.DeleteSweep,
                    contentDescription = stringResource(MR.strings.pref_clear_history),
                    onClick = onClearHistory,
                )
            }
        }

        HorizontalDivider(color = colors.outlineVariant)
    }
}

@Composable
private fun VmhHistoryEmpty(
    text: String,
    art: EmptyArt,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyMascot(art = art, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(18.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = colors.onSurface,
        )
    }
}

@Composable
private fun VmhHistoryDayHeader(text: String) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VmhLabel(
            text = text,
            color = colors.primary,
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.outlineVariant.copy(alpha = 0.7f)),
        )
    }
}

@Composable
private fun HistoryScreenContent(
    history: List<HistoryUiModel>,
    contentPadding: PaddingValues,
    onClickCover: (HistoryWithRelations) -> Unit,
    onClickResume: (HistoryWithRelations) -> Unit,
    onClickDelete: (HistoryWithRelations) -> Unit,
    onClickFavorite: (HistoryWithRelations) -> Unit,
) {
    val context = LocalContext.current
    val locale = LocalLocale.current
    val is24HourSystem = rememberIs24HourSystem()
    val timeFormat by remember { context.appGraph.uiPreferences.timeFormat }.collectAsState()
    val timeFormatter = remember(timeFormat, locale, is24HourSystem) {
        UiPreferences.timeFormat(timeFormat, locale.platformLocale, is24HourSystem)
    }

    FastScrollLazyColumn(
        contentPadding = contentPadding,
    ) {
        items(
            items = history,
            key = { "history-${it.hashCode()}" },
            contentType = {
                when (it) {
                    is HistoryUiModel.Header -> "header"
                    is HistoryUiModel.Item -> "item"
                }
            },
        ) { item ->
            when (item) {
                is HistoryUiModel.Header -> {
                    VmhHistoryDayHeader(
                        text = relativeDateText(item.date),
                    )
                }
                is HistoryUiModel.Item -> {
                    val value = item.item
                    HistoryItem(
                        modifier = Modifier.animateItem(),
                        history = value,
                        timeFormatter = timeFormatter,
                        onClickCover = { onClickCover(value) },
                        onClickResume = { onClickResume(value) },
                        onClickDelete = { onClickDelete(value) },
                        onClickFavorite = { onClickFavorite(value) },
                    )
                }
            }
        }
    }
}

sealed interface HistoryUiModel {
    data class Header(val date: LocalDate) : HistoryUiModel
    data class Item(val item: HistoryWithRelations) : HistoryUiModel
}

@PreviewLightDark
@Composable
internal fun HistoryScreenPreviews(
    @PreviewParameter(HistoryviewModelStateProvider::class)
    historyState: HistoryViewModel.State,
) {
    TachiyomiPreviewTheme {
        HistoryScreen(
            state = historyState,
            snackbarHostState = SnackbarHostState(),
            onSearchQueryChange = {},
            onClickCover = {},
            onClickResume = { _, _ -> run {} },
            onDialogChange = {},
            onClickFavorite = {},
        )
    }
}
