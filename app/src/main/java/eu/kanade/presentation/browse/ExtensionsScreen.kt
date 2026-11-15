package eu.kanade.presentation.browse

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.browse.components.ExtensionIcon
import eu.kanade.presentation.browse.components.label
import eu.kanade.presentation.more.settings.screen.browse.ExtensionStoresScreen
import eu.kanade.presentation.util.rememberRequestPackageInstallsPermissionState
import eu.kanade.tachiyomi.extension.model.Extension
import eu.kanade.tachiyomi.extension.model.InstallStep
import eu.kanade.tachiyomi.ui.browse.extension.ExtensionUiModel
import eu.kanade.tachiyomi.ui.browse.extension.ExtensionsViewModel
import eu.kanade.tachiyomi.util.system.LocaleHelper
import eu.kanade.tachiyomi.util.system.copyToClipboard
import eu.kanade.tachiyomi.util.system.launchRequestPackageInstallsPermission
import mihon.domain.extension.model.ContentWarning
import mihon.domain.extension.model.ExtensionStore
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Close
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Settings
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.material.PullRefresh
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.components.material.topSmallPaddingValues
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyArt
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.EmptyScreenAction
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.plus

@Composable
fun ExtensionScreen(
    state: ExtensionsViewModel.State,
    contentPadding: PaddingValues,
    searchQuery: String?,
    onLongClickItem: (Extension) -> Unit,
    onClickItemCancel: (Extension) -> Unit,
    onInstallExtension: (Extension.Available) -> Unit,
    onUninstallExtension: (Extension.Installed) -> Unit,
    onUpdateExtension: (Extension.Installed) -> Unit,
    onTrustExtension: (Extension.NotLoaded) -> Unit,
    onOpenExtension: (Extension.Loaded) -> Unit,
    onClickUpdateAll: () -> Unit,
    onRefresh: () -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow

    PullRefresh(
        refreshing = state.isRefreshing,
        onRefresh = onRefresh,
        enabled = !state.isLoading,
    ) {
        when {
            state.isLoading -> LoadingScreen(Modifier.padding(contentPadding))
            state.isEmpty -> {
                val msg = if (!searchQuery.isNullOrEmpty()) {
                    MR.strings.no_results_found
                } else {
                    MR.strings.empty_screen
                }
                EmptyScreen(
                    stringRes = msg,
                    modifier = Modifier.padding(contentPadding),
                    art = EmptyArt.SEARCH,
                    actions = listOf(
                        EmptyScreenAction(
                            stringRes = MR.strings.extensionStores,
                            icon = MaterialSymbols.Rounded.Settings,
                            onClick = { navigator.push(ExtensionStoresScreen()) },
                        ),
                    ),
                )
            }
            else -> {
                ExtensionContent(
                    state = state,
                    contentPadding = contentPadding,
                    onLongClickItem = onLongClickItem,
                    onClickItemCancel = onClickItemCancel,
                    onInstallExtension = onInstallExtension,
                    onUninstallExtension = onUninstallExtension,
                    onUpdateExtension = onUpdateExtension,
                    onTrustExtension = onTrustExtension,
                    onOpenExtension = onOpenExtension,
                    onClickUpdateAll = onClickUpdateAll,
                )
            }
        }
    }
}

private enum class ExtSectionTone { Default, Alert }

private enum class ExtSectionKind { Updates, NotLoaded, Installed, Available }

private data class ExtSection(
    val kind: ExtSectionKind,
    val titleRes: StringResource,
    val tone: ExtSectionTone,
    val items: List<ExtensionUiModel.Item>,
)

private fun sectionsOf(
    items: Map<ExtensionUiModel.Header, List<ExtensionUiModel.Item>>,
): List<ExtSection> {
    var available = emptyList<ExtensionUiModel.Item>()
    val sections = mutableListOf<ExtSection>()

    items.forEach { (header, group) ->
        when (header) {
            is ExtensionUiModel.Header.Resource -> {
                val section = when (header.textRes) {
                    MR.strings.ext_updates_pending -> ExtSection(
                        kind = ExtSectionKind.Updates,
                        titleRes = MR.strings.vmh_ext_section_updates,
                        tone = ExtSectionTone.Alert,
                        items = group,
                    )
                    MR.strings.ext_not_loaded -> ExtSection(
                        kind = ExtSectionKind.NotLoaded,
                        titleRes = MR.strings.vmh_ext_section_not_loaded,
                        tone = ExtSectionTone.Alert,
                        items = group,
                    )
                    MR.strings.ext_installed -> ExtSection(
                        kind = ExtSectionKind.Installed,
                        titleRes = MR.strings.vmh_ext_section_installed,
                        tone = ExtSectionTone.Default,
                        items = group,
                    )
                    else -> ExtSection(
                        kind = ExtSectionKind.Installed,
                        titleRes = header.textRes,
                        tone = ExtSectionTone.Default,
                        items = group,
                    )
                }
                sections += section
            }
            is ExtensionUiModel.Header.Text -> available = available + group
        }
    }

    if (available.isNotEmpty()) {
        sections += ExtSection(
            kind = ExtSectionKind.Available,
            titleRes = MR.strings.vmh_ext_section_available,
            tone = ExtSectionTone.Default,
            items = available,
        )
    }
    return sections
}

@Composable
private fun ExtensionContent(
    state: ExtensionsViewModel.State,
    contentPadding: PaddingValues,
    onLongClickItem: (Extension) -> Unit,
    onClickItemCancel: (Extension) -> Unit,
    onInstallExtension: (Extension.Available) -> Unit,
    onUninstallExtension: (Extension.Installed) -> Unit,
    onUpdateExtension: (Extension.Installed) -> Unit,
    onTrustExtension: (Extension.NotLoaded) -> Unit,
    onOpenExtension: (Extension.Loaded) -> Unit,
    onClickUpdateAll: () -> Unit,
) {
    val context = LocalContext.current
    var notLoadedState by remember { mutableStateOf<Extension.NotLoaded?>(null) }
    val installGranted = rememberRequestPackageInstallsPermissionState(initialValue = true)

    val sections = sectionsOf(state.items)
    val updatesCount = sections.firstOrNull { it.kind == ExtSectionKind.Updates }?.items?.size ?: 0

    FastScrollLazyColumn(
        contentPadding = contentPadding + topSmallPaddingValues,
    ) {
        if (!installGranted && state.installer?.requiresSystemPermission == true) {
            item(key = "extension-permissions-warning") {
                VmhPermissionBanner(
                    onClick = { context.launchRequestPackageInstallsPermission() },
                )
            }
        }

        if (updatesCount > 0) {
            item(key = "extension-update-all") {
                VmhUpdateAllButton(count = updatesCount, onClick = onClickUpdateAll)
            }
        }

        sections.forEach { section ->
            item(
                contentType = "header",
                key = "extensionHeader-${section.titleRes}",
            ) {
                VmhSectionHeader(
                    title = stringResource(section.titleRes),
                    count = section.items.size,
                    tone = section.tone,
                )
            }

            items(
                items = section.items,
                contentType = { "item" },
                key = { item -> "extension-${item.extension.pkgName}-${item.extension.name}" },
            ) { item ->
                VmhExtensionCard(
                    item = item,
                    onClickItem = {
                        when (it) {
                            is Extension.Available -> onInstallExtension(it)
                            is Extension.Loaded -> onOpenExtension(it)
                            is Extension.NotLoaded -> notLoadedState = it
                        }
                    },
                    onLongClickItem = onLongClickItem,
                    onClickItemCancel = onClickItemCancel,
                    onClickItemAction = {
                        when (it) {
                            is Extension.Available -> onInstallExtension(it)
                            is Extension.Loaded -> {
                                if (it.hasUpdate) {
                                    onUpdateExtension(it)
                                } else {
                                    onOpenExtension(it)
                                }
                            }
                            is Extension.NotLoaded -> {
                                if (it.hasUpdate) {
                                    onUpdateExtension(it)
                                } else {
                                    notLoadedState = it
                                }
                            }
                        }
                    },
                    onClickItemSecondaryAction = { notLoadedState = it as? Extension.NotLoaded },
                )
            }
        }

        item(key = "extension-bottom-spacer") {
            Spacer(Modifier.height(16.dp))
        }
    }

    notLoadedState?.let { extension ->
        val dismiss = { notLoadedState = null }
        if (extension.reason is Extension.NotLoaded.Reason.Untrusted) {
            ExtensionTrustDialog(
                onClickConfirm = {
                    onTrustExtension(extension)
                    dismiss()
                },
                onClickDismiss = {
                    onUninstallExtension(extension)
                    dismiss()
                },
                onDismissRequest = dismiss,
            )
        } else {
            ExtensionNotLoadedDialog(
                reason = extension.reason,
                onClickUninstall = {
                    onUninstallExtension(extension)
                    dismiss()
                },
                onDismissRequest = dismiss,
            )
        }
    }
}

@Composable
private fun VmhPermissionBanner(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = lerp(colors.surface, colors.secondary, 0.10f),
        border = BorderStroke(1.dp, colors.secondary.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(colors.secondary),
            )
            Text(
                text = stringResource(MR.strings.ext_permission_install_apps_warning),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface,
            )
        }
    }
}

@Composable
private fun VmhUpdateAllButton(count: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(46.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.primary,
        contentColor = colors.onPrimary,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Download,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(MR.strings.vmh_ext_update_all_count, count),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun VmhSectionHeader(
    title: String,
    count: Int,
    tone: ExtSectionTone,
) {
    val colors = MaterialTheme.colorScheme
    val labelColor = if (tone == ExtSectionTone.Alert) colors.secondary else colors.onSurfaceVariant
    val chipColor = if (tone == ExtSectionTone.Alert) colors.secondary else colors.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = labelColor,
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .height(18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(chipColor.copy(alpha = 0.12f))
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = chipColor,
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.outlineVariant.copy(alpha = 0.7f)),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VmhExtensionCard(
    item: ExtensionUiModel.Item,
    onClickItem: (Extension) -> Unit,
    onLongClickItem: (Extension) -> Unit,
    onClickItemCancel: (Extension) -> Unit,
    onClickItemAction: (Extension) -> Unit,
    onClickItemSecondaryAction: (Extension) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val (extension, installStep) = item

    val isUntrusted = extension is Extension.NotLoaded &&
        extension.reason is Extension.NotLoaded.Reason.Untrusted
    val hasUpdate = (extension as? Extension.Installed)?.hasUpdate == true
    val showsAccent = hasUpdate || isUntrusted

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = {
                    when (extension) {
                        is Extension.Available -> onClickItemAction(extension)
                        is Extension.Loaded -> onClickItem(extension)
                        is Extension.NotLoaded -> onClickItemSecondaryAction(extension)
                    }
                },
                onLongClick = { onLongClickItem(extension) },
            ),
        shape = RoundedCornerShape(17.dp),
        color = if (isUntrusted) lerp(colors.surface, colors.secondary, 0.06f) else colors.surface,
        border = BorderStroke(
            1.dp,
            if (isUntrusted) colors.secondary.copy(alpha = 0.35f) else colors.outlineVariant,
        ),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(
                    start = if (showsAccent) 0.dp else 14.dp,
                    top = 10.dp,
                    bottom = 10.dp,
                    end = 10.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showsAccent) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(colors.secondary),
                )
                Spacer(Modifier.width(11.dp))
            }

            ExtensionItemIcon(
                extension = extension,
                installStep = installStep,
                modifier = Modifier.size(42.dp),
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = extension.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                VmhExtensionMeta(item = item, extension = extension)
            }

            Spacer(Modifier.width(8.dp))

            VmhExtensionAction(
                extension = extension,
                installStep = installStep,
                onClickItemCancel = onClickItemCancel,
                onClickItemAction = onClickItemAction,
                onClickItemSecondaryAction = onClickItemSecondaryAction,
            )
        }
    }
}

@Composable
private fun VmhExtensionMeta(
    item: ExtensionUiModel.Item,
    extension: Extension,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    val language = extension.lang
        ?.takeIf { it.isNotEmpty() }
        ?.let { LocaleHelper.getSourceDisplayName(it, context) }
    val store = when (extension) {
        is Extension.Available -> extension.store
        is Extension.Installed -> extension.store
    }
    val updateVersion = item.updateVersionName

    val text = when {
        (extension as? Extension.Installed)?.hasUpdate == true && updateVersion != null ->
            stringResource(MR.strings.vmh_ext_version_change, extension.versionName, updateVersion)
        extension is Extension.Available ->
            listOfNotNull(language, stringResource(MR.strings.vmh_ext_new)).joinToString(" · ")
        else ->
            listOfNotNull(language, extension.versionName, store?.name).joinToString(" · ")
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )

        if (extension.contentWarning == ContentWarning.NSFW) {
            extension.contentWarning.label?.let { label ->
                Surface(
                    color = colors.secondary.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(
                        text = stringResource(label.title),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.secondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }
        }

        if (extension is Extension.Loaded && extension.isObsolete) {
            Text(
                text = stringResource(MR.strings.ext_obsolete),
                style = MaterialTheme.typography.labelSmall,
                color = colors.secondary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ExtensionItemIcon(
    extension: Extension,
    installStep: InstallStep,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(13.dp)),
        contentAlignment = Alignment.Center,
    ) {
        val idle = installStep.isCompleted()
        if (!idle) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        val padding by animateDpAsState(
            targetValue = if (idle) 0.dp else 8.dp,
            label = "iconPadding",
        )
        ExtensionIcon(
            extension = extension,
            modifier = Modifier
                .matchParentSize()
                .padding(padding),
        )
    }
}

@Composable
private fun VmhExtensionAction(
    extension: Extension,
    installStep: InstallStep,
    onClickItemCancel: (Extension) -> Unit,
    onClickItemAction: (Extension) -> Unit,
    onClickItemSecondaryAction: (Extension) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    if (!installStep.isCompleted()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = colors.primary,
            )
            IconButton(onClick = { onClickItemCancel(extension) }) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Close,
                    contentDescription = stringResource(MR.strings.action_cancel),
                    tint = colors.onSurfaceVariant,
                )
            }
        }
        return
    }

    when (extension) {
        is Extension.Available -> {
            VmhStatusPill(
                text = stringResource(MR.strings.ext_install),
                container = Color.Transparent,
                content = colors.primary,
                border = colors.primary.copy(alpha = 0.5f),
                onClick = { onClickItemAction(extension) },
            )
        }
        is Extension.NotLoaded -> {
            if (extension.reason is Extension.NotLoaded.Reason.Untrusted) {
                VmhStatusPill(
                    text = stringResource(MR.strings.ext_trust),
                    container = colors.secondary,
                    content = colors.onSecondary,
                    onClick = { onClickItemSecondaryAction(extension) },
                )
            } else {
                VmhStatusPill(
                    text = stringResource(MR.strings.ext_not_loaded_details),
                    container = Color.Transparent,
                    content = colors.onSurfaceVariant,
                    border = colors.outlineVariant,
                    onClick = { onClickItemSecondaryAction(extension) },
                )
            }
        }
        is Extension.Installed -> {
            if (extension.hasUpdate) {
                VmhStatusPill(
                    text = stringResource(MR.strings.ext_update),
                    container = colors.secondary,
                    content = colors.onSecondary,
                    onClick = { onClickItemAction(extension) },
                )
            } else {
                VmhStatusPill(
                    text = stringResource(MR.strings.vmh_ext_up_to_date),
                    container = colors.tertiary.copy(alpha = 0.12f),
                    content = colors.tertiary,
                    onClick = { onClickItemAction(extension) },
                )
            }
        }
    }
}

@Composable
private fun VmhStatusPill(
    text: String,
    container: Color,
    content: Color,
    border: Color? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(30.dp),
        shape = RoundedCornerShape(15.dp),
        color = container,
        contentColor = content,
        border = border?.let { BorderStroke(1.dp, it) },
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

private val Extension.NotLoaded.Reason.descriptionRes: StringResource
    get() = when (this) {
        is Extension.NotLoaded.Reason.Untrusted -> MR.strings.untrusted_extension_message
        Extension.NotLoaded.Reason.Filtered -> MR.strings.ext_filtered_message
        Extension.NotLoaded.Reason.Unsigned -> MR.strings.ext_unsigned_message
        Extension.NotLoaded.Reason.UnsupportedLibVersion -> MR.strings.ext_unsupported_message
        Extension.NotLoaded.Reason.Malformed -> MR.strings.ext_malformed_message
        is Extension.NotLoaded.Reason.Failed -> MR.strings.ext_load_failed_message
    }

@Composable
private fun ExtensionNotLoadedDialog(
    reason: Extension.NotLoaded.Reason,
    onClickUninstall: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(MR.strings.ext_not_loaded_dialog))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small)) {
                Text(text = stringResource(reason.descriptionRes))

                if (reason is Extension.NotLoaded.Reason.Failed) {
                    Text(
                        text = reason.message,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    val context = LocalContext.current
                    TextButton(
                        onClick = {
                            context.copyToClipboard(
                                label = context.stringResource(MR.strings.ext_copy_stacktrace),
                                content = reason.stackTrace,
                            )
                        },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(text = stringResource(MR.strings.ext_copy_stacktrace))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onClickUninstall) {
                Text(text = stringResource(MR.strings.ext_uninstall))
            }
        },
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun ExtensionTrustDialog(
    onClickConfirm: () -> Unit,
    onClickDismiss: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(MR.strings.untrusted_extension))
        },
        text = {
            Text(text = stringResource(MR.strings.untrusted_extension_message))
        },
        confirmButton = {
            TextButton(onClick = onClickConfirm) {
                Text(text = stringResource(MR.strings.ext_trust))
            }
        },
        dismissButton = {
            TextButton(onClick = onClickDismiss) {
                Text(text = stringResource(MR.strings.ext_uninstall))
            }
        },
        onDismissRequest = onDismissRequest,
    )
}
