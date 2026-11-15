package eu.kanade.presentation.reader.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalSlider
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.reader.appbars.VmhLectorPalette
import eu.kanade.presentation.reader.appbars.rememberVmhLectorPalette
import eu.kanade.presentation.util.isTabletUi
import mihon.icons.custommaterialsymbols.CustomMaterialSymbols
import mihon.icons.custommaterialsymbols.rounded.ChapterNext
import mihon.icons.custommaterialsymbols.rounded.ChapterPrev
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.VmhVerticalSliderThumb
import tachiyomi.presentation.core.components.material.VmhVerticalSliderTrack
import tachiyomi.presentation.core.i18n.stringResource
import kotlin.math.roundToInt

enum class ChapterNavigatorType {
    HORIZONTAL_LTR,
    HORIZONTAL_RTL,
    VERTICAL_LEFT,
    VERTICAL_RIGHT,
    ;

    fun isHorizontal() = this in setOf(HORIZONTAL_LTR, HORIZONTAL_RTL)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterNavigator(
    type: ChapterNavigatorType,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    currentPage: Int,
    totalPages: Int,
    onPageIndexChange: (Int) -> Unit,
    onPageIndexChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val state = if (totalPages > 1) {
        key(totalPages) {
            rememberSliderState(
                value = currentPage.toFloat(),
                steps = totalPages - 2,
                trackRange =
                1f..totalPages.toFloat(),
            )
        }.also { it.value = currentPage.toFloat() }
    } else {
        null
    }

    val interactionSource = remember { MutableInteractionSource() }
    val sliderDragged by interactionSource.collectIsDraggedAsState()
    LaunchedEffect(currentPage) {
        if (sliderDragged) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val isTabletUi = isTabletUi()
    val mainAxisPadding = if (isTabletUi) 24.dp else 8.dp
    val palette = rememberVmhLectorPalette()
    val onPageChange: (Int) -> Unit = { onPageIndexChange(it - 1) }

    if (type.isHorizontal()) {
        HorizontalChapterNavigator(
            isRtl = type == ChapterNavigatorType.HORIZONTAL_RTL,
            state = state,
            onNextChapter = onNextChapter,
            enabledNext = enabledNext,
            onPreviousChapter = onPreviousChapter,
            enabledPrevious = enabledPrevious,
            currentPage = currentPage,
            totalPages = totalPages,
            onPageChange = onPageChange,
            onPageChangeFinished = onPageIndexChangeFinished,
            interactionSource = interactionSource,
            modifier = modifier,
        )
    } else {
        VerticalChapterNavigator(
            state = state,
            onNextChapter = onNextChapter,
            enabledNext = enabledNext,
            onPreviousChapter = onPreviousChapter,
            enabledPrevious = enabledPrevious,
            currentPage = currentPage,
            totalPages = totalPages,
            onPageChange = onPageChange,
            onPageChangeFinished = onPageIndexChangeFinished,
            interactionSource = interactionSource,
            mainAxisPadding = mainAxisPadding,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalChapterNavigator(
    isRtl: Boolean,
    state: SliderState?,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    onPageChangeFinished: () -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val palette = rememberVmhLectorPalette()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                enabled = if (isRtl) enabledNext else enabledPrevious,
                onClick = if (isRtl) onNextChapter else onPreviousChapter,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = CustomMaterialSymbols.Rounded.ChapterPrev,
                    contentDescription = stringResource(
                        if (isRtl) MR.strings.action_next_chapter else MR.strings.action_previous_chapter,
                    ),
                    modifier = Modifier.size(20.dp),
                    tint = palette.acento.copy(
                        alpha = if (if (isRtl) enabledNext else enabledPrevious) 1f else 0.38f,
                    ),
                )
            }

            if (state != null) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Box(
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        val fraction = if (totalPages >
                            1
                        ) {
                            ((state.value - 1f) / (totalPages - 1f)).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().height(
                                1.dp,
                            ).background(palette.progresoPista),
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(
                                fraction,
                            ).height(3.dp).background(palette.progresoActivo),
                        )

                        Slider(
                            state = state,
                            modifier = Modifier.fillMaxWidth(),
                            onValueChange = { onPageChange(it.roundToInt()) },
                            onValueChangeFinished = onPageChangeFinished,
                            interactionSource = interactionSource,
                            thumb = {
                                Box(
                                    modifier = Modifier.size(
                                        width = 4.dp,
                                        height = 16.dp,
                                    ).background(
                                        color = palette.progresoActivo,
                                        shape = RoundedCornerShape(2.dp),
                                    ),
                                )
                            },
                            track = { Spacer(modifier = Modifier.fillMaxWidth()) },
                        )
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            IconButton(
                enabled = if (isRtl) enabledPrevious else enabledNext,
                onClick = if (isRtl) onPreviousChapter else onNextChapter,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = CustomMaterialSymbols.Rounded.ChapterNext,
                    contentDescription = stringResource(
                        if (isRtl) MR.strings.action_previous_chapter else MR.strings.action_next_chapter,
                    ),
                    modifier = Modifier.size(20.dp),
                    tint = palette.acento.copy(
                        alpha = if (if (isRtl) enabledPrevious else enabledNext) 1f else 0.38f,
                    ),
                )
            }

            Text(
                text = "$currentPage / $totalPages",
                style = MaterialTheme.typography.labelSmall,
                color = palette.texto,
                textAlign = TextAlign.End,
                modifier = Modifier.width(44.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalChapterNavigator(
    state: SliderState?,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    onPageChangeFinished: () -> Unit,
    interactionSource: MutableInteractionSource,
    mainAxisPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val palette = rememberVmhLectorPalette()
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(NAVIGATOR_WIDTH)
            .padding(vertical = mainAxisPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NavigatorArrow(
            icon = CustomMaterialSymbols.Rounded.ChapterPrev,
            description = stringResource(MR.strings.action_previous_chapter),
            enabled = enabledPrevious,
            onClick = onPreviousChapter,
            palette = palette,
        )

        if (state != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp)
                    .clip(NavigatorShape)
                    .background(palette.superficie)
                    .border(1.dp, palette.separador, NavigatorShape)
                    .padding(horizontal = 6.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = currentPage.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.texto,
                )

                VerticalSlider(
                    state = state,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    onValueChange = { onPageChange(it.roundToInt()) },
                    onValueChangeFinished = onPageChangeFinished,
                    interactionSource = interactionSource,
                    thumb = { VmhVerticalSliderThumb(color = palette.progresoActivo) },
                    track = { sliderState ->
                        VmhVerticalSliderTrack(
                            state = sliderState,
                            valueRange = 1f..totalPages.toFloat(),
                            activeColor = palette.progresoActivo,
                            inactiveColor = palette.progresoPista,
                        )
                    },
                )

                Text(
                    text = totalPages.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.acento,
                )
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        NavigatorArrow(
            icon = CustomMaterialSymbols.Rounded.ChapterNext,
            description = stringResource(MR.strings.action_next_chapter),
            enabled = enabledNext,
            onClick = onNextChapter,
            palette = palette,
        )
    }
}

private val NavigatorShape = RoundedCornerShape(22.dp)

private val NAVIGATOR_WIDTH = 56.dp

@Composable
private fun NavigatorArrow(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    palette: VmhLectorPalette,
) {
    // Sobre la superficie de la capsula y no sobre la pagina: en paginas oscuras la flecha
    // azul suelta se perdia y el cambio de capitulo quedaba invisible.
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(38.dp),
        shape = CircleShape,
        color = palette.superficie,
        border = BorderStroke(1.dp, palette.separador),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(90f),
                tint = palette.acento.copy(alpha = if (enabled) 1f else 0.38f),
            )
        }
    }
}
