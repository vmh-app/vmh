package eu.kanade.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.MoreVert
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

object TabbedDialogPaddings {
    val Horizontal = 16.dp
    val Vertical = 8.dp
}

@Composable
fun TabbedDialog(
    onDismissRequest: () -> Unit,
    tabTitles: List<String>,
    modifier: Modifier = Modifier,
    tabOverflowMenuContent: (@Composable ColumnScope.(() -> Unit) -> Unit)? = null,
    pagerState: PagerState = rememberPagerState { tabTitles.size },
    content: @Composable (Int) -> Unit,
) {
    AdaptiveSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        val scope = rememberCoroutineScope()
        val colors = MaterialTheme.colorScheme

        Column(modifier = Modifier.background(colors.surface)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp),
                ) {
                    itemsIndexed(tabTitles) { index, tab ->
                        VmhTabPill(
                            text = tab,
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        )
                    }
                }

                tabOverflowMenuContent?.let { MoreMenu(it) }
            }

            HorizontalPager(
                modifier = Modifier.animateContentSize(),
                state = pagerState,
                verticalAlignment = Alignment.Top,
                pageContent = { page -> content(page) },
            )
        }
    }
}

@Composable
private fun VmhTabPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        modifier = Modifier.height(34.dp),
        shape = RoundedCornerShape(17.dp),
        color = if (selected) colors.primary.copy(alpha = 0.12f) else colors.surface,
        contentColor = if (selected) colors.primary else colors.onSurfaceVariant,
        border = BorderStroke(
            1.dp,
            if (selected) colors.primary.copy(alpha = 0.5f) else colors.outlineVariant,
        ),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun MoreMenu(
    content: @Composable ColumnScope.(() -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        VmhCircleAction(
            icon = MaterialSymbols.Rounded.MoreVert,
            contentDescription = stringResource(MR.strings.label_more),
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            content { expanded = false }
        }
    }
}
