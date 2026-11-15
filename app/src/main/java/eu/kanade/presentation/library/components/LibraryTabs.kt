package eu.kanade.presentation.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.category.visualName
import tachiyomi.domain.category.model.Category

@Composable
internal fun LibraryTabs(
    categories: List<Category>,
    pagerState: PagerState,
    getItemCountForCategory: (Category) -> Int?,
    onTabItemClick: (Int) -> Unit,
) {
    val currentPageIndex = pagerState.currentPage.coerceAtMost(categories.lastIndex)
    val listState = rememberLazyListState()

    LaunchedEffect(currentPageIndex) {
        listState.animateScrollToItem(currentPageIndex)
    }

    LazyRow(
        modifier = Modifier.padding(top = 2.dp),
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(categories) { index, category ->
            CategoryTab(
                name = category.visualName,
                count = getItemCountForCategory(category),
                selected = currentPageIndex == index,
                onClick = { onTabItemClick(index) },
            )
        }
    }
}

@Composable
private fun CategoryTab(
    name: String,
    count: Int?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) colors.primary else colors.surfaceContainer)
            .border(
                width = 1.dp,
                color = if (selected) colors.primary else colors.outlineVariant,
                shape = CircleShape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.onPrimary else colors.onSurface,
            maxLines = 1,
        )
        if (count != null) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            colors.onPrimary.copy(alpha = 0.22f)
                        } else {
                            colors.surfaceContainerHighest
                        },
                    )
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp,
                    color = if (selected) colors.onPrimary else colors.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}
