package eu.kanade.presentation.more.settings.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.category.visualName
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.more.settings.widget.TriStateListDialog
import eu.kanade.tachiyomi.data.library.LibraryUpdateWorker
import eu.kanade.tachiyomi.ui.category.CategoryScreen
import kotlinx.coroutines.launch
import mihon.app.di.appGraph
import tachiyomi.core.common.i18n.pluralStringResource
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.domain.library.service.LibraryPreferences.Companion.DEVICE_CHARGING
import tachiyomi.domain.library.service.LibraryPreferences.Companion.DEVICE_NETWORK_NOT_METERED
import tachiyomi.domain.library.service.LibraryPreferences.Companion.DEVICE_ONLY_ON_WIFI
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MANGA_HAS_UNREAD
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MANGA_NON_COMPLETED
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MANGA_NON_READ
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MANGA_OUTSIDE_RELEASE_PERIOD
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MARK_DUPLICATE_CHAPTER_READ_EXISTING
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MARK_DUPLICATE_CHAPTER_READ_NEW
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

object SettingsLibraryScreen : SearchableSettings {

    @Composable
    @ReadOnlyComposable
    override fun getTitleRes() = MR.strings.pref_category_library

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val getCategories = remember { context.appGraph.getCategories }
        val libraryPreferences = remember { context.appGraph.libraryPreferences }
        val allCategories by getCategories.subscribe().collectAsState(initial = emptyList())

        return listOf(
            getCategoriesGroup(LocalNavigator.currentOrThrow, allCategories, libraryPreferences),
            getGlobalUpdateGroup(allCategories, libraryPreferences),
            getBehaviorGroup(libraryPreferences),
        )
    }

    @Composable
    private fun getCategoriesGroup(
        navigator: Navigator,
        allCategories: List<Category>,
        libraryPreferences: LibraryPreferences,
    ): Preference {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val userCategoriesCount = allCategories.filterNot(Category::isSystemCategory).size
        val categorizedDisplaySettings by libraryPreferences.categorizedDisplaySettings.collectAsState()
        val defaultCategory by libraryPreferences.defaultCategory.collectAsState()

        return Preference.PreferenceItem.CustomPreference(title = context.stringResource(MR.strings.categories)) {
            var listDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }
            fun <T> openListDialog(title: String, entries: Map<T, String>, selected: T, onSelect: (T) -> Unit) {
                listDialog = ListDialogData(title, entries as Map<Any, String>, selected as Any) { onSelect(it as T) }
            }

            val defaultCategoryIds = listOf(libraryPreferences.defaultCategory.defaultValue()) +
                allCategories.map { it.id.toInt() }
            val defaultCategoryLabels = listOf(
                context.stringResource(MR.strings.default_category_summary),
            ) + allCategories.map { it.visualName }
            val defaultCategoryOptions = defaultCategoryIds.zip(defaultCategoryLabels).toMap()

            Column {
                VmhSectionHeader(context.stringResource(MR.strings.categories))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.action_edit_categories),
                        subtitle = context.pluralStringResource(
                            MR.plurals.num_categories,
                            count = userCategoriesCount,
                            userCategoriesCount,
                        ),
                        onClick = { navigator.push(CategoryScreen()) },
                    )

                    VmhTextPreference(
                        title = context.stringResource(MR.strings.default_category),
                        subtitle = defaultCategoryOptions[defaultCategory] ?: "",
                        onClick = {
                            openListDialog(
                                title = context.stringResource(MR.strings.default_category),
                                entries = defaultCategoryOptions,
                                selected = defaultCategory,
                                onSelect = { libraryPreferences.defaultCategory.set(it) },
                            )
                        },
                    )

                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.categorized_display_settings),
                        checked = categorizedDisplaySettings,
                        onCheckedChange = {
                            libraryPreferences.categorizedDisplaySettings.set(it)
                            if (!it) {
                                scope.launch {
                                    context.appGraph.resetCategoryFlags.await()
                                }
                            }
                        },
                    )
                }

                listDialog?.let { data ->
                    VmhListDialog(
                        title = data.title,
                        entries = data.entries,
                        selectedValue = data.selectedValue,
                        onSelect = {
                            data.onSelect(it)
                            listDialog = null
                        },
                        onDismiss = { listDialog = null },
                    )
                }
            }
        }
    }

    @Composable
    private fun getGlobalUpdateGroup(
        allCategories: List<Category>,
        libraryPreferences: LibraryPreferences,
    ): Preference {
        val context = LocalContext.current
        val autoUpdateInterval by libraryPreferences.autoUpdateInterval.collectAsState()
        val included by libraryPreferences.updateCategories.collectAsState()
        val excluded by libraryPreferences.updateCategoriesExclude.collectAsState()
        val autoUpdateDeviceRestrictions by libraryPreferences.autoUpdateDeviceRestrictions.collectAsState()
        val autoUpdateMetadata by libraryPreferences.autoUpdateMetadata.collectAsState()
        val autoUpdateMangaRestrictions by libraryPreferences.autoUpdateMangaRestrictions.collectAsState()
        val newShowUpdatesCount by libraryPreferences.newShowUpdatesCount.collectAsState()

        var showCategoriesDialog by rememberSaveable { mutableStateOf(false) }

        return Preference.PreferenceItem.CustomPreference(
            title = context.stringResource(MR.strings.pref_category_library_update),
        ) {
            var listDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }
            var multiDialog by remember { mutableStateOf<MultiListDialogData<Any>?>(null) }

            fun <T> openListDialog(title: String, entries: Map<T, String>, selected: T, onSelect: (T) -> Unit) {
                listDialog = ListDialogData(title, entries as Map<Any, String>, selected as Any) { onSelect(it as T) }
            }

            fun <T> openMultiDialog(
                title: String,
                entries: Map<T, String>,
                selected: Set<T>,
                onSelect: (Set<T>) -> Unit,
            ) {
                multiDialog = MultiListDialogData(
                    title = title,
                    entries = entries as Map<Any, String>,
                    selectedValues = selected as Set<Any>,
                ) { onSelect(it as Set<T>) }
            }

            val intervalOptions = mapOf(
                0 to context.stringResource(MR.strings.update_never),
                12 to context.stringResource(MR.strings.update_12hour),
                24 to context.stringResource(MR.strings.update_24hour),
                48 to context.stringResource(MR.strings.update_48hour),
                72 to context.stringResource(MR.strings.update_72hour),
                168 to context.stringResource(MR.strings.update_weekly),
            )

            val restrictionOptions = mapOf(
                DEVICE_ONLY_ON_WIFI to context.stringResource(MR.strings.connected_to_wifi),
                DEVICE_NETWORK_NOT_METERED to context.stringResource(MR.strings.network_not_metered),
                DEVICE_CHARGING to context.stringResource(MR.strings.charging),
            )

            val smartUpdateOptions = mapOf(
                MANGA_HAS_UNREAD to context.stringResource(MR.strings.pref_update_only_completely_read),
                MANGA_NON_READ to context.stringResource(MR.strings.pref_update_only_started),
                MANGA_NON_COMPLETED to context.stringResource(MR.strings.pref_update_only_non_completed),
                MANGA_OUTSIDE_RELEASE_PERIOD to context.stringResource(MR.strings.pref_update_only_in_release_period),
            )

            Column {
                VmhSectionHeader(context.stringResource(MR.strings.pref_category_library_update))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_library_update_interval),
                        subtitle = intervalOptions[autoUpdateInterval] ?: "",
                        onClick = {
                            openListDialog(
                                title = context.stringResource(MR.strings.pref_library_update_interval),
                                entries = intervalOptions,
                                selected = autoUpdateInterval,
                                onSelect = {
                                    libraryPreferences.autoUpdateInterval.set(it)
                                    LibraryUpdateWorker.setupTask(context, it)
                                },
                            )
                        },
                    )

                    if (autoUpdateInterval > 0) {
                        VmhTextPreference(
                            title = context.stringResource(MR.strings.pref_library_update_restriction),
                            subtitle = restrictionOptions
                                .filterKeys { it in autoUpdateDeviceRestrictions }
                                .values
                                .joinToString()
                                .ifEmpty { context.stringResource(MR.strings.none) },
                            onClick = {
                                openMultiDialog(
                                    title = context.stringResource(MR.strings.pref_library_update_restriction),
                                    entries = restrictionOptions,
                                    selected = autoUpdateDeviceRestrictions,
                                    onSelect = {
                                        libraryPreferences.autoUpdateDeviceRestrictions.set(it)
                                        ContextCompat.getMainExecutor(context).execute {
                                            LibraryUpdateWorker.setupTask(context)
                                        }
                                    },
                                )
                            },
                        )
                    }

                    VmhTextPreference(
                        title = context.stringResource(MR.strings.categories),
                        subtitle = getCategoriesLabel(allCategories, included, excluded),
                        onClick = { showCategoriesDialog = true },
                    )

                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_library_update_refresh_metadata),
                        subtitle = context.stringResource(MR.strings.pref_library_update_refresh_metadata_summary),
                        checked = autoUpdateMetadata,
                        onCheckedChange = { libraryPreferences.autoUpdateMetadata.set(it) },
                    )

                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_library_update_smart_update),
                        subtitle = smartUpdateOptions
                            .filterKeys { it in autoUpdateMangaRestrictions }
                            .values
                            .joinToString()
                            .ifEmpty { context.stringResource(MR.strings.none) },
                        onClick = {
                            openMultiDialog(
                                title = context.stringResource(MR.strings.pref_library_update_smart_update),
                                entries = smartUpdateOptions,
                                selected = autoUpdateMangaRestrictions,
                                onSelect = { libraryPreferences.autoUpdateMangaRestrictions.set(it) },
                            )
                        },
                    )

                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_library_update_show_tab_badge),
                        checked = newShowUpdatesCount,
                        onCheckedChange = { libraryPreferences.newShowUpdatesCount.set(it) },
                    )
                }

                listDialog?.let { data ->
                    VmhListDialog(
                        title = data.title,
                        entries = data.entries,
                        selectedValue = data.selectedValue,
                        onSelect = {
                            data.onSelect(it)
                            listDialog = null
                        },
                        onDismiss = { listDialog = null },
                    )
                }

                multiDialog?.let { data ->
                    VmhMultiSelectDialog(
                        title = data.title,
                        entries = data.entries,
                        selectedValues = data.selectedValues,
                        onSelect = {
                            data.onSelect(it)
                            multiDialog = null
                        },
                        onDismiss = { multiDialog = null },
                    )
                }

                if (showCategoriesDialog) {
                    TriStateListDialog(
                        title = context.stringResource(MR.strings.categories),
                        message = context.stringResource(MR.strings.pref_library_update_categories_details),
                        items = allCategories,
                        initialChecked = included.mapNotNull { id -> allCategories.find { it.id.toString() == id } },
                        initialInversed = excluded.mapNotNull { id -> allCategories.find { it.id.toString() == id } },
                        itemLabel = { it.visualName },
                        onDismissRequest = { showCategoriesDialog = false },
                        onValueChanged = { newIncluded, newExcluded ->
                            libraryPreferences.updateCategories.set(newIncluded.map { it.id.toString() }.toSet())
                            libraryPreferences.updateCategoriesExclude.set(newExcluded.map { it.id.toString() }.toSet())
                            showCategoriesDialog = false
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun getBehaviorGroup(
        libraryPreferences: LibraryPreferences,
    ): Preference {
        val context = LocalContext.current
        val swipeToStartAction by libraryPreferences.swipeToStartAction.collectAsState()
        val swipeToEndAction by libraryPreferences.swipeToEndAction.collectAsState()
        val markDuplicate by libraryPreferences.markDuplicateReadChapterAsRead.collectAsState()
        val hideMissing by libraryPreferences.hideMissingChapters.collectAsState()

        return Preference.PreferenceItem.CustomPreference(title = context.stringResource(MR.strings.pref_behavior)) {
            var listDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }
            var multiDialog by remember { mutableStateOf<MultiListDialogData<Any>?>(null) }

            fun <T> openListDialog(title: String, entries: Map<T, String>, selected: T, onSelect: (T) -> Unit) {
                listDialog = ListDialogData(title, entries as Map<Any, String>, selected as Any) { onSelect(it as T) }
            }

            fun <T> openMultiDialog(
                title: String,
                entries: Map<T, String>,
                selected: Set<T>,
                onSelect: (Set<T>) -> Unit,
            ) {
                multiDialog =
                    MultiListDialogData(title, entries as Map<Any, String>, selected as Set<Any>) {
                        onSelect(it as Set<T>)
                    }
            }

            val swipeActions = mapOf(
                LibraryPreferences.ChapterSwipeAction.Disabled to context.stringResource(MR.strings.disabled),
                LibraryPreferences.ChapterSwipeAction.ToggleBookmark to
                    context.stringResource(MR.strings.action_bookmark),
                LibraryPreferences.ChapterSwipeAction.ToggleRead to
                    context.stringResource(MR.strings.action_mark_as_read),
                LibraryPreferences.ChapterSwipeAction.Download to context.stringResource(MR.strings.action_download),
            )

            val duplicateActions = mapOf(
                MARK_DUPLICATE_CHAPTER_READ_EXISTING to
                    context.stringResource(MR.strings.pref_mark_duplicate_read_chapter_read_existing),
                MARK_DUPLICATE_CHAPTER_READ_NEW to
                    context.stringResource(MR.strings.pref_mark_duplicate_read_chapter_read_new),
            )

            Column {
                VmhSectionHeader(context.stringResource(MR.strings.pref_behavior))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_chapter_swipe_start),
                        subtitle = swipeActions[swipeToStartAction] ?: "",
                        onClick = {
                            openListDialog(
                                title = context.stringResource(MR.strings.pref_chapter_swipe_start),
                                entries = swipeActions,
                                selected = swipeToStartAction,
                                onSelect = { libraryPreferences.swipeToStartAction.set(it) },
                            )
                        },
                    )
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_chapter_swipe_end),
                        subtitle = swipeActions[swipeToEndAction] ?: "",
                        onClick = {
                            openListDialog(
                                title = context.stringResource(MR.strings.pref_chapter_swipe_end),
                                entries = swipeActions,
                                selected = swipeToEndAction,
                                onSelect = { libraryPreferences.swipeToEndAction.set(it) },
                            )
                        },
                    )
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_mark_duplicate_read_chapter_read),
                        subtitle = duplicateActions.filterKeys {
                            it in markDuplicate
                        }.values.joinToString().ifEmpty { context.stringResource(MR.strings.none) },
                        onClick = {
                            openMultiDialog(
                                title = context.stringResource(MR.strings.pref_mark_duplicate_read_chapter_read),
                                entries = duplicateActions,
                                selected = markDuplicate,
                                onSelect = { libraryPreferences.markDuplicateReadChapterAsRead.set(it) },
                            )
                        },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_hide_missing_chapter_indicators),
                        checked = hideMissing,
                        onCheckedChange = { libraryPreferences.hideMissingChapters.set(it) },
                    )
                }

                listDialog?.let { data ->
                    VmhListDialog(
                        title = data.title,
                        entries = data.entries,
                        selectedValue = data.selectedValue,
                        onSelect = {
                            data.onSelect(it)
                            listDialog = null
                        },
                        onDismiss = { listDialog = null },
                    )
                }

                multiDialog?.let { data ->
                    VmhMultiSelectDialog(
                        title = data.title,
                        entries = data.entries,
                        selectedValues = data.selectedValues,
                        onSelect = {
                            data.onSelect(it)
                            multiDialog = null
                        },
                        onDismiss = { multiDialog = null },
                    )
                }
            }
        }
    }
}

// --- Componentes compartidos y utilitarios para VMH Settings ---
