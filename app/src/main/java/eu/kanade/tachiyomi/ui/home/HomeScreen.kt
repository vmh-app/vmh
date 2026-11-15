package eu.kanade.tachiyomi.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import eu.kanade.presentation.util.Screen
import eu.kanade.presentation.util.isTabletUi
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.GlobalSearchScreen
import eu.kanade.tachiyomi.ui.download.DownloadQueueScreen
import eu.kanade.tachiyomi.ui.history.HistoryTab
import eu.kanade.tachiyomi.ui.library.LibraryTab
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.more.MoreTab
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.MoreVert
import mihon.icons.materialsymbols.rounded.Search
import soup.compose.material.motion.animation.materialFadeThroughIn
import soup.compose.material.motion.animation.materialFadeThroughOut
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import cafe.adriel.voyager.navigator.tab.Tab as VoyagerTab

object HomeScreen : Screen() {

    private val librarySearchEvent = Channel<String>()
    private val openTabEvent = Channel<Tab>()
    private val showBottomNavEvent = Channel<Boolean>()

    @Suppress("ConstPropertyName")
    private const val TabFadeDuration = 200

    @Suppress("ConstPropertyName")
    private const val TabNavigatorKey = "HomeTabs"

    private val VmhCapsuleHeight = 48.dp

    private val MAIN_TABS = listOf(
        HomeTab,
        LibraryTab,
        BrowseTab,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TabNavigator(
            tab = HomeTab,
            key = TabNavigatorKey,
        ) { tabNavigator ->
            CompositionLocalProvider(LocalNavigator provides navigator) {
                val tabletUi = isTabletUi()
                val navigationSuiteType = if (tabletUi) {
                    NavigationSuiteType.NavigationRail
                } else {
                    NavigationSuiteType.NavigationBar
                }
                val navigationSuiteState = rememberNavigationSuiteScaffoldState()
                val scope = rememberCoroutineScope()
                var bottomBarVisible by remember { mutableStateOf(true) }

                LaunchedEffect(navigationSuiteState, tabletUi) {
                    if (tabletUi) navigationSuiteState.show()
                    showBottomNavEvent.receiveAsFlow().collectLatest { show ->
                        bottomBarVisible = show
                        if (tabletUi || show) {
                            navigationSuiteState.show()
                        } else {
                            navigationSuiteState.hide()
                        }
                    }
                }

                val onTabSelected: (eu.kanade.presentation.util.Tab) -> Unit = { tab ->
                    if (tabNavigator.current::class != tab::class) {
                        tabNavigator.current = tab
                    } else {
                        scope.launch { tab.onReselect(navigator) }
                    }
                }

                if (tabletUi) {
                    var tabletMoreMenuExpanded by remember { mutableStateOf(false) }
                    NavigationSuiteScaffold(
                        navigationSuiteType = navigationSuiteType,
                        state = navigationSuiteState,
                        navigationSuiteColors = NavigationSuiteDefaults.colors(
                            navigationRailContainerColor = MaterialTheme.colorScheme
                                .surfaceColorAtElevation(3.dp),
                        ),
                        navigationItemVerticalArrangement = Arrangement.Center,
                        navigationItems = {
                            MAIN_TABS.fastForEach { NavigationSuiteItem(it, navigationSuiteType) }

                            val isMoreSelected = tabNavigator.current is HistoryTab || tabNavigator.current is MoreTab
                            Box {
                                NavigationSuiteItem(
                                    navigationSuiteType = navigationSuiteType,
                                    selected = isMoreSelected,
                                    onClick = { tabletMoreMenuExpanded = true },
                                    icon = {
                                        Icon(
                                            imageVector = MaterialSymbols.Rounded.MoreVert,
                                            contentDescription = stringResource(MR.strings.label_more),
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(MR.strings.label_more),
                                            style = MaterialTheme.typography.labelLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                )
                                DropdownMenu(
                                    expanded = tabletMoreMenuExpanded,
                                    onDismissRequest = { tabletMoreMenuExpanded = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(MR.strings.label_recent_manga)) },
                                        onClick = {
                                            tabletMoreMenuExpanded = false
                                            onTabSelected(HistoryTab)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                painter = HistoryTab.options.icon!!,
                                                contentDescription = null,
                                            )
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(MR.strings.label_settings)) },
                                        onClick = {
                                            tabletMoreMenuExpanded = false
                                            onTabSelected(MoreTab)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                painter = MoreTab.options.icon!!,
                                                contentDescription = null,
                                            )
                                        },
                                    )
                                }
                            }
                        },
                    ) {
                        TabContent(tabNavigator)
                    }
                } else {
                    val tabBottomPadding by animateDpAsState(
                        targetValue = if (bottomBarVisible) 96.dp else 0.dp,
                        label = "tabBottomPadding",
                    )
                    Box(modifier = Modifier.fillMaxSize()) {
                        TabContent(
                            tabNavigator = tabNavigator,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = tabBottomPadding)
                                .then(if (bottomBarVisible) Modifier.navigationBarsPadding() else Modifier),
                        )

                        AnimatedVisibility(
                            visible = bottomBarVisible,
                            modifier = Modifier.align(Alignment.BottomCenter),
                            enter = fadeIn() + slideInVertically { it },
                            exit = fadeOut() + slideOutVertically { it },
                        ) {
                            VmhBottomBar(
                                tabs = MAIN_TABS,
                                current = tabNavigator.current,
                                onSelect = onTabSelected,
                            )
                        }
                    }
                }
            }

            val goToHomeTab = { tabNavigator.current = HomeTab }

            BackHandler(enabled = tabNavigator.current != HomeTab, onBack = goToHomeTab)

            LaunchedEffect(Unit) {
                launch {
                    librarySearchEvent.receiveAsFlow().collectLatest {
                        tabNavigator.current = LibraryTab
                        LibraryTab.search(it)
                    }
                }
                launch {
                    openTabEvent.receiveAsFlow().collectLatest {
                        tabNavigator.current = when (it) {
                            is Tab.Home -> HomeTab
                            is Tab.Library -> LibraryTab
                            is Tab.History -> HistoryTab
                            is Tab.Browse -> {
                                if (it.toExtensions) {
                                    BrowseTab.showExtension()
                                }
                                BrowseTab
                            }
                            is Tab.More -> MoreTab
                        }

                        if (it is Tab.Library && it.mangaIdToOpen != null) {
                            navigator.push(MangaScreen(it.mangaIdToOpen))
                        }
                        if (it is Tab.More && it.toDownloads) {
                            navigator.push(DownloadQueueScreen)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TabContent(
        tabNavigator: TabNavigator,
        modifier: Modifier = Modifier,
    ) {
        AnimatedContent(
            targetState = tabNavigator.current,
            modifier = modifier,
            transitionSpec = {
                materialFadeThroughIn(
                    initialScale = 1f,
                    durationMillis = TabFadeDuration,
                ) togetherWith materialFadeThroughOut(durationMillis = TabFadeDuration)
            },
            label = "tabContent",
        ) {
            tabNavigator.saveableState(key = "currentTab", it) {
                it.Content()
            }
        }
    }

    @Composable
    private fun VmhBottomBar(
        tabs: List<eu.kanade.presentation.util.Tab>,
        current: VoyagerTab,
        onSelect: (eu.kanade.presentation.util.Tab) -> Unit,
    ) {
        val colors = MaterialTheme.colorScheme
        val navigator = LocalNavigator.currentOrThrow

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(62.dp),
                shape = RoundedCornerShape(26.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.outlineVariant),
                shadowElevation = 10.dp,
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val slotWidth = maxWidth / tabs.size

                    val selectedIndex = tabs.indexOfFirst { current::class == it::class }
                    val showCapsule = selectedIndex != -1

                    if (showCapsule) {
                        val targetTabOffset = slotWidth * selectedIndex + 6.dp
                        val tabOffset by animateDpAsState(
                            targetValue = targetTabOffset,
                            animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                            label = "vmhTabOffset",
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = tabOffset)
                                .width(slotWidth - 12.dp)
                                .height(VmhCapsuleHeight)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.primary),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        tabs.fastForEach { tab ->
                            val selected = current::class == tab::class
                            val contentColor by animateColorAsState(
                                targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant,
                                animationSpec = tween(durationMillis = 200),
                                label = "vmhItemColor",
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { onSelect(tab) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        painter = tab.options.icon!!,
                                        contentDescription = navLabel(tab),
                                        tint = contentColor,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = navLabel(tab),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = contentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                tabBadge(tab)?.let { badge ->
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(y = if (selected) 2.dp else 4.dp)
                                            .padding(end = 6.dp),
                                    ) {
                                        badge()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            val isMoreActive = current is HistoryTab || current is MoreTab
            val moreIconColor by animateColorAsState(
                targetValue = if (isMoreActive) colors.primary else colors.onSurfaceVariant,
                animationSpec = tween(durationMillis = 200),
                label = "vmhMoreIconColor",
            )
            var moreMenuExpanded by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier
                    .width(26.dp)
                    .height(62.dp),
                shape = RoundedCornerShape(14.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.outlineVariant),
                shadowElevation = 10.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { moreMenuExpanded = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.MoreVert,
                        contentDescription = stringResource(MR.strings.label_more),
                        tint = moreIconColor,
                        modifier = Modifier.size(16.dp),
                    )

                    DropdownMenu(
                        expanded = moreMenuExpanded,
                        onDismissRequest = { moreMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(MR.strings.label_recent_manga)) },
                            onClick = {
                                moreMenuExpanded = false
                                onSelect(HistoryTab)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = HistoryTab.options.icon!!,
                                    contentDescription = null,
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(MR.strings.label_settings)) },
                            onClick = {
                                moreMenuExpanded = false
                                onSelect(MoreTab)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = MoreTab.options.icon!!,
                                    contentDescription = null,
                                )
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier.size(62.dp),
                shape = CircleShape,
                color = colors.primary,
                shadowElevation = 8.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { navigator.push(GlobalSearchScreen()) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Search,
                        contentDescription = stringResource(MR.strings.action_global_search),
                        tint = colors.onPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }

    @Composable
    private fun navLabel(tab: eu.kanade.presentation.util.Tab): String = when (tab) {
        is HomeTab -> stringResource(MR.strings.label_home)
        is LibraryTab -> stringResource(MR.strings.label_library)
        is HistoryTab -> stringResource(MR.strings.label_recent_manga)
        is BrowseTab -> stringResource(MR.strings.label_sources)
        else -> stringResource(MR.strings.label_settings)
    }

    @Composable
    private fun NavigationSuiteItem(
        tab: eu.kanade.presentation.util.Tab,
        navigationSuiteType: NavigationSuiteType,
    ) {
        val tabNavigator = LocalTabNavigator.current
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val selected = tabNavigator.current::class == tab::class
        NavigationSuiteItem(
            navigationSuiteType = navigationSuiteType,
            selected = selected,
            onClick = {
                if (!selected) {
                    tabNavigator.current = tab
                } else {
                    scope.launch { tab.onReselect(navigator) }
                }
            },
            icon = {
                Icon(
                    painter = tab.options.icon!!,
                    contentDescription = navLabel(tab),
                )
            },
            label = {
                Text(
                    text = navLabel(tab),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            badge = tabBadge(tab),
        )
    }

    @Composable
    private fun tabBadge(tab: eu.kanade.presentation.util.Tab): (@Composable () -> Unit)? {
        val context = LocalContext.current
        val count by produceState(initialValue = 0, tab) {
            val graph = context.appGraph
            when (tab) {
                is LibraryTab -> {
                    combine(
                        graph.libraryPreferences.newShowUpdatesCount.changes(),
                        graph.libraryPreferences.newUpdatesCount.changes(),
                    ) { show, count ->
                        if (show) count else 0
                    }
                        .collectLatest { value = it }
                }

                is BrowseTab -> {
                    graph.sourcePreferences.extensionUpdatesCount.changes()
                        .collectLatest { value = it }
                }

                else -> value = 0
            }
        }
        if (count <= 0) return null
        return {
            Badge {
                val desc = when (tab) {
                    is LibraryTab -> pluralStringResource(
                        MR.plurals.home_badge_library_updates,
                        count = count,
                        count,
                    )

                    is BrowseTab -> pluralStringResource(
                        MR.plurals.update_check_notification_ext_updates,
                        count = count,
                        count,
                    )

                    else -> null
                }
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    modifier = Modifier.semantics {
                        if (desc != null) contentDescription = desc
                    },
                )
            }
        }
    }

    suspend fun search(query: String) {
        librarySearchEvent.send(query)
    }

    suspend fun openTab(tab: Tab) {
        openTabEvent.send(tab)
    }

    suspend fun showBottomNav(show: Boolean) {
        showBottomNavEvent.send(show)
    }

    sealed interface Tab {
        data object Home : Tab
        data class Library(val mangaIdToOpen: Long? = null) : Tab
        data object History : Tab
        data class Browse(val toExtensions: Boolean = false) : Tab
        data class More(val toDownloads: Boolean) : Tab
    }
}
