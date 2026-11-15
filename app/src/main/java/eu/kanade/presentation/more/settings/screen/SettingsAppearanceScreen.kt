package eu.kanade.presentation.more.settings.screen

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.domain.ui.model.TabletUiMode
import eu.kanade.domain.ui.model.ThemeMode
import eu.kanade.domain.ui.model.TimeFormat
import eu.kanade.domain.ui.model.setAppCompatDelegateThemeMode
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.more.settings.screen.appearance.AppLanguageScreen
import eu.kanade.presentation.more.settings.widget.AppThemeModePreferenceWidget
import eu.kanade.presentation.more.settings.widget.AppThemePreferenceWidget
import eu.kanade.presentation.util.rememberIs24HourSystem
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import mihon.app.di.appGraph
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import kotlin.time.Clock

object SettingsAppearanceScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_appearance

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val uiPreferences = remember { context.appGraph.uiPreferences }

        return listOf(
            getThemeGroup(uiPreferences),
            getDisplayGroup(uiPreferences),
        )
    }

    @Composable
    private fun getThemeGroup(uiPreferences: UiPreferences): Preference {
        val context = LocalContext.current

        val themeModePref = uiPreferences.themeMode
        val themeMode by themeModePref.collectAsState()

        val appThemePref = uiPreferences.appTheme
        val appTheme by appThemePref.collectAsState()

        val amoledPref = uiPreferences.themeDarkAmoled
        val amoled by amoledPref.collectAsState()

        return Preference.PreferenceItem.CustomPreference(
            title = stringResource(MR.strings.pref_category_theme),
        ) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.pref_category_theme))
                VmhSettingsCard {
                    AppThemeModePreferenceWidget(
                        value = themeMode,
                        onItemClick = {
                            themeModePref.set(it)
                            setAppCompatDelegateThemeMode(it)
                        },
                    )

                    AppThemePreferenceWidget(
                        value = appTheme,
                        amoled = amoled,
                        onItemClick = { appThemePref.set(it) },
                    )

                    if (themeMode != ThemeMode.LIGHT) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        )
                        VmhSwitchPreference(
                            title = stringResource(MR.strings.pref_dark_theme_pure_black),
                            checked = amoled,
                            onCheckedChange = {
                                amoledPref.set(it)
                                (context as? Activity)?.let { act -> ActivityCompat.recreate(act) }
                            },
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun getDisplayGroup(uiPreferences: UiPreferences): Preference {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val locale = LocalLocale.current
        val is24HourSystem = rememberIs24HourSystem()

        val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime() }

        val dateFormatPref = uiPreferences.dateFormat
        val dateFormat by dateFormatPref.collectAsState()
        val formattedDateNow = remember(dateFormat) {
            UiPreferences.dateFormat(dateFormat, locale.platformLocale).format(now)
        }

        val timeFormatPref = uiPreferences.timeFormat
        val timeFormat by timeFormatPref.collectAsState()

        val tabletUiModePref = uiPreferences.tabletUiMode
        val tabletUiMode by tabletUiModePref.collectAsState()

        val relativeTimePref = uiPreferences.relativeTime
        val relativeTime by relativeTimePref.collectAsState()

        val imagesPref = uiPreferences.imagesInDescription
        val images by imagesPref.collectAsState()

        var showTabletModeDialog by remember { mutableStateOf(false) }
        var showDateFormatDialog by remember { mutableStateOf(false) }
        var showTimeFormatDialog by remember { mutableStateOf(false) }

        return Preference.PreferenceItem.CustomPreference(
            title = stringResource(MR.strings.pref_category_display),
        ) {
            Column {
                VmhSectionHeader(stringResource(MR.strings.pref_category_display))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_app_language),
                        onClick = { navigator.push(AppLanguageScreen()) },
                    )

                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_tablet_ui_mode),
                        subtitle = stringResource(tabletUiMode.titleRes),
                        onClick = { showTabletModeDialog = true },
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )

                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_date_format),
                        subtitle = if (dateFormat.isEmpty()) {
                            "${stringResource(MR.strings.label_default)} ($formattedDateNow)"
                        } else {
                            "$dateFormat ($formattedDateNow)"
                        },
                        onClick = { showDateFormatDialog = true },
                    )

                    VmhTextPreference(
                        title = stringResource(MR.strings.pref_time_format),
                        subtitle = "${stringResource(
                            timeFormat.titleRes,
                        )} (${UiPreferences.timeFormat(
                            timeFormat,
                            locale.platformLocale,
                            is24HourSystem,
                        ).format(now)})",
                        onClick = { showTimeFormatDialog = true },
                    )

                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_relative_format),
                        subtitle = stringResource(
                            MR.strings.pref_relative_format_summary,
                            stringResource(MR.strings.relative_time_today),
                            formattedDateNow,
                        ),
                        checked = relativeTime,
                        onCheckedChange = { relativeTimePref.set(it) },
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )

                    VmhSwitchPreference(
                        title = stringResource(MR.strings.pref_display_images_description),
                        checked = images,
                        onCheckedChange = { imagesPref.set(it) },
                    )
                }

                if (showTabletModeDialog) {
                    VmhListDialog(
                        title = stringResource(MR.strings.pref_tablet_ui_mode),
                        entries = TabletUiMode.entries.associateWith { stringResource(it.titleRes) },
                        selectedValue = tabletUiMode,
                        onSelect = {
                            tabletUiModePref.set(it)
                            context.toast(MR.strings.requires_app_restart)
                        },
                        onDismiss = { showTabletModeDialog = false },
                        warning = stringResource(MR.strings.requires_app_restart),
                    )
                }

                if (showDateFormatDialog) {
                    VmhListDialog(
                        title = stringResource(MR.strings.pref_date_format),
                        entries = DateFormats.associateWith {
                            val fd = UiPreferences.dateFormat(it, locale.platformLocale).format(now)
                            "${it.ifEmpty { stringResource(MR.strings.label_default) }} ($fd)"
                        },
                        selectedValue = dateFormat,
                        onSelect = { dateFormatPref.set(it) },
                        onDismiss = { showDateFormatDialog = false },
                    )
                }

                if (showTimeFormatDialog) {
                    VmhListDialog(
                        title = stringResource(MR.strings.pref_time_format),
                        entries = TimeFormat.entries.associateWith {
                            val ft = UiPreferences.timeFormat(it, locale.platformLocale, is24HourSystem).format(now)
                            "${stringResource(it.titleRes)} ($ft)"
                        },
                        selectedValue = timeFormat,
                        onSelect = { timeFormatPref.set(it) },
                        onDismiss = { showTimeFormatDialog = false },
                    )
                }
            }
        }
    }
}

private val DateFormats = listOf(
    "", // Default
    "MM/dd/yy",
    "dd/MM/yy",
    "yyyy-MM-dd",
    "dd MMM yyyy",
    "MMM dd, yyyy",
)
