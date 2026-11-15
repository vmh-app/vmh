package eu.kanade.presentation.manga.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.DownloadDropdownMenu
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.manga.DownloadAction
import eu.kanade.tachiyomi.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mihon.icons.custommaterialsymbols.CustomMaterialSymbols
import mihon.icons.custommaterialsymbols.rounded.DonePrevious
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.BookmarkAdd
import mihon.icons.materialsymbols.rounded.BookmarkRemove
import mihon.icons.materialsymbols.rounded.Delete
import mihon.icons.materialsymbols.rounded.DoneAll
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.RemoveDone
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import kotlin.time.Duration.Companion.seconds

@Composable
fun MangaBottomActionMenu(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onBookmarkClicked: (() -> Unit)? = null,
    onRemoveBookmarkClicked: (() -> Unit)? = null,
    onMarkAsReadClicked: (() -> Unit)? = null,
    onMarkAsUnreadClicked: (() -> Unit)? = null,
    onMarkPreviousAsReadClicked: (() -> Unit)? = null,
    onDownloadClicked: (() -> Unit)? = null,
    onDeleteClicked: (() -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(expandFrom = Alignment.Bottom),
        exit = shrinkVertically(shrinkTowards = Alignment.Bottom),
    ) {
        val scope = rememberCoroutineScope()
        val colors = MaterialTheme.colorScheme
        Surface(
            modifier = modifier
                .navigationBarsPadding()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
            shape = RoundedCornerShape(26.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outlineVariant),
            shadowElevation = 6.dp,
        ) {
            val haptic = LocalHapticFeedback.current
            val confirm = remember { mutableStateListOf(false, false, false, false, false, false, false) }
            var resetJob by remember { mutableStateOf<Job?>(null) }
            val onLongClickItem: (Int) -> Unit = { toConfirmIndex ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                confirm.indices.forEach { i -> confirm[i] = i == toConfirmIndex }
                resetJob?.cancel()
                resetJob = scope.launch {
                    delay(1.seconds)
                    if (isActive) confirm[toConfirmIndex] = false
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            ) {
                if (onBookmarkClicked != null) {
                    Button(
                        title = stringResource(MR.strings.action_bookmark),
                        icon = MaterialSymbols.Rounded.BookmarkAdd,
                        toConfirm = confirm[0],
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onLongClick = { onLongClickItem(0) },
                        onClick = onBookmarkClicked,
                    )
                }
                if (onRemoveBookmarkClicked != null) {
                    Button(
                        title = stringResource(MR.strings.action_remove_bookmark),
                        icon = MaterialSymbols.Rounded.BookmarkRemove,
                        toConfirm = confirm[1],
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onLongClick = { onLongClickItem(1) },
                        onClick = onRemoveBookmarkClicked,
                    )
                }
                if (onMarkAsReadClicked != null) {
                    Button(
                        title = stringResource(MR.strings.action_mark_as_read),
                        icon = MaterialSymbols.Rounded.DoneAll,
                        toConfirm = confirm[2],
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onLongClick = { onLongClickItem(2) },
                        onClick = onMarkAsReadClicked,
                    )
                }
                if (onMarkAsUnreadClicked != null) {
                    Button(
                        title = stringResource(MR.strings.action_mark_as_unread),
                        icon = MaterialSymbols.Rounded.RemoveDone,
                        toConfirm = confirm[3],
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onLongClick = { onLongClickItem(3) },
                        onClick = onMarkAsUnreadClicked,
                    )
                }
                if (onMarkPreviousAsReadClicked != null) {
                    Button(
                        title = stringResource(MR.strings.action_mark_previous_as_read),
                        icon = CustomMaterialSymbols.Rounded.DonePrevious,
                        toConfirm = confirm[4],
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onLongClick = { onLongClickItem(4) },
                        onClick = onMarkPreviousAsReadClicked,
                    )
                }
                if (onDownloadClicked != null) {
                    Button(
                        title = stringResource(MR.strings.action_download),
                        icon = MaterialSymbols.Rounded.Download,
                        toConfirm = confirm[5],
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onLongClick = { onLongClickItem(5) },
                        onClick = onDownloadClicked,
                    )
                }
                if (onDeleteClicked != null) {
                    Button(
                        title = stringResource(MR.strings.action_delete),
                        icon = MaterialSymbols.Rounded.Delete,
                        toConfirm = confirm[6],
                        tint = MaterialTheme.colorScheme.error,
                        onLongClick = { onLongClickItem(6) },
                        onClick = onDeleteClicked,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.Button(
    title: String,
    icon: ImageVector,
    toConfirm: Boolean,
    tint: Color,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    content: (@Composable () -> Unit)? = null,
) {
    val animatedWeight by animateFloatAsState(
        targetValue = if (toConfirm) 2f else 1f,
        label = "weight",
    )
    Box(
        modifier = Modifier
            .height(48.dp)
            .weight(animatedWeight)
            .combinedClickable(
                interactionSource = null,
                indication = ripple(bounded = false),
                onLongClick = onLongClick,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(20.dp),
                )
            }
            AnimatedVisibility(
                visible = toConfirm,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
            ) {
                Text(
                    text = title,
                    overflow = TextOverflow.Visible,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        content?.invoke()
    }
}

@Composable
fun LibraryBottomActionMenu(
    visible: Boolean,
    onChangeCategoryClicked: () -> Unit,
    onMarkAsReadClicked: () -> Unit,
    onMarkAsUnreadClicked: () -> Unit,
    onDownloadClicked: ((DownloadAction) -> Unit)?,
    onDeleteClicked: () -> Unit,
    onMigrateClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(animationSpec = tween(delayMillis = 300)),
        exit = shrinkVertically(animationSpec = tween()),
    ) {
        val scope = rememberCoroutineScope()
        val colors = MaterialTheme.colorScheme
        Row(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.outlineVariant),
                shadowElevation = 10.dp,
            ) {
                val haptic = LocalHapticFeedback.current
                val confirm = remember { mutableStateListOf(false, false, false, false, false, false) }
                var resetJob by remember { mutableStateOf<Job?>(null) }
                val onLongClickItem: (Int) -> Unit = { toConfirmIndex ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    confirm.indices.forEach { i -> confirm[i] = i == toConfirmIndex }
                    resetJob?.cancel()
                    resetJob = scope.launch {
                        delay(1.seconds)
                        if (isActive) confirm[toConfirmIndex] = false
                    }
                }
                val itemOverflow = onDownloadClicked != null
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LibraryAction(
                        title = stringResource(MR.strings.action_move_category),
                        icon = painterResource(R.drawable.ic_vmh_move_category),
                        toConfirm = confirm[0],
                        onLongClick = { onLongClickItem(0) },
                        onClick = onChangeCategoryClicked,
                    )
                    LibraryAction(
                        title = stringResource(MR.strings.action_mark_as_read),
                        icon = painterResource(R.drawable.ic_vmh_mark_read),
                        toConfirm = confirm[1],
                        onLongClick = { onLongClickItem(1) },
                        onClick = onMarkAsReadClicked,
                    )
                    LibraryAction(
                        title = stringResource(MR.strings.action_mark_as_unread),
                        icon = painterResource(R.drawable.ic_vmh_mark_unread),
                        toConfirm = confirm[2],
                        onLongClick = { onLongClickItem(2) },
                        onClick = onMarkAsUnreadClicked,
                    )
                    if (onDownloadClicked != null) {
                        var downloadExpanded by remember { mutableStateOf(false) }
                        LibraryAction(
                            title = stringResource(MR.strings.action_download),
                            icon = painterResource(R.drawable.ic_vmh_download),
                            toConfirm = confirm[3],
                            onLongClick = { onLongClickItem(3) },
                            onClick = { downloadExpanded = !downloadExpanded },
                        ) {
                            DownloadDropdownMenu(
                                expanded = downloadExpanded,
                                onDismissRequest = { downloadExpanded = false },
                                onDownloadClicked = onDownloadClicked,
                                offset = BottomBarMenuDpOffset,
                            )
                        }
                    }
                    if (!itemOverflow) {
                        LibraryAction(
                            title = stringResource(MR.strings.label_migration),
                            icon = painterResource(R.drawable.ic_vmh_migrate),
                            toConfirm = confirm[4],
                            onLongClick = { onLongClickItem(4) },
                            onClick = onMigrateClicked,
                        )
                        LibraryAction(
                            title = stringResource(MR.strings.action_delete),
                            icon = painterResource(R.drawable.ic_vmh_delete),
                            toConfirm = confirm[5],
                            onLongClick = { onLongClickItem(5) },
                            onClick = onDeleteClicked,
                            destructive = true,
                        )
                    } else {
                        var overflowMenuOpen by remember { mutableStateOf(false) }
                        LibraryAction(
                            title = stringResource(MR.strings.label_more),
                            icon = painterResource(R.drawable.ic_vmh_more),
                            toConfirm = false,
                            onLongClick = {},
                            onClick = { overflowMenuOpen = true },
                        ) {
                            DropdownMenu(
                                expanded = overflowMenuOpen,
                                onDismissRequest = { overflowMenuOpen = false },
                                offset = BottomBarMenuDpOffset,
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(MR.strings.label_migration)) },
                                    onClick = onMigrateClicked,
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_vmh_migrate),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = colors.onSurfaceVariant,
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(MR.strings.action_delete),
                                            color = colors.error,
                                        )
                                    },
                                    onClick = onDeleteClicked,
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_vmh_delete),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = colors.error,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.LibraryAction(
    title: String,
    icon: Painter,
    toConfirm: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    destructive: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val accent = if (destructive) colors.error else colors.primary
    val animatedWeight by animateFloatAsState(
        targetValue = if (toConfirm) 2f else 1f,
        label = "weight",
    )
    Column(
        modifier = Modifier.weight(animatedWeight),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .combinedClickable(
                    interactionSource = null,
                    indication = ripple(bounded = false),
                    onLongClick = onLongClick,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = icon,
                    contentDescription = title,
                    modifier = Modifier.size(20.dp),
                    tint = accent,
                )
            }
            trailing?.invoke()
        }
        AnimatedVisibility(
            visible = toConfirm,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            Text(
                text = title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
        }
    }
}

private val BottomBarMenuDpOffset = DpOffset(0.dp, 0.dp)
