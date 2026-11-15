package eu.kanade.tachiyomi.ui.browse.migration.manga

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import eu.kanade.presentation.components.VmhContinueBar
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.flow.collectLatest
import mihon.feature.migration.config.MigrationConfigScreen
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ArrowBack
import mihon.icons.materialsymbols.rounded.Check
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.VmhLabel
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen

data class MigrateMangaScreen(
    private val sourceId: Long,
) : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val viewModel =
            assistedMetroViewModel<MigrateMangaViewModel, MigrateMangaViewModel.Factory> { create(sourceId = sourceId) }

        val state by viewModel.state.collectAsStateWithLifecycle()

        if (state.isLoading) {
            LoadingScreen()
            return
        }

        BackHandler(enabled = state.selectionMode) {
            viewModel.clearSelection()
        }

        val lazyListState = rememberLazyListState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            VmhReviewHeader(
                sourceName = state.source?.name.orEmpty(),
                count = state.titles.size,
                onBack = {
                    if (state.selectionMode) {
                        viewModel.clearSelection()
                    } else {
                        navigator.pop()
                    }
                },
            )

            if (state.isEmpty) {
                EmptyScreen(
                    stringRes = MR.strings.empty_screen,
                    modifier = Modifier.weight(1f),
                )
            } else {
                FastScrollLazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    item(key = "section-header") {
                        VmhReviewSectionHeader(count = state.titles.size)
                    }
                    items(state.titles, key = { it.id }) { manga ->
                        VmhMangaReviewCard(
                            manga = manga,
                            selected = manga.id in state.selection,
                            onClickItem = { viewModel.toggleSelection(manga) },
                            onClickCover = { navigator.push(MangaScreen(manga.id)) },
                        )
                    }
                }
            }

            VmhContinueBar(
                enabled = state.selectionMode,
                onClick = {
                    val selection = state.selection
                    viewModel.clearSelection()
                    navigator.push(MigrationConfigScreen(selection))
                },
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
            )
        }

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    MigrationMangaEvent.FailedFetchingFavorites -> {
                        context.toast(MR.strings.internal_error)
                    }
                }
            }
        }
    }
}

@Composable
private fun VmhReviewHeader(
    sourceName: String,
    count: Int,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 12.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onBack,
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = colors.surface,
            contentColor = colors.onSurfaceVariant,
            border = BorderStroke(1.dp, colors.outlineVariant),
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = MaterialSymbols.AutoMirroredRounded.ArrowBack,
                    contentDescription = stringResource(MR.strings.action_bar_up_description),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            VmhLabel(
                text = stringResource(MR.strings.label_migration),
                color = colors.primary,
            )
            Text(
                text = stringResource(MR.strings.vmh_migration_step_review),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (sourceName.isNotBlank()) {
                Text(
                    text = sourceName +
                        " · " +
                        pluralStringResource(MR.plurals.vmh_migration_titles_in_library, count, count),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun VmhReviewSectionHeader(count: Int) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VmhLabel(
            text = stringResource(MR.strings.vmh_migration_titles),
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(colors.primary.copy(alpha = 0.12f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
            )
        }
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
private fun VmhMangaReviewCard(
    manga: Manga,
    selected: Boolean,
    onClickItem: () -> Unit,
    onClickCover: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClickItem),
        shape = RoundedCornerShape(17.dp),
        color = colors.surface,
        border = BorderStroke(
            1.dp,
            if (selected) colors.primary.copy(alpha = 0.55f) else colors.outlineVariant,
        ),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(
                    start = if (selected) 0.dp else 12.dp,
                    top = 10.dp,
                    bottom = 10.dp,
                    end = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(colors.primary),
                )
                Spacer(Modifier.width(9.dp))
            }

            MangaCover.Square(
                data = manga,
                modifier = Modifier.size(56.dp),
                onClick = onClickCover,
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = manga.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                val credit = listOfNotNull(manga.author, manga.artist)
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(" · ")
                if (credit.isNotEmpty()) {
                    Text(
                        text = credit,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            VmhSelectionCircle(selected = selected)
        }
    }
}

@Composable
private fun VmhSelectionCircle(selected: Boolean) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (selected) colors.primary else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (selected) colors.primary else colors.outlineVariant,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = colors.onPrimary,
            )
        }
    }
}
