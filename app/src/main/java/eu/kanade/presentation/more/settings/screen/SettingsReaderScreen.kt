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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import eu.kanade.tachiyomi.util.system.hasDisplayCutout
import mihon.app.di.appGraph
import tachiyomi.core.common.i18n.pluralStringResource
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import java.text.NumberFormat

val LocalIsWide = staticCompositionLocalOf { false }

object SettingsReaderScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_reader

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val readerPref = remember { context.appGraph.readerPreferences }

        return listOf(
            getDailyGroup(readerPref),
            getNavigationGroup(readerPref),
            getPagedGroup(readerPref),
            getWebtoonGroup(readerPref),
            getAdvancedGroup(readerPref),
        )
    }

    @Composable
    private fun getDailyGroup(readerPref: ReaderPreferences): Preference {
        val context = LocalContext.current
        val defaultReadingMode by readerPref.defaultReadingMode.collectAsState()
        val defaultOrientationType by readerPref.defaultOrientationType.collectAsState()
        val readerTheme by readerPref.readerTheme.collectAsState()
        val fullscreen by readerPref.fullscreen.collectAsState()
        val drawUnderCutout by readerPref.drawUnderCutout.collectAsState()
        val keepScreenOn by readerPref.keepScreenOn.collectAsState()
        val showPageNumber by readerPref.showPageNumber.collectAsState()
        val hasCutout = LocalView.current.hasDisplayCutout()

        return Preference.PreferenceItem.CustomPreference(
            title = context.stringResource(MR.strings.pref_category_display),
        ) {
            var listDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }

            fun <T> openDialog(title: String, entries: Map<T, String>, selected: T, onSelect: (T) -> Unit) {
                listDialog = ListDialogData(title, entries as Map<Any, String>, selected as Any) { onSelect(it as T) }
            }

            Column {
                VmhSectionHeader(context.stringResource(MR.strings.pref_category_display))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_viewer_type),
                        subtitle =
                        ReadingMode.entries.drop(1).find {
                            it.flagValue == defaultReadingMode
                        }?.stringRes?.let { context.stringResource(it) }
                            ?: "",
                        onClick = {
                            openDialog(
                                title = context.stringResource(MR.strings.pref_viewer_type),
                                entries = ReadingMode.entries.drop(1).associate {
                                    it.flagValue to
                                        context.stringResource(it.stringRes)
                                },
                                selected = defaultReadingMode,
                                onSelect = { readerPref.defaultReadingMode.set(it) },
                            )
                        },
                    )
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_rotation_type),
                        subtitle =
                        ReaderOrientation.entries.drop(1).find {
                            it.flagValue == defaultOrientationType
                        }?.stringRes?.let { context.stringResource(it) }
                            ?: "",
                        onClick = {
                            openDialog(
                                title = context.stringResource(MR.strings.pref_rotation_type),
                                entries = ReaderOrientation.entries.drop(1).associate {
                                    it.flagValue to
                                        context.stringResource(it.stringRes)
                                },
                                selected = defaultOrientationType,
                                onSelect = { readerPref.defaultOrientationType.set(it) },
                            )
                        },
                    )
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_reader_theme),
                        subtitle = mapOf(
                            1 to context.stringResource(MR.strings.black_background),
                            2 to context.stringResource(MR.strings.gray_background),
                            0 to context.stringResource(MR.strings.white_background),
                            3 to context.stringResource(MR.strings.automatic_background),
                        )[readerTheme] ?: "",
                        onClick = {
                            openDialog(
                                title = context.stringResource(MR.strings.pref_reader_theme),
                                entries = mapOf(
                                    1 to context.stringResource(MR.strings.black_background),
                                    2 to context.stringResource(MR.strings.gray_background),
                                    0 to context.stringResource(MR.strings.white_background),
                                    3 to context.stringResource(MR.strings.automatic_background),
                                ),
                                selected = readerTheme,
                                onSelect = { readerPref.readerTheme.set(it) },
                            )
                        },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_fullscreen),
                        checked = fullscreen,
                        onCheckedChange = { readerPref.fullscreen.set(it) },
                    )
                    if (hasCutout && fullscreen) {
                        VmhSwitchPreference(
                            title = context.stringResource(MR.strings.pref_cutout_short),
                            checked = drawUnderCutout,
                            onCheckedChange = { readerPref.drawUnderCutout.set(it) },
                        )
                    }
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_keep_screen_on),
                        checked = keepScreenOn,
                        onCheckedChange = { readerPref.keepScreenOn.set(it) },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_show_page_number),
                        checked = showPageNumber,
                        onCheckedChange = { readerPref.showPageNumber.set(it) },
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
    private fun getNavigationGroup(readerPref: ReaderPreferences): Preference {
        val context = LocalContext.current
        val readWithVolumeKeys by readerPref.readWithVolumeKeys.collectAsState()
        val readWithVolumeKeysInverted by readerPref.readWithVolumeKeysInverted.collectAsState()
        val verticalNavigator by readerPref.verticalNavigator.collectAsState()
        val verticalNavigatorOnLeft by readerPref.verticalNavigatorOnLeft.collectAsState()
        val verticalNavigatorHeight by readerPref.verticalNavigatorHeight.collectAsState()
        val showNavigationOverlayOnStart by readerPref.showNavigationOverlayOnStart.collectAsState()
        val showReadingMode by readerPref.showReadingMode.collectAsState()

        return Preference.PreferenceItem.CustomPreference(
            title = context.stringResource(MR.strings.pref_reader_navigation),
        ) {
            var multiDialog by remember { mutableStateOf<MultiListDialogData<Any>?>(null) }

            Column {
                VmhSectionHeader(context.stringResource(MR.strings.pref_reader_navigation))
                VmhSettingsCard {
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_read_with_volume_keys),
                        checked = readWithVolumeKeys,
                        onCheckedChange = { readerPref.readWithVolumeKeys.set(it) },
                    )
                    if (readWithVolumeKeys) {
                        VmhSwitchPreference(
                            title = context.stringResource(MR.strings.pref_read_with_volume_keys_inverted),
                            checked = readWithVolumeKeysInverted,
                            onCheckedChange = { readerPref.readWithVolumeKeysInverted.set(it) },
                        )
                    }
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_vertical_navigator),
                        subtitle = if (verticalNavigator.isEmpty()) {
                            context.stringResource(
                                MR.strings.off,
                            )
                        } else {
                            "${verticalNavigator.size} items"
                        },
                        onClick = {
                            val entries = ReadingMode.entries.filter { it != ReadingMode.DEFAULT }
                                .associate { it to context.stringResource(it.stringRes) }
                            multiDialog = MultiListDialogData(
                                title = context.stringResource(MR.strings.pref_vertical_navigator),
                                entries = entries as Map<Any, String>,
                                selectedValues = verticalNavigator as Set<Any>,
                                onSelect = { readerPref.verticalNavigator.set(it as Set<ReadingMode>) },
                            )
                        },
                    )
                    if (verticalNavigator.isNotEmpty()) {
                        VmhSwitchPreference(
                            title = context.stringResource(MR.strings.pref_webtoon_vertical_navigator_on_left),
                            checked = verticalNavigatorOnLeft,
                            onCheckedChange = { readerPref.verticalNavigatorOnLeft.set(it) },
                        )
                        VmhSliderPreference(
                            title = context.stringResource(MR.strings.pref_vertical_navigator_height),
                            value = verticalNavigatorHeight,
                            valueRange = 65..100,
                            steps = 6,
                            onValueChange = { readerPref.verticalNavigatorHeight.set(it) },
                        )
                    }
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_show_navigation_mode),
                        subtitle = context.stringResource(MR.strings.pref_show_navigation_mode_summary),
                        checked = showNavigationOverlayOnStart,
                        onCheckedChange = { readerPref.showNavigationOverlayOnStart.set(it) },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_show_reading_mode),
                        subtitle = context.stringResource(MR.strings.pref_show_reading_mode_summary),
                        checked = showReadingMode,
                        onCheckedChange = { readerPref.showReadingMode.set(it) },
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
    private fun getPagedGroup(readerPref: ReaderPreferences): Preference {
        val context = LocalContext.current
        val navMode by readerPref.navigationModePager.collectAsState()
        val pagerNavInverted by readerPref.pagerNavInverted.collectAsState()
        val imageScaleType by readerPref.imageScaleType.collectAsState()
        val zoomStart by readerPref.zoomStart.collectAsState()
        val cropBorders by readerPref.cropBorders.collectAsState()
        val landscapeZoom by readerPref.landscapeZoom.collectAsState()
        val navigateToPan by readerPref.navigateToPan.collectAsState()
        val dualPageSplit by readerPref.dualPageSplitPaged.collectAsState()
        val dualPageInvert by readerPref.dualPageInvertPaged.collectAsState()
        val rotateToFit by readerPref.dualPageRotateToFit.collectAsState()
        val rotateToFitInvert by readerPref.dualPageRotateToFitInvert.collectAsState()

        return Preference.PreferenceItem.CustomPreference(title = context.stringResource(MR.strings.pager_viewer)) {
            var listDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }
            fun <T> openDialog(title: String, entries: Map<T, String>, selected: T, onSelect: (T) -> Unit) {
                listDialog = ListDialogData(title, entries as Map<Any, String>, selected as Any) { onSelect(it as T) }
            }

            val invertModes = listOf(
                ReaderPreferences.TappingInvertMode.NONE,
                ReaderPreferences.TappingInvertMode.HORIZONTAL,
                ReaderPreferences.TappingInvertMode.VERTICAL,
                ReaderPreferences.TappingInvertMode.BOTH,
            ).associateWith { context.stringResource(it.titleRes) }

            Column {
                VmhSectionHeader(context.stringResource(MR.strings.pager_viewer))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_viewer_nav),
                        subtitle =
                        ReaderPreferences.TapZones.getOrNull(navMode)?.let { context.stringResource(it) } ?: "",
                        onClick = {
                            openDialog(
                                title = context.stringResource(MR.strings.pref_viewer_nav),
                                entries = ReaderPreferences.TapZones.mapIndexed { index, it ->
                                    index to
                                        context.stringResource(it)
                                }.toMap(),
                                selected = navMode,
                                onSelect = { readerPref.navigationModePager.set(it) },
                            )
                        },
                    )
                    if (navMode != 5) {
                        VmhTextPreference(
                            title = context.stringResource(MR.strings.pref_read_with_tapping_inverted),
                            subtitle = invertModes[pagerNavInverted] ?: "",
                            onClick = {
                                openDialog(
                                    title = context.stringResource(MR.strings.pref_read_with_tapping_inverted),
                                    entries = invertModes,
                                    selected = pagerNavInverted,
                                    onSelect = { readerPref.pagerNavInverted.set(it) },
                                )
                            },
                        )
                        VmhSwitchPreference(
                            title = context.stringResource(MR.strings.pref_navigate_pan),
                            checked = navigateToPan,
                            onCheckedChange = { readerPref.navigateToPan.set(it) },
                        )
                    }
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_image_scale_type),
                        subtitle =
                        ReaderPreferences.ImageScaleType.getOrNull(imageScaleType - 1)?.let {
                            context.stringResource(it)
                        }
                            ?: "",
                        onClick = {
                            openDialog(
                                title = context.stringResource(MR.strings.pref_image_scale_type),
                                entries = ReaderPreferences.ImageScaleType.mapIndexed { index, it ->
                                    index + 1 to
                                        context.stringResource(it)
                                }.toMap(),
                                selected = imageScaleType,
                                onSelect = { readerPref.imageScaleType.set(it) },
                            )
                        },
                    )
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_zoom_start),
                        subtitle =
                        ReaderPreferences.ZoomStart.getOrNull(zoomStart - 1)?.let { context.stringResource(it) }
                            ?: "",
                        onClick = {
                            openDialog(
                                title = context.stringResource(MR.strings.pref_zoom_start),
                                entries = ReaderPreferences.ZoomStart.mapIndexed { index, it ->
                                    index + 1 to
                                        context.stringResource(it)
                                }.toMap(),
                                selected = zoomStart,
                                onSelect = { readerPref.zoomStart.set(it) },
                            )
                        },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_crop_borders),
                        checked = cropBorders,
                        onCheckedChange = { readerPref.cropBorders.set(it) },
                    )
                    if (imageScaleType == 1) {
                        VmhSwitchPreference(
                            title = context.stringResource(MR.strings.pref_landscape_zoom),
                            checked = landscapeZoom,
                            onCheckedChange = { readerPref.landscapeZoom.set(it) },
                        )
                    }
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_dual_page_split),
                        checked = dualPageSplit,
                        onCheckedChange = {
                            readerPref.dualPageSplitPaged.set(it)
                            if (it) readerPref.dualPageRotateToFit.set(false)
                        },
                    )
                    if (dualPageSplit) {
                        VmhSwitchPreference(
                            title = context.stringResource(MR.strings.pref_dual_page_invert),
                            subtitle = context.stringResource(MR.strings.pref_dual_page_invert_summary),
                            checked = dualPageInvert,
                            onCheckedChange = { readerPref.dualPageInvertPaged.set(it) },
                        )
                    }
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_page_rotate),
                        checked = rotateToFit,
                        onCheckedChange = {
                            readerPref.dualPageRotateToFit.set(it)
                            if (it) readerPref.dualPageSplitPaged.set(false)
                        },
                    )
                    if (rotateToFit) {
                        VmhSwitchPreference(
                            title = context.stringResource(MR.strings.pref_page_rotate_invert),
                            checked = rotateToFitInvert,
                            onCheckedChange = { readerPref.dualPageRotateToFitInvert.set(it) },
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
    private fun getWebtoonGroup(readerPref: ReaderPreferences): Preference {
        val context = LocalContext.current
        val navMode by readerPref.navigationModeWebtoon.collectAsState()
        val webtoonNavInverted by readerPref.webtoonNavInverted.collectAsState()
        val webtoonSidePadding by readerPref.webtoonSidePadding.collectAsState()
        val readerHideThreshold by readerPref.readerHideThreshold.collectAsState()
        val cropBordersWebtoon by readerPref.cropBordersWebtoon.collectAsState()
        val dualPageSplit by readerPref.dualPageSplitWebtoon.collectAsState()
        val dualPageInvert by readerPref.dualPageInvertWebtoon.collectAsState()
        val rotateToFit by readerPref.dualPageRotateToFitWebtoon.collectAsState()
        val rotateToFitInvert by readerPref.dualPageRotateToFitInvertWebtoon.collectAsState()
        val doubleTapZoomEnabled by readerPref.webtoonDoubleTapZoomEnabled.collectAsState()
        val disableZoomOut by readerPref.webtoonDisableZoomOut.collectAsState()

        val numberFormat = remember { NumberFormat.getPercentInstance() }

        return Preference.PreferenceItem.CustomPreference(title = context.stringResource(MR.strings.webtoon_viewer)) {
            var listDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }
            fun <T> openDialog(title: String, entries: Map<T, String>, selected: T, onSelect: (T) -> Unit) {
                listDialog = ListDialogData(title, entries as Map<Any, String>, selected as Any) { onSelect(it as T) }
            }

            val invertModes = listOf(
                ReaderPreferences.TappingInvertMode.NONE,
                ReaderPreferences.TappingInvertMode.HORIZONTAL,
                ReaderPreferences.TappingInvertMode.VERTICAL,
                ReaderPreferences.TappingInvertMode.BOTH,
            ).associateWith { context.stringResource(it.titleRes) }

            val hideThresholds = mapOf(
                ReaderPreferences.ReaderHideThreshold.HIGHEST to context.stringResource(MR.strings.pref_highest),
                ReaderPreferences.ReaderHideThreshold.HIGH to context.stringResource(MR.strings.pref_high),
                ReaderPreferences.ReaderHideThreshold.LOW to context.stringResource(MR.strings.pref_low),
                ReaderPreferences.ReaderHideThreshold.LOWEST to context.stringResource(MR.strings.pref_lowest),
            )

            Column {
                VmhSectionHeader(context.stringResource(MR.strings.webtoon_viewer))
                VmhSettingsCard {
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_viewer_nav),
                        subtitle =
                        ReaderPreferences.TapZones.getOrNull(navMode)?.let { context.stringResource(it) } ?: "",
                        onClick = {
                            openDialog(
                                title = context.stringResource(MR.strings.pref_viewer_nav),
                                entries = ReaderPreferences.TapZones.mapIndexed { index, it ->
                                    index to
                                        context.stringResource(it)
                                }.toMap(),
                                selected = navMode,
                                onSelect = { readerPref.navigationModeWebtoon.set(it) },
                            )
                        },
                    )
                    if (navMode != 5) {
                        VmhTextPreference(
                            title = context.stringResource(MR.strings.pref_read_with_tapping_inverted),
                            subtitle = invertModes[webtoonNavInverted] ?: "",
                            onClick = {
                                openDialog(
                                    title = context.stringResource(MR.strings.pref_read_with_tapping_inverted),
                                    entries = invertModes,
                                    selected = webtoonNavInverted,
                                    onSelect = { readerPref.webtoonNavInverted.set(it) },
                                )
                            },
                        )
                    }
                    VmhSliderPreference(
                        title = context.stringResource(MR.strings.pref_webtoon_side_padding),
                        value = webtoonSidePadding,
                        valueRange = ReaderPreferences.WEBTOON_PADDING_MIN..ReaderPreferences.WEBTOON_PADDING_MAX,
                        valueText = { numberFormat.format(it / 100f) },
                        onValueChange = { readerPref.webtoonSidePadding.set(it) },
                    )
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_hide_threshold),
                        subtitle = hideThresholds[readerHideThreshold] ?: "",
                        onClick = {
                            openDialog(
                                title = context.stringResource(MR.strings.pref_hide_threshold),
                                entries = hideThresholds,
                                selected = readerHideThreshold,
                                onSelect = { readerPref.readerHideThreshold.set(it) },
                            )
                        },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_crop_borders),
                        checked = cropBordersWebtoon,
                        onCheckedChange = { readerPref.cropBordersWebtoon.set(it) },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_dual_page_split),
                        checked = dualPageSplit,
                        onCheckedChange = {
                            readerPref.dualPageSplitWebtoon.set(it)
                            if (it) readerPref.dualPageRotateToFitWebtoon.set(false)
                        },
                    )
                    if (dualPageSplit) {
                        VmhSwitchPreference(
                            title = context.stringResource(MR.strings.pref_dual_page_invert),
                            subtitle = context.stringResource(MR.strings.pref_dual_page_invert_summary),
                            checked = dualPageInvert,
                            onCheckedChange = { readerPref.dualPageInvertWebtoon.set(it) },
                        )
                    }
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_page_rotate),
                        checked = rotateToFit,
                        onCheckedChange = {
                            readerPref.dualPageRotateToFitWebtoon.set(it)
                            if (it) readerPref.dualPageSplitWebtoon.set(false)
                        },
                    )
                    if (rotateToFit) {
                        VmhSwitchPreference(
                            title = context.stringResource(MR.strings.pref_page_rotate_invert),
                            checked = rotateToFitInvert,
                            onCheckedChange = { readerPref.dualPageRotateToFitInvertWebtoon.set(it) },
                        )
                    }
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_double_tap_zoom),
                        checked = doubleTapZoomEnabled,
                        onCheckedChange = { readerPref.webtoonDoubleTapZoomEnabled.set(it) },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_webtoon_disable_zoom_out),
                        checked = disableZoomOut,
                        onCheckedChange = { readerPref.webtoonDisableZoomOut.set(it) },
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
    private fun getAdvancedGroup(readerPref: ReaderPreferences): Preference {
        val context = LocalContext.current
        val skipRead by readerPref.skipRead.collectAsState()
        val skipFiltered by readerPref.skipFiltered.collectAsState()
        val skipDupe by readerPref.skipDupe.collectAsState()
        val alwaysShowChapterTransition by readerPref.alwaysShowChapterTransition.collectAsState()
        val pageTransitions by readerPref.pageTransitions.collectAsState()
        val doubleTapAnimSpeed by readerPref.doubleTapAnimSpeed.collectAsState()
        val loadAheadOnlyOverWifi by readerPref.loadAheadOnlyOverWifi.collectAsState()
        val readWithLongTap by readerPref.readWithLongTap.collectAsState()
        val folderPerManga by readerPref.folderPerManga.collectAsState()
        val flashOnPageChange by readerPref.flashOnPageChange.collectAsState()
        val flashDurationMillis by readerPref.flashDurationMillis.collectAsState()
        val flashPageInterval by readerPref.flashPageInterval.collectAsState()
        val flashColor by readerPref.flashColor.collectAsState()

        return Preference.PreferenceItem.CustomPreference(
            title = context.stringResource(MR.strings.pref_category_advanced),
        ) {
            var listDialog by remember { mutableStateOf<ListDialogData<Any>?>(null) }
            fun <T> openDialog(title: String, entries: Map<T, String>, selected: T, onSelect: (T) -> Unit) {
                listDialog = ListDialogData(title, entries as Map<Any, String>, selected as Any) { onSelect(it as T) }
            }

            val speedOptions = mapOf(
                1 to context.stringResource(MR.strings.double_tap_anim_speed_0),
                500 to context.stringResource(MR.strings.double_tap_anim_speed_normal),
                250 to context.stringResource(MR.strings.double_tap_anim_speed_fast),
            )

            val flashColors = mapOf(
                ReaderPreferences.FlashColor.BLACK to context.stringResource(MR.strings.pref_flash_style_black),
                ReaderPreferences.FlashColor.WHITE to context.stringResource(MR.strings.pref_flash_style_white),
                ReaderPreferences.FlashColor.WHITE_BLACK to
                    context.stringResource(MR.strings.pref_flash_style_white_black),
            )

            Column {
                VmhSectionHeader(context.stringResource(MR.strings.pref_category_advanced))
                VmhSettingsCard {
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_skip_read_chapters),
                        checked = skipRead,
                        onCheckedChange = { readerPref.skipRead.set(it) },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_skip_filtered_chapters),
                        checked = skipFiltered,
                        onCheckedChange = { readerPref.skipFiltered.set(it) },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_skip_dupe_chapters),
                        checked = skipDupe,
                        onCheckedChange = { readerPref.skipDupe.set(it) },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_always_show_chapter_transition),
                        checked = alwaysShowChapterTransition,
                        onCheckedChange = { readerPref.alwaysShowChapterTransition.set(it) },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_page_transitions),
                        checked = pageTransitions,
                        onCheckedChange = { readerPref.pageTransitions.set(it) },
                    )
                    VmhTextPreference(
                        title = context.stringResource(MR.strings.pref_double_tap_anim_speed),
                        subtitle = speedOptions[doubleTapAnimSpeed] ?: "",
                        onClick = {
                            openDialog(
                                title = context.stringResource(MR.strings.pref_double_tap_anim_speed),
                                entries = speedOptions,
                                selected = doubleTapAnimSpeed,
                                onSelect = { readerPref.doubleTapAnimSpeed.set(it) },
                            )
                        },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_load_ahead_only_over_wifi),
                        subtitle = context.stringResource(MR.strings.pref_load_ahead_only_over_wifi_summary),
                        checked = loadAheadOnlyOverWifi,
                        onCheckedChange = { readerPref.loadAheadOnlyOverWifi.set(it) },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_read_with_long_tap),
                        checked = readWithLongTap,
                        onCheckedChange = { readerPref.readWithLongTap.set(it) },
                    )
                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_create_folder_per_manga),
                        subtitle = context.stringResource(MR.strings.pref_create_folder_per_manga_summary),
                        checked = folderPerManga,
                        onCheckedChange = { readerPref.folderPerManga.set(it) },
                    )

                    VmhSwitchPreference(
                        title = context.stringResource(MR.strings.pref_flash_page),
                        subtitle = context.stringResource(MR.strings.pref_flash_page_summ),
                        checked = flashOnPageChange,
                        onCheckedChange = { readerPref.flashOnPageChange.set(it) },
                    )
                    if (flashOnPageChange) {
                        VmhSliderPreference(
                            title = context.stringResource(MR.strings.pref_flash_duration),
                            value = flashDurationMillis,
                            valueRange = 100..1500,
                            steps = 13,
                            valueText = { context.stringResource(MR.strings.pref_flash_duration_summary, it) },
                            onValueChange = { readerPref.flashDurationMillis.set(it) },
                        )
                        VmhSliderPreference(
                            title = context.stringResource(MR.strings.pref_flash_page_interval),
                            value = flashPageInterval,
                            valueRange = 1..10,
                            valueText = { context.pluralStringResource(MR.plurals.pref_pages, it, it) },
                            onValueChange = { readerPref.flashPageInterval.set(it) },
                        )
                        VmhTextPreference(
                            title = context.stringResource(MR.strings.pref_flash_with),
                            subtitle = flashColors[flashColor] ?: "",
                            onClick = {
                                openDialog(
                                    title = context.stringResource(MR.strings.pref_flash_with),
                                    entries = flashColors,
                                    selected = flashColor,
                                    onSelect = { readerPref.flashColor.set(it) },
                                )
                            },
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
}
