package eu.kanade.presentation.more.settings.screen

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.base.BasePreferences
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.more.settings.screen.advanced.ClearDatabaseScreen
import eu.kanade.presentation.more.settings.screen.debug.DebugInfoScreen
import eu.kanade.tachiyomi.data.library.MetadataUpdateWorker
import eu.kanade.tachiyomi.network.NetworkPreferences
import eu.kanade.tachiyomi.network.PREF_DOH_360
import eu.kanade.tachiyomi.network.PREF_DOH_ADGUARD
import eu.kanade.tachiyomi.network.PREF_DOH_ALIDNS
import eu.kanade.tachiyomi.network.PREF_DOH_CLOUDFLARE
import eu.kanade.tachiyomi.network.PREF_DOH_CONTROLD
import eu.kanade.tachiyomi.network.PREF_DOH_DNSPOD
import eu.kanade.tachiyomi.network.PREF_DOH_GOOGLE
import eu.kanade.tachiyomi.network.PREF_DOH_MULLVAD
import eu.kanade.tachiyomi.network.PREF_DOH_NJALLA
import eu.kanade.tachiyomi.network.PREF_DOH_QUAD101
import eu.kanade.tachiyomi.network.PREF_DOH_QUAD9
import eu.kanade.tachiyomi.network.PREF_DOH_SHECAN
import eu.kanade.tachiyomi.ui.more.OnboardingScreen
import eu.kanade.tachiyomi.util.system.isReleaseBuildType
import eu.kanade.tachiyomi.util.system.isShizukuInstalled
import eu.kanade.tachiyomi.util.system.powerManager
import eu.kanade.tachiyomi.util.system.setDefaultSettings
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.workManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import mihon.app.di.appGraph
import okhttp3.Headers
import tachiyomi.core.common.i18n.pluralStringResource
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import java.io.File

object SettingsAdvancedScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_advanced

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val graph = remember { context.appGraph }
        val basePreferences = remember { graph.basePreferences }
        val networkPreferences = remember { graph.networkPreferences }
        val libraryPreferences = remember { graph.libraryPreferences }

        return listOf(
            getGeneralGroup(),
            getBackgroundActivityGroup(),
            getDataGroup(),
            getNetworkGroup(networkPreferences),
            getLibraryGroup(libraryPreferences),
            getReaderGroup(basePreferences),
            getExtensionsGroup(basePreferences),
        )
    }

    @Composable
    private fun getGeneralGroup(): Preference {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val graph = remember { context.appGraph }
        val crashLogUtil = remember { graph.crashLogUtil }
        val networkPreferences = remember { graph.networkPreferences }

        val verboseLogging by networkPreferences.verboseLogging.collectAsState()
        val redactSensitiveHeaders by networkPreferences.redactSensitiveHeaders.collectAsState()

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.pref_category_advanced)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.pref_category_advanced))
                VmhSettingsCard {
                    DestructiveTextPreference(
                        title = stringResource(MR.strings.pref_dump_crash_logs),
                        subtitle = stringResource(MR.strings.pref_dump_crash_logs_summary),
                        onClick = { scope.launch { crashLogUtil.dumpLogs() } },
                    )
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_verbose_logging),
                        subtitle = stringResource(MR.strings.pref_verbose_logging_summary),
                        checked = verboseLogging,
                        onCheckedChange = {
                            networkPreferences.verboseLogging.set(it)
                            context.toast(MR.strings.requires_app_restart)
                        },
                    )
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_redact_sensitive_headers),
                        subtitle = stringResource(MR.strings.pref_redact_sensitive_headers_summary),
                        checked = redactSensitiveHeaders,
                        onCheckedChange = {
                            networkPreferences.redactSensitiveHeaders.set(it)
                            context.toast(MR.strings.requires_app_restart)
                        },
                    )
                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_debug_info),
                        onClick = { navigator.push(DebugInfoScreen()) },
                    )
                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_onboarding_guide),
                        onClick = { navigator.push(OnboardingScreen()) },
                    )
                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_manage_notifications),
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun getBackgroundActivityGroup(): Preference {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current

        val msgNotFound = context.stringResource(MR.strings.battery_optimization_setting_activity_not_found)
        val msgDisabled = context.stringResource(MR.strings.battery_optimization_disabled)

        return Preference.PreferenceItem.CustomPreference(
            title = stringResource(MR.strings.label_background_activity),
        ) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.label_background_activity))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_disable_battery_optimization),
                        subtitle = stringResource(MR.strings.pref_disable_battery_optimization_summary),
                        onClick = {
                            val packageName = context.packageName
                            if (!context.powerManager.isIgnoringBatteryOptimizations(packageName)) {
                                try {
                                    @SuppressLint("BatteryLife")
                                    val intent = Intent().apply {
                                        action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                        data = "package:$packageName".toUri()
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    context.toast(msgNotFound)
                                }
                            } else {
                                context.toast(msgDisabled)
                            }
                        },
                    )
                    VmhTextPreference(
                        title = "Don't kill my app!",
                        subtitle = stringResource(MR.strings.about_dont_kill_my_app),
                        onClick = { uriHandler.openUri("https://dontkillmyapp.com/") },
                    )
                }
            }
        }
    }

    @Composable
    private fun getDataGroup(): Preference {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val msgInvalidated = context.stringResource(MR.strings.download_cache_invalidated)

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.label_data)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.label_data))
                VmhSettingsCard {
                    DestructiveTextPreference(
                        title = stringResource(MR.strings.pref_invalidate_download_cache),
                        subtitle = stringResource(MR.strings.pref_invalidate_download_cache_summary),
                        onClick = {
                            context.appGraph.downloadCache.invalidateCache()
                            context.toast(msgInvalidated)
                        },
                    )
                    DestructiveTextPreference(
                        title = stringResource(MR.strings.pref_clear_database),
                        subtitle = stringResource(MR.strings.pref_clear_database_summary),
                        onClick = { navigator.push(ClearDatabaseScreen()) },
                    )
                }
            }
        }
    }

    @Composable
    private fun getNetworkGroup(networkPreferences: NetworkPreferences): Preference {
        val context = LocalContext.current
        val networkHelper = remember { context.appGraph.networkHelper }
        val scope = rememberCoroutineScope()

        val dohProvider by networkPreferences.dohProvider.collectAsState()
        val userAgent by networkPreferences.defaultUserAgent.collectAsState()

        val msgCookiesCleared = context.stringResource(MR.strings.cookies_cleared)
        val msgWebviewDeleted = context.stringResource(MR.strings.webview_data_deleted)
        val msgDeleteError = context.stringResource(MR.strings.cache_delete_error)
        val msgInvalidAgent = context.stringResource(MR.strings.error_user_agent_string_invalid)

        var showDohDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }
        var showUserAgentDialog by remember { mutableStateOf(false) }

        val dohOptions = mapOf(
            -1 to stringResource(MR.strings.disabled),
            PREF_DOH_CLOUDFLARE to "Cloudflare",
            PREF_DOH_GOOGLE to "Google",
            PREF_DOH_ADGUARD to "AdGuard",
            PREF_DOH_QUAD9 to "Quad9",
            PREF_DOH_ALIDNS to "AliDNS",
            PREF_DOH_DNSPOD to "DNSPod",
            PREF_DOH_360 to "360",
            PREF_DOH_QUAD101 to "Quad 101",
            PREF_DOH_MULLVAD to "Mullvad",
            PREF_DOH_CONTROLD to "Control D",
            PREF_DOH_NJALLA to "Njalla",
            PREF_DOH_SHECAN to "Shecan",
        )

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.label_network)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.label_network))
                VmhSettingsCard {
                    DestructiveTextPreference(
                        title = stringResource(MR.strings.pref_clear_cookies),
                        onClick = {
                            networkHelper.cookieJar.removeAll()
                            context.toast(msgCookiesCleared)
                        },
                    )
                    DestructiveTextPreference(
                        title = stringResource(MR.strings.pref_clear_webview_data),
                        onClick = {
                            try {
                                WebView(context).run {
                                    setDefaultSettings()
                                    clearCache(true)
                                    clearFormData()
                                    clearHistory()
                                    clearSslPreferences()
                                }
                                WebStorage.getInstance().deleteAllData()
                                scope.launch(Dispatchers.IO) {
                                    val dataDir = context.applicationInfo?.dataDir
                                    dataDir?.let { File("$it/app_webview/").deleteRecursively() }
                                    withContext(Dispatchers.Main) { context.toast(msgWebviewDeleted) }
                                }
                            } catch (e: Throwable) {
                                logcat(LogPriority.ERROR, e)
                                context.toast(msgDeleteError)
                            }
                        },
                    )
                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_dns_over_https),
                        subtitle = dohOptions[dohProvider] ?: "",
                        onClick = {
                            showDohDialog = ListDialogData(
                                title = context.getString(MR.strings.pref_dns_over_https.resourceId),
                                entries = dohOptions as Map<Any, String>,
                                selectedValue = dohProvider as Any,
                                onSelect = {
                                    networkPreferences.dohProvider.set(it as Int)
                                    context.toast(MR.strings.requires_app_restart)
                                },
                            )
                        },
                    )
                    VmhMonoTextPreference(
                        title = stringResource(MR.strings.pref_user_agent_string),
                        subtitle = userAgent,
                        onClick = { showUserAgentDialog = true },
                    )
                    if (userAgent != networkPreferences.defaultUserAgent.defaultValue()) {
                        DestructiveTextPreference(
                            title = stringResource(MR.strings.pref_reset_user_agent_string),
                            onClick = {
                                networkPreferences.defaultUserAgent.delete()
                                context.toast(MR.strings.requires_app_restart)
                            },
                        )
                    }
                }

                showDohDialog?.let { data ->
                    VmhListDialog(
                        title = data.title,
                        entries = data.entries,
                        selectedValue = data.selectedValue,
                        onSelect = {
                            data.onSelect(it)
                            showDohDialog = null
                        },
                        onDismiss = { showDohDialog = null },
                    )
                }

                if (showUserAgentDialog) {
                    VmhEditTextDialog(
                        title = stringResource(MR.strings.pref_user_agent_string),
                        initialValue = userAgent,
                        onConfirm = {
                            try {
                                Headers.Builder().add("User-Agent", it)
                                networkPreferences.defaultUserAgent.set(it)
                                context.toast(MR.strings.requires_app_restart)
                                showUserAgentDialog = false
                            } catch (_: IllegalArgumentException) {
                                context.toast(msgInvalidAgent)
                            }
                        },
                        onDismiss = { showUserAgentDialog = false },
                    )
                }
            }
        }
    }

    @Composable
    private fun getLibraryGroup(libraryPreferences: LibraryPreferences): Preference {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val updateMangaTitles by libraryPreferences.updateMangaTitles.collectAsState()
        val disallowNonAsciiFilenames by libraryPreferences.disallowNonAsciiFilenames.collectAsState()
        val enableChapterNameHash by libraryPreferences.enableChapterNameHash.collectAsState()

        val msgSuccess = context.stringResource(MR.strings.pref_reset_viewer_flags_success)
        val msgError = context.stringResource(MR.strings.pref_reset_viewer_flags_error)

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.label_library)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.label_library))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_refresh_library_covers),
                        onClick = { scope.launch { MetadataUpdateWorker.startNow(context.workManager) } },
                    )
                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_reset_viewer_flags),
                        subtitle = stringResource(MR.strings.pref_reset_viewer_flags_summary),
                        onClick = {
                            scope.launch {
                                val success = context.appGraph.resetViewerFlags.await()
                                context.toast(if (success) msgSuccess else msgError)
                            }
                        },
                    )
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_update_library_manga_titles),
                        subtitle = stringResource(MR.strings.pref_update_library_manga_titles_summary),
                        checked = updateMangaTitles,
                        onCheckedChange = { libraryPreferences.updateMangaTitles.set(it) },
                    )
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_disallow_non_ascii_filenames),
                        subtitle = stringResource(MR.strings.pref_disallow_non_ascii_filenames_details),
                        checked = disallowNonAsciiFilenames,
                        onCheckedChange = { libraryPreferences.disallowNonAsciiFilenames.set(it) },
                    )
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_enable_chapter_name_hash),
                        subtitle = stringResource(MR.strings.pref_enable_chapter_name_hash_details),
                        checked = enableChapterNameHash,
                        onCheckedChange = { libraryPreferences.enableChapterNameHash.set(it) },
                    )
                }
            }
        }
    }

    @Composable
    private fun getReaderGroup(basePreferences: BasePreferences): Preference {
        val highQualityRenderer by basePreferences.highQualityRenderer.collectAsState()

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.pref_category_reader)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.pref_category_reader))
                VmhSettingsCard {
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_high_quality_renderer),
                        checked = highQualityRenderer,
                        onCheckedChange = { basePreferences.highQualityRenderer.set(it) },
                    )
                }
            }
        }
    }

    @Composable
    private fun getExtensionsGroup(basePreferences: BasePreferences): Preference {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        var shizukuMissing by rememberSaveable { mutableStateOf(false) }
        var listDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }

        val trustExtension = remember { context.appGraph.trustExtension }
        val extensionInstaller by basePreferences.extensionInstaller.collectAsState()

        val installerOptions = basePreferences.extensionInstaller.entries
            .filter { if (isReleaseBuildType) it != BasePreferences.ExtensionInstaller.PRIVATE else true }
            .associateWith { stringResource(it.titleRes) }

        val titleInstaller = stringResource(MR.strings.ext_installer_pref)

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.label_extensions)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.label_extensions))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = titleInstaller,
                        subtitle = installerOptions[extensionInstaller] ?: "",
                        onClick = {
                            listDialog = ListDialogData(
                                title = titleInstaller,
                                entries = installerOptions as Map<Any, String>,
                                selectedValue = extensionInstaller as Any,
                                onSelect = {
                                    val installer = it as BasePreferences.ExtensionInstaller
                                    if (installer == BasePreferences.ExtensionInstaller.SHIZUKU &&
                                        !context.isShizukuInstalled
                                    ) {
                                        shizukuMissing = true
                                    } else {
                                        basePreferences.extensionInstaller.set(installer)
                                    }
                                },
                            )
                        },
                    )
                    DestructiveTextPreference(
                        title = stringResource(MR.strings.ext_revoke_trust),
                        onClick = { trustExtension.revokeAll() },
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

                if (shizukuMissing) {
                    val dismiss = { shizukuMissing = false }
                    AlertDialog(
                        onDismissRequest = dismiss,
                        title = { Text(text = stringResource(MR.strings.ext_installer_shizuku)) },
                        text = { Text(text = stringResource(MR.strings.ext_installer_shizuku_unavailable_dialog)) },
                        dismissButton = {
                            TextButton(onClick = dismiss) { Text(text = stringResource(MR.strings.action_cancel)) }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    dismiss()
                                    uriHandler.openUri("https://shizuku.rikka.app/download")
                                },
                            ) { Text(text = stringResource(MR.strings.action_ok)) }
                        },
                    )
                }
            }
        }
    }

    // --- Componentes adicionales locales de SettingsAdvancedScreen ---

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

    @Composable
    private fun VmhMonoTextPreference(
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
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }

    @Composable
    private fun VmhEditTextDialog(
        title: String,
        initialValue: String,
        onConfirm: (String) -> Unit,
        onDismiss: () -> Unit,
    ) {
        var text by remember { mutableStateOf(initialValue) }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(text) }) {
                    Text(stringResource(MR.strings.action_save), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(MR.strings.action_cancel), color = MaterialTheme.colorScheme.error)
                }
            },
        )
    }
}
