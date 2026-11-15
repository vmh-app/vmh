package eu.kanade.presentation.more.settings.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.category.visualName
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.more.settings.widget.TriStateListDialog
import mihon.app.di.appGraph
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.download.service.DownloadPreferences
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

object SettingsDownloadScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_downloads

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val getCategories = remember { context.appGraph.getCategories }
        val allCategories by getCategories.subscribe().collectAsState(initial = emptyList())
        val downloadPreferences = remember { context.appGraph.downloadPreferences }

        return listOf(
            getGeneralGroup(downloadPreferences),
            getDeleteChaptersGroup(downloadPreferences, allCategories),
            getAutoDownloadGroup(downloadPreferences, allCategories),
            getDownloadAheadGroup(downloadPreferences),
        )
    }

    @Composable
    private fun getGeneralGroup(downloadPreferences: DownloadPreferences): Preference {
        val downloadOnlyOverWifi by downloadPreferences.downloadOnlyOverWifi.collectAsState()
        val saveChaptersAsCBZ by downloadPreferences.saveChaptersAsCBZ.collectAsState()
        val parallelSourceLimit by downloadPreferences.parallelSourceLimit.collectAsState()
        val parallelPageLimit by downloadPreferences.parallelPageLimit.collectAsState()

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.pref_category_downloads)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.pref_category_downloads))
                VmhSettingsCard {
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.connected_to_wifi),
                        checked = downloadOnlyOverWifi,
                        onCheckedChange = { downloadPreferences.downloadOnlyOverWifi.set(it) },
                    )
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.save_chapter_as_cbz),
                        checked = saveChaptersAsCBZ,
                        onCheckedChange = { downloadPreferences.saveChaptersAsCBZ.set(it) },
                    )
                    VmhSliderPreference(
                        title = stringResource(MR.strings.pref_download_concurrent_sources),
                        value = parallelSourceLimit,
                        valueRange = 1..10,
                        onValueChange = { downloadPreferences.parallelSourceLimit.set(it) },
                    )
                    VmhSliderPreference(
                        title = stringResource(MR.strings.pref_download_concurrent_pages),
                        value = parallelPageLimit,
                        valueRange = 1..15,
                        onValueChange = { downloadPreferences.parallelPageLimit.set(it) },
                    )
                    // Añadimos el subtitle del slider de forma independiente para no alterar la firma del componente reutilizado
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(MR.strings.pref_download_concurrent_pages_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun getDeleteChaptersGroup(
        downloadPreferences: DownloadPreferences,
        categories: List<Category>,
    ): Preference {
        val removeAfterMarkedAsRead by downloadPreferences.removeAfterMarkedAsRead.collectAsState()
        val removeAfterReadSlots by downloadPreferences.removeAfterReadSlots.collectAsState()
        val removeBookmarkedChapters by downloadPreferences.removeBookmarkedChapters.collectAsState()
        val removeExcludeCategories by downloadPreferences.removeExcludeCategories.collectAsState()

        return Preference.PreferenceItem.CustomPreference(
            title = stringResource(MR.strings.pref_category_delete_chapters),
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

            val slotsOptions = mapOf(
                -1 to stringResource(MR.strings.disabled),
                0 to stringResource(MR.strings.last_read_chapter),
                1 to stringResource(MR.strings.second_to_last),
                2 to stringResource(MR.strings.third_to_last),
                3 to stringResource(MR.strings.fourth_to_last),
                4 to stringResource(MR.strings.fifth_to_last),
            )

            val categoryOptions = categories.associate { it.id.toString() to it.visualName }

            val titleRemoveAfterRead = stringResource(MR.strings.pref_remove_after_read)
            val titleExcludeCategories = stringResource(MR.strings.pref_remove_exclude_categories)

            Column {
                VmhSectionHeader(stringResource(MR.strings.pref_category_delete_chapters))
                VmhSettingsCard {
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_remove_after_marked_as_read),
                        checked = removeAfterMarkedAsRead,
                        onCheckedChange = { downloadPreferences.removeAfterMarkedAsRead.set(it) },
                    )
                    VmhTextPreference(
                        title = titleRemoveAfterRead,
                        subtitle = slotsOptions[removeAfterReadSlots] ?: "",
                        onClick = {
                            openListDialog(
                                title = titleRemoveAfterRead,
                                entries = slotsOptions,
                                selected = removeAfterReadSlots,
                                onSelect = { downloadPreferences.removeAfterReadSlots.set(it) },
                            )
                        },
                    )
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_remove_bookmarked_chapters),
                        checked = removeBookmarkedChapters,
                        onCheckedChange = { downloadPreferences.removeBookmarkedChapters.set(it) },
                    )
                    VmhTextPreference(
                        title = titleExcludeCategories,
                        subtitle = categoryOptions
                            .filterKeys { it in removeExcludeCategories }
                            .values
                            .joinToString()
                            .ifEmpty { stringResource(MR.strings.none) },
                        onClick = {
                            openMultiDialog(
                                title = titleExcludeCategories,
                                entries = categoryOptions,
                                selected = removeExcludeCategories,
                                onSelect = { downloadPreferences.removeExcludeCategories.set(it) },
                            )
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

    @Composable
    private fun getAutoDownloadGroup(
        downloadPreferences: DownloadPreferences,
        allCategories: List<Category>,
    ): Preference {
        val downloadNewChapters by downloadPreferences.downloadNewChapters.collectAsState()
        val downloadNewUnreadChaptersOnly by downloadPreferences.downloadNewUnreadChaptersOnly.collectAsState()
        val included by downloadPreferences.downloadNewChapterCategories.collectAsState()
        val excluded by downloadPreferences.downloadNewChapterCategoriesExclude.collectAsState()

        var showDialog by rememberSaveable { mutableStateOf(false) }

        return Preference.PreferenceItem.CustomPreference(
            title = stringResource(MR.strings.pref_category_auto_download),
        ) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.pref_category_auto_download))
                VmhSettingsCard {
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_download_new),
                        checked = downloadNewChapters,
                        onCheckedChange = { downloadPreferences.downloadNewChapters.set(it) },
                    )
                    if (downloadNewChapters) {
                        VmhSwitchPreference(
                            title = stringResource(MR.strings.pref_download_new_unread_chapters_only),
                            checked = downloadNewUnreadChaptersOnly,
                            onCheckedChange = { downloadPreferences.downloadNewUnreadChaptersOnly.set(it) },
                        )
                        VmhTextPreference(
                            title = stringResource(MR.strings.categories),
                            subtitle = getCategoriesLabel(allCategories, included, excluded),
                            onClick = { showDialog = true },
                        )
                    }
                }

                if (showDialog) {
                    TriStateListDialog(
                        title = stringResource(MR.strings.categories),
                        message = stringResource(MR.strings.pref_download_new_categories_details),
                        items = allCategories,
                        initialChecked = included.mapNotNull { id -> allCategories.find { it.id.toString() == id } },
                        initialInversed = excluded.mapNotNull { id -> allCategories.find { it.id.toString() == id } },
                        itemLabel = { it.visualName },
                        onDismissRequest = { showDialog = false },
                        onValueChanged = { newIncluded, newExcluded ->
                            downloadPreferences.downloadNewChapterCategories.set(
                                newIncluded.map {
                                    it.id.toString()
                                }.toSet(),
                            )
                            downloadPreferences.downloadNewChapterCategoriesExclude.set(
                                newExcluded.map {
                                    it.id.toString()
                                }.toSet(),
                            )
                            showDialog = false
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun getDownloadAheadGroup(downloadPreferences: DownloadPreferences): Preference {
        val autoDownloadWhileReading by downloadPreferences.autoDownloadWhileReading.collectAsState()

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.download_ahead)) {
            var listDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }

            fun <T> openListDialog(title: String, entries: Map<T, String>, selected: T, onSelect: (T) -> Unit) {
                listDialog = ListDialogData(title, entries as Map<Any, String>, selected as Any) { onSelect(it as T) }
            }

            val disabledText = stringResource(MR.strings.disabled)
            val options = listOf(0, 2, 3, 5, 10).associateWith {
                if (it == 0) disabledText else pluralStringResource(MR.plurals.next_unread_chapters, count = it, it)
            }
            val titleAutoDownload = stringResource(MR.strings.auto_download_while_reading)

            Column {
                VmhSectionHeader(stringResource(MR.strings.download_ahead))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = titleAutoDownload,
                        subtitle = options[autoDownloadWhileReading] ?: "",
                        onClick = {
                            openListDialog(
                                title = titleAutoDownload,
                                entries = options,
                                selected = autoDownloadWhileReading,
                                onSelect = { downloadPreferences.autoDownloadWhileReading.set(it) },
                            )
                        },
                    )
                    // Sustituto transparente a InfoPreference
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(MR.strings.download_ahead_info),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
    private fun getCategoriesLabel(
        allCategories: List<Category>,
        included: Set<String>,
        excluded: Set<String>,
    ): String {
        val includedItems = included.mapNotNull { id -> allCategories.find { it.id.toString() == id }?.visualName }
        val excludedItems = excluded.mapNotNull { id -> allCategories.find { it.id.toString() == id }?.visualName }
        if (includedItems.isEmpty() && excludedItems.isEmpty()) {
            return stringResource(MR.strings.all)
        }
        return buildString {
            if (includedItems.isNotEmpty()) {
                append(stringResource(MR.strings.include, includedItems.joinToString()))
            }
            if (excludedItems.isNotEmpty()) {
                if (isNotEmpty()) appendLine()
                append(stringResource(MR.strings.exclude, excludedItems.joinToString()))
            }
        }
    }
}
