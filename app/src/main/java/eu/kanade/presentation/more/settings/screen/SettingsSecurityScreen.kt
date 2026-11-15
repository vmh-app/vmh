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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.tachiyomi.core.security.PrivacyPreferences
import eu.kanade.tachiyomi.core.security.SecurityPreferences
import eu.kanade.tachiyomi.util.system.AuthenticatorUtil.authenticate
import eu.kanade.tachiyomi.util.system.AuthenticatorUtil.isAuthenticationSupported
import eu.kanade.tachiyomi.util.system.telemetryIncluded
import kotlinx.coroutines.launch
import mihon.app.di.appGraph
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

private val LockAfterValues = listOf(0, 1, 2, 5, 10, -1)

object SettingsSecurityScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_security

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val securityPreferences = remember { context.appGraph.securityPreferences }
        val privacyPreferences = remember { context.appGraph.privacyPreferences }

        return buildList {
            add(getAppLockGroup(securityPreferences))
            add(getSecurityGroup(securityPreferences))
            if (telemetryIncluded) {
                add(getFirebaseGroup(privacyPreferences))
            }
        }
    }

    @Composable
    private fun getAppLockGroup(securityPreferences: SecurityPreferences): Preference {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val authSupported = remember { context.isAuthenticationSupported() }
        val useAuth by securityPreferences.useAuthenticator.collectAsState()
        val lockAppAfter by securityPreferences.lockAppAfter.collectAsState()

        var listDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }
        val titleAuth = context.stringResource(MR.strings.lock_with_biometrics)
        val titleLockIdle = context.stringResource(MR.strings.lock_when_idle)

        val lockOptions = LockAfterValues.associateWith {
            when (it) {
                -1 -> stringResource(MR.strings.lock_never)
                0 -> stringResource(MR.strings.lock_always)
                else -> pluralStringResource(MR.plurals.lock_after_mins, count = it, it)
            }
        }

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.lock_with_biometrics)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.lock_with_biometrics))
                VmhSettingsCard {
                    if (authSupported) {
                        VmhSwitchPreference(
                            title = stringResource(MR.strings.lock_with_biometrics),
                            checked = useAuth,
                            onCheckedChange = {
                                scope.launch { (context as FragmentActivity).authenticate(title = titleAuth) }
                                securityPreferences.useAuthenticator.set(it)
                            },
                        )
                        if (useAuth) {
                            VmhTextPreference(
                                title = titleLockIdle,
                                subtitle = lockOptions[lockAppAfter] ?: "",
                                onClick = {
                                    listDialog = ListDialogData(
                                        title = titleLockIdle,
                                        entries = lockOptions as Map<Any, String>,
                                        selectedValue = lockAppAfter as Any,
                                        onSelect = {
                                            scope.launch {
                                                (context as FragmentActivity).authenticate(title = titleLockIdle)
                                            }
                                            securityPreferences.lockAppAfter.set(it as Int)
                                        },
                                    )
                                },
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(MR.strings.disabled),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
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
    private fun getSecurityGroup(securityPreferences: SecurityPreferences): Preference {
        val hideNotificationContent by securityPreferences.hideNotificationContent.collectAsState()
        val secureScreen by securityPreferences.secureScreen.collectAsState()
        var listDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }
        val titleSecureScreen = stringResource(MR.strings.secure_screen)

        val secureScreenOptions = SecurityPreferences.SecureScreenMode.entries
            .associateWith { stringResource(it.titleRes) }

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.pref_security)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.pref_security))
                VmhSettingsCard {
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.hide_notification_content),
                        checked = hideNotificationContent,
                        onCheckedChange = { securityPreferences.hideNotificationContent.set(it) },
                    )
                    VmhTextPreference(
                        title = titleSecureScreen,
                        subtitle = secureScreenOptions[secureScreen] ?: "",
                        onClick = {
                            listDialog = ListDialogData(
                                title = titleSecureScreen,
                                entries = secureScreenOptions as Map<Any, String>,
                                selectedValue = secureScreen as Any,
                                onSelect = {
                                    securityPreferences.secureScreen.set(it as SecurityPreferences.SecureScreenMode)
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
                        Text(
                            text = stringResource(MR.strings.secure_screen_summary),
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
    private fun getFirebaseGroup(privacyPreferences: PrivacyPreferences): Preference {
        val crashlytics by privacyPreferences.crashlytics.collectAsState()
        val analytics by privacyPreferences.analytics.collectAsState()

        return Preference.PreferenceItem.CustomPreference(title = stringResource(MR.strings.pref_firebase)) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.pref_firebase))
                VmhSettingsCard {
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.onboarding_permission_crashlytics),
                        subtitle = stringResource(MR.strings.onboarding_permission_crashlytics_description),
                        checked = crashlytics,
                        onCheckedChange = { privacyPreferences.crashlytics.set(it) },
                    )
                    VmhSwitchPreference(
                        title = stringResource(MR.strings.onboarding_permission_analytics),
                        subtitle = stringResource(MR.strings.onboarding_permission_analytics_description),
                        checked = analytics,
                        onCheckedChange = { privacyPreferences.analytics.set(it) },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 14.dp),
                    ) {
                        Text(
                            text = stringResource(MR.strings.firebase_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
