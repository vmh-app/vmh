package eu.kanade.presentation.more.settings.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.more.settings.screen.about.AboutScreen
import eu.kanade.presentation.util.LocalBackPress
import eu.kanade.presentation.util.Screen
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ChromeReaderMode
import mihon.icons.materialsymbols.rounded.Code
import mihon.icons.materialsymbols.rounded.CollectionsBookmark
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Explore
import mihon.icons.materialsymbols.rounded.Info
import mihon.icons.materialsymbols.rounded.Palette
import mihon.icons.materialsymbols.rounded.Search
import mihon.icons.materialsymbols.rounded.Security
import mihon.icons.materialsymbols.rounded.Storage
import mihon.icons.materialsymbols.rounded.Sync
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import cafe.adriel.voyager.core.screen.Screen as VoyagerScreen

object SettingsMainScreen : Screen() {

    @Composable
    override fun Content() {
        Content(twoPane = false)
    }

    @Composable
    private fun getPalerSurface(): Color {
        val surface = MaterialTheme.colorScheme.surface
        val dark = isSystemInDarkTheme()
        return remember(surface, dark) {
            val arr = FloatArray(3)
            ColorUtils.colorToHSL(surface.toArgb(), arr)
            arr[2] = if (dark) {
                arr[2] - 0.05f
            } else {
                arr[2] + 0.02f
            }.coerceIn(0f, 1f)
            Color.hsl(arr[0], arr[1], arr[2])
        }
    }

    @Composable
    fun Content(twoPane: Boolean) {
        val navigator = LocalNavigator.currentOrThrow
        val backPress = LocalBackPress.currentOrThrow
        val containerColor = if (twoPane) getPalerSurface() else MaterialTheme.colorScheme.surface
        val topBarState = rememberTopAppBarState()

        Scaffold(
            topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState),
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(MR.strings.label_settings),
                    navigateUp = backPress::invoke,
                    actions = {
                        AppBarActions(
                            listOf(
                                AppBar.Action(
                                    title = stringResource(MR.strings.action_search),
                                    icon = MaterialSymbols.Rounded.Search,
                                    onClick = { navigator.navigate(SettingsSearchScreen(), twoPane) },
                                ),
                            ),
                        )
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            containerColor = containerColor,
            content = { contentPadding ->
                val state = rememberLazyListState()

                val activeScreenClass = if (twoPane) navigator.items.first()::class else null
                val currentSectionIndex = if (activeScreenClass != null) {
                    sections.indexOfFirst { section ->
                        section.items.any { it.screen::class == activeScreenClass }
                    }
                } else {
                    -1
                }

                LaunchedEffect(currentSectionIndex) {
                    if (twoPane && currentSectionIndex >= 0) {
                        state.animateScrollToItem(currentSectionIndex)
                        if (currentSectionIndex > 0) {
                            topBarState.contentOffset = topBarState.heightOffsetLimit
                        }
                    }
                }

                LazyColumn(
                    state = state,
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    sections.forEach { section ->
                        item {
                            Text(
                                text = stringResource(section.titleRes).uppercase(),
                                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        item {
                            BoxWithConstraints(modifier = Modifier.padding(horizontal = 16.dp)) {
                                val columns = if (maxWidth < 300.dp) 1 else 2
                                Column {
                                    section.items.chunked(columns).forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.padding(bottom = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            rowItems.forEach { item ->
                                                val isSelected = activeScreenClass != null &&
                                                    item.screen::class == activeScreenClass
                                                SettingsEntryCard(
                                                    item = item,
                                                    isSelected = isSelected,
                                                    onClick = { navigator.navigate(item.screen, twoPane) },
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                            if (columns == 2 && rowItems.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            },
        )
    }

    private fun Navigator.navigate(screen: VoyagerScreen, twoPane: Boolean) {
        if (twoPane) replaceAll(screen) else push(screen)
    }

    @Composable
    private fun SettingsEntryCard(
        item: Item,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val bgColor = if (isSelected) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }
        val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = bgColor,
            // 136 dp para que quepan titulo de dos lineas y resumen de tres: con 116 dp y una
            // sola linea el resumen salia cortado ("Colores de la interfaz y for...").
            modifier = modifier.height(136.dp),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(26.dp),
                )

                Column {
                    Text(
                        text = stringResource(item.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val subtitle = item.formatSubtitle()
                    if (!subtitle.isNullOrEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }

    private data class Item(
        val titleRes: StringResource,
        val subtitleRes: StringResource? = null,
        val formatSubtitle: @Composable () -> String? = { subtitleRes?.let { stringResource(it) } },
        val icon: ImageVector,
        val screen: VoyagerScreen,
    )

    private data class Section(
        val titleRes: StringResource,
        val items: List<Item>,
    )

    private val sections = listOf(
        Section(
            titleRes = MR.strings.vmh_more_reading,
            items = listOf(
                Item(
                    titleRes = MR.strings.pref_category_appearance,
                    subtitleRes = MR.strings.pref_appearance_summary,
                    icon = MaterialSymbols.Rounded.Palette,
                    screen = SettingsAppearanceScreen,
                ),
                Item(
                    titleRes = MR.strings.pref_category_library,
                    subtitleRes = MR.strings.pref_library_summary,
                    icon = MaterialSymbols.Rounded.CollectionsBookmark,
                    screen = SettingsLibraryScreen,
                ),
                Item(
                    titleRes = MR.strings.pref_category_reader,
                    subtitleRes = MR.strings.pref_reader_summary,
                    icon = MaterialSymbols.AutoMirroredRounded.ChromeReaderMode,
                    screen = SettingsReaderScreen,
                ),
            ),
        ),
        Section(
            titleRes = MR.strings.vmh_more_content,
            items = listOf(
                Item(
                    titleRes = MR.strings.pref_category_downloads,
                    subtitleRes = MR.strings.pref_downloads_summary,
                    icon = MaterialSymbols.Rounded.Download,
                    screen = SettingsDownloadScreen,
                ),
                Item(
                    titleRes = MR.strings.pref_category_tracking,
                    subtitleRes = MR.strings.pref_tracking_summary,
                    icon = MaterialSymbols.Rounded.Sync,
                    screen = SettingsTrackingScreen,
                ),
                Item(
                    titleRes = MR.strings.browse,
                    subtitleRes = MR.strings.pref_browse_summary,
                    icon = MaterialSymbols.Rounded.Explore,
                    screen = SettingsBrowseScreen,
                ),
                Item(
                    titleRes = MR.strings.label_data_storage,
                    subtitleRes = MR.strings.pref_backup_summary,
                    icon = MaterialSymbols.Rounded.Storage,
                    screen = SettingsDataScreen,
                ),
            ),
        ),
        Section(
            titleRes = MR.strings.vmh_more_about,
            items = listOf(
                Item(
                    titleRes = MR.strings.pref_category_security,
                    subtitleRes = MR.strings.pref_security_summary,
                    icon = MaterialSymbols.Rounded.Security,
                    screen = SettingsSecurityScreen,
                ),
                Item(
                    titleRes = MR.strings.pref_category_advanced,
                    subtitleRes = MR.strings.pref_advanced_summary,
                    icon = MaterialSymbols.Rounded.Code,
                    screen = SettingsAdvancedScreen,
                ),
                Item(
                    titleRes = MR.strings.pref_category_about,
                    formatSubtitle = {
                        "${stringResource(MR.strings.app_name)} ${AboutScreen.getVersionName(withBuildDate = false)}"
                    },
                    icon = MaterialSymbols.Rounded.Info,
                    screen = AboutScreen,
                ),
            ),
        ),
    )
}
