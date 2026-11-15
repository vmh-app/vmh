package eu.kanade.presentation.library.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.manga.components.MangaCover
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.Refresh
import mihon.icons.materialsymbols.roundedfilled.PlayArrow
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.BadgeGroup
import tachiyomi.presentation.core.components.VmhCardShape
import tachiyomi.presentation.core.components.VmhPillShape
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.domain.manga.model.MangaCover as MangaCoverModel

/**
 * Banda superior de la biblioteca: cuántas series hay, cuántos capítulos siguen sin leer y un
 * acceso directo para seguir leyendo. Reemplaza la grilla que empezaba pegada a la cabecera.
 */
@Composable
internal fun LibrarySummaryBand(
    seriesCount: Int,
    unreadCount: Long,
    onResume: (() -> Unit)?,
    onRefresh: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
        shape = VmhCardShape,
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.7f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryStat(
                value = seriesCount.toString(),
                label = stringResource(MR.strings.vmh_eyebrow_series),
            )
            Spacer(Modifier.width(20.dp))
            SummaryStat(
                value = unreadCount.toString(),
                label = stringResource(MR.strings.unread),
                highlight = unreadCount > 0,
            )
            Spacer(Modifier.weight(1f))
            if (onResume != null) {
                ResumePill(onClick = onResume)
            } else {
                RefreshButton(onClick = onRefresh)
            }
        }
    }
}

@Composable
private fun SummaryStat(
    value: String,
    label: String,
    highlight: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme

    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (highlight) colors.primary else colors.onSurface,
            maxLines = 1,
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ResumePill(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = VmhPillShape,
        color = colors.primary,
        contentColor = colors.onPrimary,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MaterialSymbols.RoundedFilled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(MR.strings.action_resume),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RefreshButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
        contentColor = colors.primary,
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Refresh,
                contentDescription = stringResource(MR.strings.action_update_library),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Tarjeta de estante: portada, título y barra de avance dentro de una tarjeta con el radio de
 * VMH. Sustituye la portada suelta con el título colgando que traía Mihon.
 */
@Composable
internal fun LibraryShelfCard(
    coverData: MangaCoverModel,
    title: String,
    progress: Float?,
    isSelected: Boolean,
    downloadCount: Int,
    unreadCount: Long,
    isLocal: Boolean,
    sourceLanguage: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickContinueReading: (() -> Unit)?,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = VmhCardShape,
        color = colors.surfaceContainer,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.7f),
        ),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box {
                MangaCover.Book(
                    data = coverData,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(colors.primary.copy(alpha = 0.18f)),
                    )
                }

                BadgeGroup(
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.TopStart),
                ) {
                    DownloadsBadge(count = downloadCount)
                    UnreadBadge(count = unreadCount)
                }

                BadgeGroup(
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.TopEnd),
                ) {
                    LanguageBadge(isLocal = isLocal, sourceLanguage = sourceLanguage)
                }

                if (onClickContinueReading != null) {
                    FilledIconButton(
                        onClick = onClickContinueReading,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                        ),
                        modifier = Modifier
                            .padding(6.dp)
                            .size(32.dp)
                            .align(Alignment.BottomEnd),
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.RoundedFilled.PlayArrow,
                            contentDescription = stringResource(MR.strings.action_resume),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.BottomStart)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(colors.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = colors.onPrimary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 2.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(8.dp))

            ProgressBar(progress = progress)
        }
    }
}

@Composable
private fun ProgressBar(progress: Float?) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(2.dp)
    val value = progress?.coerceIn(0f, 1f) ?: 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(shape)
            .background(colors.outlineVariant.copy(alpha = 0.4f)),
    ) {
        if (value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value)
                    .fillMaxHeight()
                    .background(colors.primary),
            )
        }
    }
}
