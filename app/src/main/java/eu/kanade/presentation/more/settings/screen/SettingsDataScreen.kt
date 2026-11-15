package eu.kanade.presentation.more.settings.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.hippo.unifile.UniFile
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.more.settings.screen.data.CreateBackupScreen
import eu.kanade.presentation.more.settings.screen.data.RestoreBackupScreen
import eu.kanade.presentation.more.settings.screen.data.StorageInfo
import eu.kanade.presentation.util.relativeTimeSpanString
import eu.kanade.tachiyomi.data.backup.create.BackupCreateWorker
import eu.kanade.tachiyomi.data.backup.restore.BackupRestoreWorker
import eu.kanade.tachiyomi.data.export.LibraryExporter
import eu.kanade.tachiyomi.data.export.LibraryExporter.ExportOptions
import eu.kanade.tachiyomi.util.system.DeviceUtil
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.workManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import logcat.LogPriority
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.Help
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.storage.displayablePath
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.backup.service.BackupPreferences
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.storage.service.StoragePreferences
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.TextButton
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

object SettingsDataScreen : SearchableSettings {
    val restorePreferenceKeyString = MR.strings.label_backup

    const val HELP_URL = "https://mihon.app/docs/faq/storage"

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.label_data_storage

    @Composable
    override fun RowScope.AppBarAction() {
        val uriHandler = LocalUriHandler.current
        IconButton(onClick = { uriHandler.openUri(HELP_URL) }) {
            Icon(
                imageVector = MaterialSymbols.AutoMirroredRounded.Help,
                contentDescription = stringResource(MR.strings.tracking_guide),
            )
        }
    }

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val backupPreferences = remember { context.appGraph.backupPreferences }
        val storagePreferences = remember { context.appGraph.storagePreferences }

        return listOf(
            getStorageLocationPref(storagePreferences),
            getBackupAndRestoreGroup(backupPreferences),
            getDataGroup(),
            getExportGroup(),
        )
    }

    @Composable
    fun storageLocationPicker(
        storageDirPref: tachiyomi.core.common.preference.Preference<String>,
    ): ManagedActivityResultLauncher<Uri?, Uri?> {
        val context = LocalContext.current

        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree(),
        ) { uri ->
            if (uri != null) {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                try {
                    context.contentResolver.takePersistableUriPermission(uri, flags)
                } catch (e: SecurityException) {
                    logcat(LogPriority.ERROR, e)
                    context.toast(MR.strings.file_picker_uri_permission_unsupported)
                }

                UniFile.fromUri(context, uri)?.let {
                    storageDirPref.set(it.uri.toString())
                }
            }
        }
    }

    @Composable
    fun storageLocationText(
        storageDirPref: tachiyomi.core.common.preference.Preference<String>,
    ): String {
        val context = LocalContext.current
        val storageDir by storageDirPref.collectAsState()

        if (storageDir == storageDirPref.defaultValue()) {
            return stringResource(MR.strings.no_location_set)
        }

        return remember(storageDir) {
            val file = UniFile.fromUri(context, storageDir.toUri())
            file?.displayablePath
        } ?: stringResource(MR.strings.invalid_location, storageDir)
    }

    @Composable
    private fun getStorageLocationPref(storagePreferences: StoragePreferences): Preference {
        val ctx = LocalContext.current
        val pickStorageLocation = storageLocationPicker(storagePreferences.baseStorageDirectory)
        val errPicker = stringResource(MR.strings.file_picker_error)

        return Preference.PreferenceItem.CustomPreference(
            title = stringResource(MR.strings.pref_storage_location),
        ) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.pref_storage_location))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_storage_location),
                        subtitle = storageLocationText(storagePreferences.baseStorageDirectory),
                        onClick = {
                            try {
                                pickStorageLocation.launch(null)
                            } catch (e: ActivityNotFoundException) {
                                ctx.toast(errPicker)
                            }
                        },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 14.dp),
                    ) {
                        Text(
                            text = stringResource(MR.strings.pref_storage_location_info),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun getBackupAndRestoreGroup(backupPreferences: BackupPreferences): Preference {
        val ctx = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var listDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }

        fun <T> openListDialog(title: String, entries: Map<T, String>, selected: T, onSelect: (T) -> Unit) {
            listDialog = ListDialogData(title, entries as Map<Any, String>, selected as Any) { onSelect(it as T) }
        }

        val lastAutoBackup by backupPreferences.lastAutoBackupTimestamp.collectAsState()
        val backupInterval by backupPreferences.backupInterval.collectAsState()

        val errNullUri = stringResource(MR.strings.file_null_uri_error)
        val chooseBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            if (it == null) {
                ctx.toast(errNullUri)
                return@rememberLauncherForActivityResult
            }
            try {
                ctx.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                logcat(LogPriority.ERROR, e)
            }
            navigator.push(RestoreBackupScreen(it.toString()))
        }

        val warnMiui = stringResource(MR.strings.restore_miui_warning)
        val warnProgress = stringResource(MR.strings.restore_in_progress)

        val intervalOptions = mapOf(
            0 to stringResource(MR.strings.off),
            6 to stringResource(MR.strings.update_6hour),
            12 to stringResource(MR.strings.update_12hour),
            24 to stringResource(MR.strings.update_24hour),
            48 to stringResource(MR.strings.update_48hour),
            168 to stringResource(MR.strings.update_weekly),
        )
        val titleBackupInterval = stringResource(MR.strings.pref_backup_interval)

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.label_backup)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.label_backup))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_create_backup),
                        onClick = { navigator.push(CreateBackupScreen()) },
                    )
                    DestructiveTextPreference(
                        title = stringResource(MR.strings.pref_restore_backup),
                        onClick = {
                            scope.launch {
                                if (!BackupRestoreWorker.isRunning(ctx.workManager)) {
                                    if (DeviceUtil.isMiui && DeviceUtil.isMiuiOptimizationDisabled()) {
                                        ctx.toast(warnMiui)
                                    }
                                    chooseBackup.launch(arrayOf("*/*"))
                                } else {
                                    ctx.toast(warnProgress)
                                }
                            }
                        },
                    )
                    VmhTextPreference(
                        title = titleBackupInterval,
                        subtitle = intervalOptions[backupInterval] ?: "",
                        onClick = {
                            openListDialog(
                                title = titleBackupInterval,
                                entries = intervalOptions,
                                selected = backupInterval,
                                onSelect = {
                                    backupPreferences.backupInterval.set(it)
                                    BackupCreateWorker.setupTask(ctx, it)
                                },
                            )
                        },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 14.dp),
                    ) {
                        val autoInfo =
                            stringResource(MR.strings.last_auto_backup_info, relativeTimeSpanString(lastAutoBackup))
                        Text(
                            text = stringResource(MR.strings.backup_info) + "\n\n" + autoInfo,
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
    private fun getDataGroup(): Preference {
        val ctx = LocalContext.current
        val scope = rememberCoroutineScope()
        val libraryPreferences = remember { ctx.appGraph.libraryPreferences }
        val autoClearChapterCache by libraryPreferences.autoClearChapterCache.collectAsState()
        val chapterCache = remember { ctx.appGraph.chapterCache }

        var cacheReadableSizeSema by remember { mutableIntStateOf(0) }
        val cacheReadableSize by produceState("", cacheReadableSizeSema) {
            value = chapterCache.readableSize()
        }
        val msgError = stringResource(MR.strings.cache_delete_error)
        val isWide = LocalContext.current.resources.configuration.screenWidthDp > 500

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.pref_storage_usage)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.pref_storage_usage))
                VmhSettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(if (isWide) 0.5f else 1f)
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        StorageInfo()
                    }
                    DestructiveTextPreference(
                        title = stringResource(MR.strings.pref_clear_chapter_cache),
                        subtitle = cacheReadableSize,
                        onClick = {
                            scope.launch {
                                try {
                                    val deletedFiles = chapterCache.clear()
                                    ctx.toast(ctx.stringResource(MR.strings.cache_deleted, deletedFiles))
                                    cacheReadableSizeSema++
                                } catch (e: Exception) {
                                    if (e is CancellationException) throw e
                                    logcat(LogPriority.ERROR, e)
                                    ctx.toast(msgError)
                                }
                            }
                        },
                    )
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_auto_clear_chapter_cache),
                        checked = autoClearChapterCache,
                        onCheckedChange = { libraryPreferences.autoClearChapterCache.set(it) },
                    )
                }
            }
        }
    }

    @Composable
    private fun getExportGroup(): Preference {
        var showDialog by remember { mutableStateOf(false) }
        var exportOptions by remember {
            mutableStateOf(ExportOptions(includeTitle = true, includeAuthor = true, includeArtist = true))
        }

        val ctx = LocalContext.current
        val scope = rememberCoroutineScope()
        val getFavorites = remember { ctx.appGraph.getFavorites }
        var favorites by remember { mutableStateOf<List<Manga>>(emptyList()) }
        LaunchedEffect(Unit) {
            favorites = getFavorites.await()
        }
        val msgExported = stringResource(MR.strings.library_exported)

        val saveFileLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/csv"),
        ) { uri ->
            uri?.let {
                scope.launch {
                    LibraryExporter.exportToCsv(
                        context = ctx,
                        uri = it,
                        favorites = favorites,
                        options = exportOptions,
                        onExportComplete = {
                            scope.launch(Dispatchers.Main) {
                                ctx.toast(msgExported)
                            }
                        },
                    )
                }
            }
        }

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.export)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.export))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = stringResource(MR.strings.library_list),
                        onClick = { showDialog = true },
                    )
                }
            }

            if (showDialog) {
                ColumnSelectionDialog(
                    options = exportOptions,
                    onConfirm = { options ->
                        exportOptions = options
                        saveFileLauncher.launch("mihon_library.csv")
                    },
                    onDismissRequest = { showDialog = false },
                )
            }
        }
    }

    @Composable
    private fun ColumnSelectionDialog(
        options: ExportOptions,
        onConfirm: (ExportOptions) -> Unit,
        onDismissRequest: () -> Unit,
    ) {
        var titleSelected by remember { mutableStateOf(options.includeTitle) }
        var authorSelected by remember { mutableStateOf(options.includeAuthor) }
        var artistSelected by remember { mutableStateOf(options.includeArtist) }

        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = stringResource(MR.strings.migration_dialog_what_to_include)) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = titleSelected,
                            onCheckedChange = { checked ->
                                titleSelected = checked
                                if (!checked) {
                                    authorSelected = false
                                    artistSelected = false
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                        )
                        Text(text = stringResource(MR.strings.title))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = authorSelected,
                            onCheckedChange = { authorSelected = it },
                            enabled = titleSelected,
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                        )
                        Text(text = stringResource(MR.strings.author))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = artistSelected,
                            onCheckedChange = { artistSelected = it },
                            enabled = titleSelected,
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                        )
                        Text(text = stringResource(MR.strings.artist))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm(
                            ExportOptions(
                                includeTitle = titleSelected,
                                includeAuthor = authorSelected,
                                includeArtist = artistSelected,
                            ),
                        )
                        onDismissRequest()
                    },
                ) {
                    Text(text = stringResource(MR.strings.action_save), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(MR.strings.action_cancel), color = MaterialTheme.colorScheme.error)
                }
            },
        )
    }

    @Composable
    private fun DestructiveTextPreference(
        title: String,
        subtitle: String? = null,
        onClick: () -> Unit,
    ) {
        val isWide = LocalContext.current.resources.configuration.screenWidthDp > 500
        Row(
            modifier = Modifier
                .fillMaxWidth(if (isWide) 0.5f else 1f)
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}
