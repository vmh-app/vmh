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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.more.settings.screen.browse.ExtensionStoresScreen
import eu.kanade.tachiyomi.util.system.AuthenticatorUtil.authenticate
import kotlinx.coroutines.launch
import mihon.app.di.appGraph
import mihon.domain.extension.model.ContentWarning
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

object SettingsBrowseScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.browse

    @Composable
    override fun getPreferences(): List<Preference> {
        return listOf(
            getSourcesGroup(),
            getExtensionsGroup(),
        )
    }

    @Composable
    private fun getSourcesGroup(): Preference {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val sourcePreferences = remember { context.appGraph.sourcePreferences }
        val getExtensionStoreCountAsFlow = remember { context.appGraph.getExtensionStoreCountAsFlow }

        val hideInLibraryItems by sourcePreferences.hideInLibraryItems.collectAsState()
        val reposCount by getExtensionStoreCountAsFlow().collectAsState(0)
        val subtitleRepos = pluralStringResource(MR.plurals.num_repos, reposCount.toInt(), reposCount)

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.label_sources)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.label_sources))
                VmhSettingsCard {
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_hide_in_library_items),
                        checked = hideInLibraryItems,
                        onCheckedChange = { sourcePreferences.hideInLibraryItems.set(it) },
                    )
                    VmhTextPreference(
                        title = stringResource(MR.strings.extensionStores),
                        subtitle = subtitleRepos,
                        onClick = { navigator.push(ExtensionStoresScreen()) },
                    )
                }
            }
        }
    }

    @Composable
    private fun getExtensionsGroup(): Preference {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val sourcePreferences = remember { context.appGraph.sourcePreferences }

        val enabledWarnings by sourcePreferences.enabledContentWarnings.collectAsState()
        val applyContentWarningsToInstalled by sourcePreferences.applyContentWarningsToInstalled.collectAsState()

        var showWarningsDialog by remember { mutableStateOf<MultiListDialogData<Any>?>(null) }

        val titleWarnings = stringResource(MR.strings.pref_allowed_content_warnings)
        val authTitleWarnings = context.stringResource(MR.strings.pref_allowed_content_warnings)
        val authTitleApply = context.stringResource(MR.strings.pref_apply_content_warnings_to_installed)

        val warningOptions = mapOf(
            ContentWarning.SAFE to stringResource(MR.strings.ext_content_warning_safe),
            ContentWarning.MIXED to stringResource(MR.strings.ext_content_warning_mixed),
            ContentWarning.NSFW to stringResource(MR.strings.ext_content_warning_nsfw),
        )

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.pref_category_extensions)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.pref_category_extensions))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = titleWarnings,
                        subtitle = warningOptions.filterKeys {
                            it in enabledWarnings
                        }.values.joinToString().ifEmpty { stringResource(MR.strings.none) },
                        onClick = {
                            showWarningsDialog = MultiListDialogData(
                                title = titleWarnings,
                                entries = warningOptions as Map<Any, String>,
                                selectedValues = enabledWarnings as Set<Any>,
                                onSelect = { newSet ->
                                    val typedSet = newSet as Set<ContentWarning>
                                    val added = typedSet - sourcePreferences.enabledContentWarnings.get()
                                    if (added.any { it != ContentWarning.SAFE }) {
                                        scope.launch {
                                            (context as FragmentActivity).authenticate(title = authTitleWarnings)
                                        }
                                    }
                                    sourcePreferences.enabledContentWarnings.set(typedSet)
                                },
                            )
                        },
                    )
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_apply_content_warnings_to_installed),
                        subtitle = stringResource(MR.strings.pref_apply_content_warnings_to_installed_summary),
                        checked = applyContentWarningsToInstalled,
                        onCheckedChange = { newValue ->
                            if (!newValue) {
                                scope.launch { (context as FragmentActivity).authenticate(title = authTitleApply) }
                            }
                            sourcePreferences.applyContentWarningsToInstalled.set(newValue)
                        },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 14.dp),
                    ) {
                        Text(
                            text = stringResource(MR.strings.content_warnings_info),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                showWarningsDialog?.let { data ->
                    VmhMultiSelectDialog(
                        title = data.title,
                        entries = data.entries,
                        selectedValues = data.selectedValues,
                        onSelect = {
                            data.onSelect(it)
                            showWarningsDialog = null
                        },
                        onDismiss = { showWarningsDialog = null },
                    )
                }
            }
        }
    }
}
