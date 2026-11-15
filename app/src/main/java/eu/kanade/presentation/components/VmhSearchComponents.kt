package eu.kanade.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ArrowBack
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.Close
import mihon.icons.materialsymbols.rounded.FilterList
import mihon.icons.materialsymbols.rounded.Search
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.vmhChipBorder
import tachiyomi.presentation.core.components.vmhChipFill
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.runOnEnterKeyPressed

@Composable
fun VmhSearchField(
    query: String,
    onQueryChange: (String?) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderRes: StringResource = MR.strings.vmh_search_hint,
    autoFocus: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val fieldState = rememberTextFieldState(query)

    LaunchedEffect(query) {
        if (query != fieldState.text.toString()) {
            fieldState.setTextAndPlaceCursorAtEnd(query)
        }
    }
    LaunchedEffect(fieldState) {
        snapshotFlow { fieldState.text.toString() }
            .drop(1)
            .collect { onQueryChange(it) }
    }
    if (autoFocus) {
        LaunchedEffect(Unit) {
            delay(150)
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    val submit: () -> Unit = {
        val current = fieldState.text.toString()
        if (current.isNotBlank()) {
            onSearch(current)
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = colors.onSurfaceVariant,
            )
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                state = fieldState,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .runOnEnterKeyPressed(action = submit),
                textStyle = MaterialTheme.typography.titleSmall.copy(color = colors.onSurface),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                onKeyboardAction = { submit() },
                lineLimits = TextFieldLineLimits.SingleLine,
                cursorBrush = SolidColor(colors.primary),
                interactionSource = interactionSource,
                decorator = TextFieldDefaults.decorator(
                    state = fieldState,
                    enabled = true,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    outputTransformation = null,
                    interactionSource = interactionSource,
                    placeholder = {
                        Text(
                            text = stringResource(placeholderRes),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    },
                    container = {},
                ),
            )
            if (query.isNotEmpty()) {
                Spacer(Modifier.width(4.dp))
                Surface(
                    onClick = {
                        onQueryChange("")
                        focusRequester.requestFocus()
                    },
                    modifier = Modifier.size(30.dp),
                    shape = CircleShape,
                    color = colors.surfaceVariant,
                    contentColor = colors.onSurfaceVariant,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Close,
                            contentDescription = stringResource(MR.strings.action_reset),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VmhFilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = MaterialSymbols.Rounded.FilterList,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        modifier = modifier.height(30.dp),
        shape = RoundedCornerShape(15.dp),
        color = vmhChipFill(selected, colors.surface),
        contentColor = if (selected) colors.primary else colors.onSurfaceVariant,
        border = BorderStroke(
            1.dp,
            vmhChipBorder(selected),
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .height(30.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
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
fun VmhBackCircle(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = colors.surface,
        contentColor = colors.onSurfaceVariant,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = MaterialSymbols.AutoMirroredRounded.ArrowBack,
                contentDescription = stringResource(MR.strings.action_bar_up_description),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun VmhCheckRow(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(7.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(shape)
                .background(if (checked) colors.primary else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (checked) colors.primary else colors.outlineVariant,
                    shape = shape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = colors.onPrimary,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
        )
    }
}

@Composable
fun VmhCircleAction(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activo: Boolean = false,
    tint: Color? = null,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = vmhChipFill(activo, colors.surface),
        contentColor = if (activo) colors.primary else colors.onSurfaceVariant,
        border = BorderStroke(
            1.dp,
            vmhChipBorder(activo),
        ),
        shadowElevation = 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = tint ?: LocalContentColor.current,
            )
        }
    }
}
