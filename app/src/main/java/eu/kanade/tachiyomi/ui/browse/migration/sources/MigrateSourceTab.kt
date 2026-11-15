package eu.kanade.tachiyomi.ui.browse.migration.sources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.domain.source.interactor.SetMigrateSorting
import eu.kanade.presentation.browse.MigrateSourceScreen
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.TabContent
import eu.kanade.tachiyomi.ui.browse.migration.manga.MigrateMangaScreen
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.ArrowDownward
import mihon.icons.materialsymbols.rounded.ArrowUpward
import mihon.icons.materialsymbols.rounded.Numbers
import mihon.icons.materialsymbols.rounded.SortByAlpha
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun Screen.migrateSourceTab(): TabContent {
    val navigator = LocalNavigator.currentOrThrow
    val viewModel = metroViewModel<MigrateSourceViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    return TabContent(
        titleRes = MR.strings.label_migration,
        subtitleRes = MR.strings.vmh_migration_subtitle,
        actions = listOf(
            AppBar.Action(
                title = stringResource(
                    when (state.sortingMode) {
                        SetMigrateSorting.Mode.ALPHABETICAL -> MR.strings.action_sort_alpha
                        SetMigrateSorting.Mode.TOTAL -> MR.strings.action_sort_count
                    },
                ),
                icon = when (state.sortingMode) {
                    SetMigrateSorting.Mode.ALPHABETICAL -> MaterialSymbols.Rounded.SortByAlpha
                    SetMigrateSorting.Mode.TOTAL -> MaterialSymbols.Rounded.Numbers
                },
                onClick = viewModel::toggleSortingMode,
            ),
            AppBar.Action(
                title = stringResource(
                    when (state.sortingDirection) {
                        SetMigrateSorting.Direction.ASCENDING -> MR.strings.action_asc
                        SetMigrateSorting.Direction.DESCENDING -> MR.strings.action_desc
                    },
                ),
                icon = when (state.sortingDirection) {
                    SetMigrateSorting.Direction.ASCENDING -> MaterialSymbols.Rounded.ArrowUpward
                    SetMigrateSorting.Direction.DESCENDING -> MaterialSymbols.Rounded.ArrowDownward
                },
                onClick = viewModel::toggleSortingDirection,
            ),
        ),
        content = { contentPadding, _ ->
            MigrateSourceScreen(
                state = state,
                contentPadding = contentPadding,
                onStartMigration = { origin, destination ->
                    viewModel.setMigrationDestination(destination.id)
                    navigator.push(MigrateMangaScreen(origin.id))
                },
            )
        },
    )
}
