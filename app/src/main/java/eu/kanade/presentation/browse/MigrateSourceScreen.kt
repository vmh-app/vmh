package eu.kanade.presentation.browse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.browse.components.SourceIcon
import eu.kanade.presentation.components.VmhContinueBar
import eu.kanade.tachiyomi.ui.browse.migration.sources.MigrateSourceViewModel
import eu.kanade.tachiyomi.util.system.copyToClipboard
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Check
import tachiyomi.domain.source.model.Source
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.VmhLabel
import tachiyomi.presentation.core.components.material.topSmallPaddingValues
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.plus

@Composable
fun MigrateSourceScreen(
    state: MigrateSourceViewModel.State,
    contentPadding: PaddingValues,
    onStartMigration: (origin: Source, destination: Source) -> Unit,
) {
    when {
        state.isLoading -> LoadingScreen(Modifier.padding(contentPadding))
        state.isEmpty -> EmptyScreen(
            stringRes = MR.strings.information_empty_library,
            modifier = Modifier.padding(contentPadding),
        )
        else -> MigrateSourceContent(
            items = state.items,
            contentPadding = contentPadding,
            onStartMigration = onStartMigration,
        )
    }
}

@Composable
private fun MigrateSourceContent(
    items: List<Pair<Source, Long>>,
    contentPadding: PaddingValues,
    onStartMigration: (origin: Source, destination: Source) -> Unit,
) {
    val context = LocalContext.current
    var originId by rememberSaveable { mutableStateOf<Long?>(null) }
    var destinationId by rememberSaveable { mutableStateOf<Long?>(null) }

    val origin = items.firstOrNull { it.first.id == originId }
    val destinations = origin
        ?.let { entry -> items.filter { it.first.id != entry.first.id && !it.first.isStub } }
        .orEmpty()
    val destination = destinations.firstOrNull { it.first.id == destinationId }

    val copySourceId = { source: Source ->
        val sourceId = source.id.toString()
        context.copyToClipboard(sourceId, sourceId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FastScrollLazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = contentPadding + topSmallPaddingValues,
        ) {
            item(key = "migration-steps") {
                VmhMigrationSteps(step = if (origin == null) 1 else 2)
            }

            item(key = "migration-from-label") {
                VmhSectionLabel(stringResource(MR.strings.vmh_migration_from))
            }

            if (origin == null) {
                items(
                    items = items,
                    key = { "origin-${it.first.id}" },
                ) { (source, count) ->
                    VmhMigrationCard(
                        source = source,
                        count = count,
                        onClick = if (source.isStub) {
                            null
                        } else {
                            {
                                originId = source.id
                                destinationId = null
                            }
                        },
                        onLongClick = { copySourceId(source) },
                    ) {
                        if (!source.isStub) {
                            VmhOutlinePill(
                                text = stringResource(MR.strings.vmh_migration_choose),
                                onClick = {
                                    originId = source.id
                                    destinationId = null
                                },
                            )
                        }
                    }
                }
            } else {
                item(key = "migration-origin-card") {
                    VmhMigrationCard(
                        source = origin.first,
                        count = origin.second,
                        onClick = null,
                        onLongClick = { copySourceId(origin.first) },
                    ) {
                        VmhOutlinePill(
                            text = stringResource(MR.strings.vmh_migration_change),
                            onClick = {
                                originId = null
                                destinationId = null
                            },
                        )
                    }
                }

                item(key = "migration-to-label") {
                    VmhSectionLabel(stringResource(MR.strings.vmh_migration_to))
                }

                if (destinations.isEmpty()) {
                    item(key = "migration-no-destinations") {
                        Text(
                            text = stringResource(MR.strings.vmh_migration_no_other_sources),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(
                        items = destinations,
                        key = { "destination-${it.first.id}" },
                    ) { (source, count) ->
                        VmhMigrationCard(
                            source = source,
                            count = count,
                            selected = source.id == destinationId,
                            onClick = { destinationId = source.id },
                            onLongClick = { copySourceId(source) },
                        ) {
                            VmhRadio(selected = source.id == destinationId)
                        }
                    }

                    item(key = "migration-keeps-note") {
                        Text(
                            text = stringResource(MR.strings.vmh_migration_keeps),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 40.dp, vertical = 18.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            item(key = "migration-bottom-spacer") {
                Spacer(Modifier.height(8.dp))
            }
        }

        if (origin != null) {
            VmhContinueBar(
                enabled = destination != null,
                onClick = { destination?.let { onStartMigration(origin.first, it.first) } },
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    .padding(bottom = contentPadding.calculateBottomPadding() + 12.dp),
            )
        }
    }
}

private enum class VmhStepState { Done, Active, Pending }

@Composable
private fun VmhMigrationSteps(step: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VmhMigrationStep(
            index = 1,
            label = stringResource(MR.strings.vmh_migration_step_origin),
            state = if (step == 1) VmhStepState.Active else VmhStepState.Done,
        )
        VmhStepConnector(reached = step > 1)
        VmhMigrationStep(
            index = 2,
            label = stringResource(MR.strings.vmh_migration_step_destination),
            state = if (step == 1) VmhStepState.Pending else VmhStepState.Active,
        )
        VmhStepConnector(reached = step > 2)
        VmhMigrationStep(
            index = 3,
            label = stringResource(MR.strings.vmh_migration_step_review),
            state = VmhStepState.Pending,
        )
    }
}

@Composable
private fun VmhMigrationStep(
    index: Int,
    label: String,
    state: VmhStepState,
) {
    val colors = MaterialTheme.colorScheme
    val circleColor = if (state == VmhStepState.Done) colors.primary else Color.Transparent
    val borderColor = if (state == VmhStepState.Pending) colors.outlineVariant else colors.primary

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(circleColor)
                .border(1.5.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (state == VmhStepState.Done) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Check,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(14.dp),
                )
            } else {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (state == VmhStepState.Active) colors.primary else colors.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (state == VmhStepState.Pending) FontWeight.Normal else FontWeight.SemiBold,
            color = when (state) {
                VmhStepState.Done -> colors.primary
                VmhStepState.Active -> colors.onSurface
                VmhStepState.Pending -> colors.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun RowScope.VmhStepConnector(reached: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 8.dp)
            .height(1.5.dp)
            .background(if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun VmhSectionLabel(text: String) {
    VmhLabel(
        text = text,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VmhMigrationCard(
    source: Source,
    count: Long,
    selected: Boolean = false,
    onClick: (() -> Unit)?,
    onLongClick: () -> Unit,
    trailing: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                enabled = onClick != null,
                onClick = { onClick?.invoke() },
                onLongClick = onLongClick,
            ),
        shape = RoundedCornerShape(17.dp),
        color = colors.surface,
        border = BorderStroke(
            1.dp,
            if (selected) colors.primary.copy(alpha = 0.55f) else colors.outlineVariant,
        ),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(
                    start = if (selected) 0.dp else 14.dp,
                    top = 10.dp,
                    bottom = 10.dp,
                    end = 10.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(colors.primary),
                )
                Spacer(Modifier.width(11.dp))
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp)),
            ) {
                SourceIcon(source = source, modifier = Modifier.matchParentSize())
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = source.name.ifBlank { source.id.toString() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (source.isStub) {
                        stringResource(MR.strings.not_installed)
                    } else {
                        pluralStringResource(
                            MR.plurals.vmh_migration_titles_in_library,
                            count.toInt(),
                            count.toInt(),
                        )
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (source.isStub) colors.error else colors.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(8.dp))

            trailing()
        }
    }
}

@Composable
private fun VmhOutlinePill(text: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        modifier = Modifier.height(30.dp),
        shape = RoundedCornerShape(15.dp),
        color = Color.Transparent,
        contentColor = colors.primary,
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun VmhRadio(selected: Boolean) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(2.dp, if (selected) colors.primary else colors.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
            )
        }
    }
}
