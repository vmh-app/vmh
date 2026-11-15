package mihon.feature.migration.config

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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.icerock.moko.resources.StringResource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.presentation.browse.components.SourceIcon
import eu.kanade.presentation.components.VmhContinueBar
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.ui.browse.migration.search.MigrateSearchScreen
import eu.kanade.tachiyomi.util.system.LocaleHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mihon.feature.migration.list.MigrationListScreen
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ArrowBack
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.Deselect
import mihon.icons.materialsymbols.rounded.DragHandle
import mihon.icons.materialsymbols.rounded.MoreVert
import mihon.icons.materialsymbols.rounded.SelectAll
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import tachiyomi.domain.source.model.Source
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.LoadingScreen

class MigrationConfigScreen(private val mangaIds: Collection<Long>) : Screen() {

    constructor(mangaId: Long) : this(listOf(mangaId))

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val viewModel = metroViewModel<Model>()
        val state by viewModel.state.collectAsState()

        var migrationSheetOpen by rememberSaveable { mutableStateOf(false) }

        fun continueMigration(openSheet: Boolean, extraSearchQuery: String?) {
            val mangaId = mangaIds.singleOrNull()
            if (mangaId == null && openSheet) {
                migrationSheetOpen = true
                return
            }
            val screen = if (mangaId == null) {
                MigrationListScreen(mangaIds, extraSearchQuery)
            } else {
                MigrateSearchScreen(mangaId)
            }
            navigator.replace(screen)
        }

        if (state.isLoading) {
            LoadingScreen()
            return
        }

        val (selectedSources, availableSources) = state.sources.partition { it.isSelected }
        val showLanguage by remember(state) {
            derivedStateOf {
                state.sources.distinctBy { it.source.lang }.size > 1
            }
        }

        val lazyListState = rememberLazyListState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            VmhConfigHeader(
                selected = selectedSources.size,
                total = state.sources.size,
                onBack = navigator::pop,
                onSelectAll = { viewModel.toggleSelection(Model.SelectionConfig.All) },
                onSelectNone = { viewModel.toggleSelection(Model.SelectionConfig.None) },
                onSelectEnabled = { viewModel.toggleSelection(Model.SelectionConfig.Enabled) },
                onSelectPinned = { viewModel.toggleSelection(Model.SelectionConfig.Pinned) },
            )

            val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                val fromIndex = selectedSources.indexOfFirst { it.id == from.key }
                val toIndex = selectedSources.indexOfFirst { it.id == to.key }
                if (fromIndex == -1 || toIndex == -1) return@rememberReorderableLazyListState
                viewModel.orderSource(fromIndex, toIndex)
            }

            FastScrollLazyColumn(
                modifier = Modifier.weight(1f),
                state = lazyListState,
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                listOf(selectedSources, availableSources).fastForEachIndexed { listIndex, sources ->
                    val selectedSourceList = listIndex == 0
                    if (sources.isNotEmpty()) {
                        val headerPrefix = if (selectedSourceList) "selected" else "available"
                        item("$headerPrefix-header") {
                            VmhSourcesSectionHeader(
                                labelRes = if (selectedSourceList) {
                                    MR.strings.migrationConfigScreen_selectedHeader
                                } else {
                                    MR.strings.migrationConfigScreen_availableHeader
                                },
                                count = sources.size,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                    itemsIndexed(
                        items = sources,
                        key = { _, item -> item.id },
                    ) { _, item ->
                        SourceItemContainer(
                            source = item,
                            showLanguage = showLanguage,
                            dragEnabled = selectedSourceList && sources.size > 1,
                            state = reorderableState,
                            key = { if (selectedSourceList) it.id else "available-${it.id}" },
                            onClick = { viewModel.toggleSelection(item.id) },
                        )
                    }
                }
            }

            VmhContinueBar(
                enabled = true,
                onClick = {
                    viewModel.saveSources()
                    continueMigration(openSheet = true, extraSearchQuery = null)
                },
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
            )
        }

        if (migrationSheetOpen) {
            MigrationConfigScreenSheet(
                preferences = viewModel.sourcePreferences,
                onDismissRequest = { migrationSheetOpen = false },
                onStartMigration = { extraSearchQuery ->
                    migrationSheetOpen = false
                    continueMigration(openSheet = false, extraSearchQuery = extraSearchQuery)
                },
            )
        }
    }

    @Composable
    private fun LazyItemScope.SourceItemContainer(
        source: MigrationSource,
        showLanguage: Boolean,
        dragEnabled: Boolean,
        state: ReorderableLazyListState,
        key: (MigrationSource) -> Any,
        onClick: () -> Unit,
    ) {
        ReorderableItem(
            state = state,
            key = key(source),
            enabled = dragEnabled,
        ) { _ ->
            VmhSourceCard(
                source = source,
                showLanguage = showLanguage,
                selected = source.isSelected,
                dragEnabled = dragEnabled,
                modifier = Modifier.animateItem(),
                scope = this@ReorderableItem,
                onClick = onClick,
            )
        }
    }

    @Composable
    private fun VmhSourceCard(
        source: MigrationSource,
        showLanguage: Boolean,
        selected: Boolean,
        dragEnabled: Boolean,
        modifier: Modifier = Modifier,
        scope: ReorderableCollectionItemScope,
        onClick: () -> Unit,
    ) {
        val colors = MaterialTheme.colorScheme

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable(onClick = onClick),
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
                        end = 10.dp,
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

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp)),
                ) {
                    SourceIcon(source = source.source, modifier = Modifier.matchParentSize())
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = source.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (showLanguage) {
                        Text(
                            text = LocaleHelper.getShortDisplayName(source.shortLanguage, uppercase = true),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                if (dragEnabled) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.DragHandle,
                        contentDescription = null,
                        modifier = with(scope) {
                            Modifier
                                .size(24.dp)
                                .draggableHandle()
                        },
                        tint = colors.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                }

                VmhSelectionCircle(selected = selected)
            }
        }
    }

    @Composable
    private fun VmhConfigHeader(
        selected: Int,
        total: Int,
        onBack: () -> Unit,
        onSelectAll: () -> Unit,
        onSelectNone: () -> Unit,
        onSelectEnabled: () -> Unit,
        onSelectPinned: () -> Unit,
    ) {
        val colors = MaterialTheme.colorScheme
        var menuOpen by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VmhCircleAction(
                    icon = MaterialSymbols.AutoMirroredRounded.ArrowBack,
                    description = stringResource(MR.strings.action_bar_up_description),
                    onClick = onBack,
                )
                Spacer(Modifier.weight(1f))
                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.SelectAll,
                    description = stringResource(MR.strings.migrationConfigScreen_selectAllLabel),
                    onClick = onSelectAll,
                )
                Spacer(Modifier.width(8.dp))
                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.Deselect,
                    description = stringResource(MR.strings.migrationConfigScreen_selectNoneLabel),
                    onClick = onSelectNone,
                )
                Spacer(Modifier.width(8.dp))
                Box {
                    VmhCircleAction(
                        icon = MaterialSymbols.Rounded.MoreVert,
                        description = null,
                        onClick = { menuOpen = true },
                    )
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(MR.strings.migrationConfigScreen_selectEnabledLabel)) },
                            onClick = {
                                menuOpen = false
                                onSelectEnabled()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(MR.strings.migrationConfigScreen_selectPinnedLabel)) },
                            onClick = {
                                menuOpen = false
                                onSelectPinned()
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(MR.strings.label_migration).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = colors.primary,
            )
            Text(
                text = stringResource(MR.strings.vmh_migration_destinations),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(MR.strings.vmh_migration_sources_ratio, selected, total),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
            )
        }
    }

    @Composable
    private fun VmhCircleAction(
        icon: ImageVector,
        description: String?,
        onClick: () -> Unit,
    ) {
        val colors = MaterialTheme.colorScheme

        Surface(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = colors.surface,
            contentColor = colors.onSurfaceVariant,
            border = BorderStroke(1.dp, colors.outlineVariant),
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = description,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

    @Composable
    private fun VmhSourcesSectionHeader(
        labelRes: StringResource,
        count: Int,
        modifier: Modifier = Modifier,
    ) {
        val colors = MaterialTheme.colorScheme

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 14.dp, top = 16.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(labelRes).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                ),
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

    @Inject
    @ViewModelKey
    @ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
    class Model(
        val sourcePreferences: SourcePreferences,
        private val sourceManager: SourceManager,
    ) : ViewModel() {

        val state: StateFlow<Model.State>
            field = MutableStateFlow<Model.State>(State())

        private val sourcesComparator = { includedSources: List<Long> ->
            compareBy<MigrationSource>(
                { !it.isSelected },
                { includedSources.indexOf(it.id) },
                { with(it) { "$name ($shortLanguage)" } },
            )
        }

        init {
            viewModelScope.launch(Dispatchers.IO) {
                initSources()
                state.update { it.copy(isLoading = false) }
            }
        }

        private fun updateSources(action: (List<MigrationSource>) -> List<MigrationSource>) {
            state.update { state ->
                val updatedSources = action(state.sources)
                val includedSources = updatedSources.mapNotNull { if (!it.isSelected) null else it.id }
                state.copy(sources = updatedSources.sortedWith(sourcesComparator(includedSources)))
            }
            saveSources()
        }

        private suspend fun initSources() {
            val languages = sourcePreferences.enabledLanguages.get()
            val pinnedSources = sourcePreferences.pinnedSources.get().mapNotNull { it.toLongOrNull() }
            val includedSources = sourcePreferences.migrationSources.get()
            val disabledSources = sourcePreferences.disabledSources.get()
                .mapNotNull { it.toLongOrNull() }
            val sources = sourceManager.getAll()
                .asSequence()
                .filterIsInstance<HttpSource>()
                .filter { it.lang in languages }
                .map {
                    val source = Source(
                        id = it.id,
                        lang = it.lang,
                        name = it.name,
                        supportsLatest = false,
                        isStub = false,
                    )
                    MigrationSource(
                        source = source,
                        isSelected = when {
                            includedSources.isNotEmpty() -> source.id in includedSources
                            pinnedSources.isNotEmpty() -> source.id in pinnedSources
                            else -> source.id !in disabledSources
                        },
                    )
                }
                .toList()

            state.update { state ->
                state.copy(sources = sources.sortedWith(sourcesComparator(includedSources)))
            }
        }

        fun toggleSelection(id: Long) {
            updateSources { sources ->
                sources.map { source ->
                    source.copy(isSelected = if (source.source.id == id) !source.isSelected else source.isSelected)
                }
            }
        }

        fun toggleSelection(config: SelectionConfig) {
            val pinnedSources = sourcePreferences.pinnedSources.get().mapNotNull { it.toLongOrNull() }
            val disabledSources = sourcePreferences.disabledSources.get()
                .mapNotNull { it.toLongOrNull() }
            val isSelected: (Long) -> Boolean = {
                when (config) {
                    SelectionConfig.All -> true
                    SelectionConfig.None -> false
                    SelectionConfig.Pinned -> it in pinnedSources
                    SelectionConfig.Enabled -> it !in disabledSources
                }
            }
            updateSources { sources ->
                sources.map { source ->
                    source.copy(isSelected = isSelected(source.source.id))
                }
            }
        }

        fun orderSource(from: Int, to: Int) {
            updateSources {
                it.toMutableList()
                    .apply {
                        add(to, removeAt(from))
                    }
                    .toList()
            }
        }

        fun saveSources() {
            state.value.sources
                .filter { source -> source.isSelected }
                .map { source -> source.source.id }
                .let { sources -> sourcePreferences.migrationSources.set(sources) }
        }

        data class State(
            val isLoading: Boolean = true,
            val sources: List<MigrationSource> = emptyList(),
        )

        enum class SelectionConfig {
            All,
            None,
            Pinned,
            Enabled,
        }
    }

    data class MigrationSource(
        val source: Source,
        val isSelected: Boolean,
    ) {
        val id: Long
            inline get() = source.id

        val name: String
            inline get() = source.name

        val shortLanguage: String = LocaleHelper.getShortDisplayName(source.lang)
    }
}
