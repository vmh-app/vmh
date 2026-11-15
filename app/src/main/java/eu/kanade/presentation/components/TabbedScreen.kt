package eu.kanade.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Close
import mihon.icons.materialsymbols.rounded.MoreVert
import mihon.icons.materialsymbols.rounded.Search
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.VmhScreenHeader
import tachiyomi.presentation.core.components.VmhSnackbarHost
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun TabbedScreen(
    titleRes: StringResource,
    tabs: List<TabContent>,
    state: PagerState = rememberPagerState { tabs.size },
    searchQuery: String? = null,
    onChangeSearchQuery: (String?) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentTab = tabs[state.currentPage]

    Scaffold(
        topBar = {
            VmhCatalogHeader(
                eyebrow = stringResource(titleRes),
                title = stringResource(currentTab.titleRes),
                subtitle = currentTab.subtitleText ?: currentTab.subtitleRes?.let { stringResource(it) },
                actions = currentTab.actions,
                searchEnabled = currentTab.searchEnabled,
                searchQuery = searchQuery,
                onChangeSearchQuery = onChangeSearchQuery,
            )
        },
        snackbarHost = { VmhSnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
        ) {
            VmhTabCapsules(
                tabs = tabs,
                selectedIndex = state.currentPage,
                onSelect = { index -> scope.launch { state.animateScrollToPage(index) } },
            )

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = state,
                verticalAlignment = Alignment.Top,
            ) { page ->
                tabs[page].content(
                    PaddingValues(bottom = contentPadding.calculateBottomPadding()),
                    snackbarHostState,
                )
            }
        }
    }
}

@Composable
private fun VmhCatalogHeader(
    eyebrow: String,
    title: String,
    subtitle: String?,
    actions: List<AppBar.AppBarAction>,
    searchEnabled: Boolean,
    searchQuery: String?,
    onChangeSearchQuery: (String?) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 18.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
    ) {
        if (searchEnabled && searchQuery != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onChangeSearchQuery,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    placeholder = {
                        Text(
                            text = stringResource(MR.strings.action_search_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant,
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colors.outlineVariant,
                        focusedBorderColor = colors.primary,
                        unfocusedContainerColor = colors.surface,
                        focusedContainerColor = colors.surface,
                    ),
                )
                VmhCircleAction(
                    icon = MaterialSymbols.Rounded.Close,
                    contentDescription = stringResource(MR.strings.action_cancel),
                    onClick = { onChangeSearchQuery(null) },
                )
            }
            return@Column
        }

        VmhScreenHeader(
            eyebrow = eyebrow,
            title = title,
            subtitle = subtitle,
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (searchEnabled) {
                        VmhCircleAction(
                            icon = MaterialSymbols.Rounded.Search,
                            contentDescription = stringResource(MR.strings.action_search),
                            onClick = { onChangeSearchQuery("") },
                        )
                    }

                    actions.forEach { action ->
                        if (action is AppBar.Action) {
                            VmhCircleAction(
                                icon = action.icon,
                                contentDescription = action.title,
                                tint = action.iconTint,
                                enabled = action.enabled,
                                onClick = action.onClick,
                            )
                        }
                    }

                    val overflow = actions.filterIsInstance<AppBar.OverflowAction>()
                    if (overflow.isNotEmpty()) {
                        VmhOverflowAction(overflow)
                    }
                }
            },
        )
    }
}

@Composable
private fun VmhOverflowAction(actions: List<AppBar.OverflowAction>) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        VmhCircleAction(
            icon = MaterialSymbols.Rounded.MoreVert,
            contentDescription = stringResource(MR.strings.label_more),
            onClick = { expanded = true },
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(text = action.title) },
                    onClick = {
                        expanded = false
                        action.onClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun VmhTabCapsules(
    tabs: List<TabContent>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Surface(
                onClick = { onSelect(index) },
                shape = RoundedCornerShape(17.dp),
                color = if (selected) colors.primary else colors.surface,
                contentColor = if (selected) colors.onPrimary else colors.onSurface,
                border = if (selected) null else BorderStroke(1.dp, colors.outlineVariant),
                shadowElevation = if (selected) 6.dp else 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .height(34.dp)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(tab.titleRes),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                    )
                    tab.badgeNumber?.takeIf { it > 0 }?.let { count ->
                        Box(
                            modifier = Modifier
                                .height(17.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (selected) colors.onPrimary else colors.secondary)
                                .padding(horizontal = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = if (selected) colors.primary else colors.onSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

data class TabContent(
    val titleRes: StringResource,
    val subtitleRes: StringResource? = null,
    val subtitleText: String? = null,
    val badgeNumber: Int? = null,
    val searchEnabled: Boolean = false,
    val actions: List<AppBar.AppBarAction> = listOf(),
    val content: @Composable (contentPadding: PaddingValues, snackbarHostState: SnackbarHostState) -> Unit,
)
