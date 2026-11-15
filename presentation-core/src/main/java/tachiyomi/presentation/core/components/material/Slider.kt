package tachiyomi.presentation.core.components.material

import androidx.annotation.IntRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun Slider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: IntProgression = 0..1,
    @IntRange(from = 0) steps: Int = with(valueRange) { (last - first) - 1 },
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    thumb: @Composable (SliderState) -> Unit = { VmhSliderThumb(colors = colors) },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        VmhSliderTrack(
            state = sliderState,
            steps = steps,
            valueRange = with(valueRange) { first.toFloat()..last.toFloat() },
            colors = colors,
        )
    },
) {
    val state = key(steps, valueRange) {
        rememberSliderState(
            value = value.toFloat(),
            steps = steps,
            trackRange = with(valueRange) { first.toFloat()..last.toFloat() },
        )
    }
    state.value = value.toFloat()
    Slider(
        state = state,
        modifier = modifier,
        enabled = enabled,
        onValueChange = { onValueChange(it.roundToInt()) },
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        thumb = thumb,
        track = track,
    )
}

@Composable
fun VmhSliderThumb(
    colors: SliderColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 4.dp, height = 18.dp)
            .background(color = colors.thumbColor, shape = vmhThumbShape),
    )
}

@Composable
fun VmhSliderTrack(
    state: SliderState,
    steps: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    colors: SliderColors,
    modifier: Modifier = Modifier,
) {
    val first = valueRange.start
    val span = (valueRange.endInclusive - first).coerceAtLeast(1f)
    val fraction = ((state.value - first) / span).coerceIn(0f, 1f)
    val tickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape)
                .background(colors.inactiveTrackColor),
        )
        if (steps in 2..12) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
            ) {
                val positions = steps + 2
                for (index in 1 until positions - 1) {
                    drawCircle(
                        color = tickColor,
                        radius = 1.3.dp.toPx(),
                        center = Offset(size.width * index / (positions - 1), size.height / 2),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(5.dp)
                .clip(CircleShape)
                .background(colors.activeTrackColor),
        )
    }
}

private val vmhThumbShape = RoundedCornerShape(2.dp)

@Composable
fun VmhVerticalSliderThumb(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 18.dp, height = 4.dp)
            .background(color = color, shape = vmhThumbShape),
    )
}

@Composable
fun VmhVerticalSliderTrack(
    state: SliderState,
    valueRange: ClosedFloatingPointRange<Float>,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
) {
    val first = valueRange.start
    val span = (valueRange.endInclusive - first).coerceAtLeast(1f)
    val fraction = ((state.value - first) / span).coerceIn(0f, 1f)
    Canvas(modifier = modifier.fillMaxSize()) {
        val barWidth = 5.dp.toPx()
        val left = (size.width - barWidth) / 2f
        val radius = CornerRadius(barWidth / 2f)
        drawRoundRect(
            color = inactiveColor,
            topLeft = Offset(left, 0f),
            size = Size(barWidth, size.height),
            cornerRadius = radius,
        )
        val activeHeight = size.height * fraction
        drawRoundRect(
            color = activeColor,
            topLeft = Offset(left, size.height - activeHeight),
            size = Size(barWidth, activeHeight),
            cornerRadius = radius,
        )
    }
}
