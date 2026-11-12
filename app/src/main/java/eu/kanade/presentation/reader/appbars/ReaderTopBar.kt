package eu.kanade.presentation.reader.appbars

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mihon.icons.custommaterialsymbols.CustomMaterialSymbols
import mihon.icons.custommaterialsymbols.rounded.Back
import mihon.icons.custommaterialsymbols.rounded.Bookmark
import mihon.icons.custommaterialsymbols.rounded.BookmarkFilled
import mihon.icons.custommaterialsymbols.rounded.MoreVert
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import java.util.Locale

private val VmhLomoAzul = Color(0xFF0B1F3A)
private val VmhLomoAzulClaro = Color(0xFF8AB4FF)
private val VmhTextoClaro = Color(0xFFF7F9FF)
private val VmhCoralActivo = Color(0xFFFF8A70)
private val VmhIconoActivo = Color(0xFF06214F)

@Composable
fun ReaderTopBar(
    mangaTitle: String?,
    chapterTitle: String?,
    navigateUp: () -> Unit,
    bookmarked: Boolean,
    onToggleBookmarked: () -> Unit,
    onOpenInWebView: (() -> Unit)?,
    onOpenInBrowser: (() -> Unit)?,
    onShare: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val hasChapterTitle = !chapterTitle.isNullOrBlank()
    var menuExpanded by remember { mutableStateOf(false) }

    val overflowActions = buildList {
        onOpenInWebView?.let {
            add(
                ReaderOverflowAction(
                    title = stringResource(MR.strings.action_open_in_web_view),
                    onClick = it,
                ),
            )
        }
        onOpenInBrowser?.let {
            add(
                ReaderOverflowAction(
                    title = stringResource(MR.strings.action_open_in_browser),
                    onClick = it,
                ),
            )
        }
        onShare?.let {
            add(
                ReaderOverflowAction(
                    title = stringResource(MR.strings.action_share),
                    onClick = it,
                ),
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VmhLomoAzul)
            .drawBehind {
                drawRect(
                    color = VmhLomoAzulClaro,
                    size = Size(width = 4.dp.toPx(), height = size.height),
                )
            }
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (hasChapterTitle) 68.dp else 56.dp)
                .padding(start = 4.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = navigateUp,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = CustomMaterialSymbols.Rounded.Back,
                    contentDescription = stringResource(MR.strings.action_bar_up_description),
                    modifier = Modifier.size(24.dp),
                    tint = VmhTextoClaro,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, end = 8.dp),
            ) {
                if (hasChapterTitle && mangaTitle != null) {
                    Text(
                        text = mangaTitle.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = VmhLomoAzulClaro,
                        letterSpacing = 1.8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 1.dp),
                    )
                }
                Text(
                    text = chapterTitle?.takeIf { it.isNotBlank() } ?: mangaTitle.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = VmhTextoClaro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(),
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (bookmarked) VmhCoralActivo else Color.Transparent)
                    .border(1.dp, VmhLomoAzulClaro, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = onToggleBookmarked,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = if (bookmarked) {
                            CustomMaterialSymbols.Rounded.BookmarkFilled
                        } else {
                            CustomMaterialSymbols.Rounded.Bookmark
                        },
                        contentDescription = stringResource(
                            if (bookmarked) MR.strings.action_remove_bookmark else MR.strings.action_bookmark,
                        ),
                        modifier = Modifier.size(22.dp),
                        tint = if (bookmarked) VmhIconoActivo else VmhLomoAzulClaro,
                    )
                }
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = CustomMaterialSymbols.Rounded.MoreVert,
                        contentDescription = "Menú",
                        modifier = Modifier.size(22.dp),
                        tint = VmhTextoClaro,
                    )
                }

                ReaderOverflowMenu(
                    actions = overflowActions,
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                )
            }
        }
    }
}
