package eu.kanade.presentation.more.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.domain.ui.model.AppTheme
import eu.kanade.domain.ui.model.ThemeMode
import eu.kanade.domain.ui.model.setAppCompatDelegateThemeMode
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Check
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

internal class ThemeStep : OnboardingStep {

    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val uiPreferences = remember { context.appGraph.uiPreferences }
        val themeModePref = uiPreferences.themeMode
        val themeMode by themeModePref.collectAsState()

        val appThemePref = uiPreferences.appTheme
        val appTheme by appThemePref.collectAsState()
        val colors = MaterialTheme.colorScheme

        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Previsualización viva al inicio para ver el efecto instantáneo
            VmhThemePreview()

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                VmhSectionLabel(stringResource(MR.strings.onboarding_theme_mode_label))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        ThemeMode.SYSTEM to MR.strings.theme_system,
                        ThemeMode.LIGHT to MR.strings.theme_light,
                        ThemeMode.DARK to MR.strings.theme_dark,
                    ).forEach { (mode, labelRes) ->
                        VmhChoiceChip(
                            label = stringResource(labelRes),
                            selected = themeMode == mode,
                            onClick = {
                                themeModePref.set(mode)
                                setAppCompatDelegateThemeMode(mode)
                                (context as? android.app.Activity)?.let {
                                    androidx.core.app.ActivityCompat.recreate(it)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                VmhSectionLabel(stringResource(MR.strings.onboarding_theme_style_label))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    VmhThemeCard(
                        label = "VMH",
                        selected = appTheme == AppTheme.DEFAULT,
                        background = Color(0xFF0B0F17),
                        dots = listOf(Color(0xFF8AB4FF), Color(0xFFFF8A70), Color(0xFF4ADE9B)),
                        onClick = { appThemePref.set(AppTheme.DEFAULT) },
                    )
                    VmhThemeCard(
                        label = stringResource(MR.strings.theme_monet),
                        selected = appTheme == AppTheme.MONET,
                        background = colors.surfaceContainerHighest,
                        dots = listOf(colors.primary, colors.secondary, colors.tertiary),
                        onClick = { appThemePref.set(AppTheme.MONET) },
                    )
                    VmhThemeCard(
                        label = stringResource(MR.strings.theme_catppuccin),
                        selected = appTheme == AppTheme.CATPPUCCIN,
                        background = Color(0xFF1E1E2E),
                        dots = listOf(Color(0xFFCBA6F7), Color(0xFF89B4FA), Color(0xFFA6E3A1)),
                        onClick = { appThemePref.set(AppTheme.CATPPUCCIN) },
                    )
                }
            }
        }
    }
}

@Composable
private fun VmhThemePreview() {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(shape)
            .background(colors.surfaceContainer)
            .border(2.dp, colors.outlineVariant.copy(alpha = 0.5f), shape)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Portada (Usa Gradiente Primario a Secundario)
            Box(
                modifier = Modifier
                    .width(76.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(colors.primary, colors.secondary),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Títulos
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(colors.onSurface),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.onSurfaceVariant.copy(alpha = 0.6f)),
                    )
                }

                // Botones / Badges (Menta/Terciario y Primario)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.primary),
                    )
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.tertiary),
                    )
                }
            }
        }
    }
}

@Composable
private fun VmhThemeCard(
    label: String,
    selected: Boolean,
    background: Color,
    dots: List<Color>,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .width(110.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(shape)
                .background(background)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) colors.primary else colors.outlineVariant.copy(alpha = 0.5f),
                    shape = shape,
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    dots.take(2).forEach { dot ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(dot),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(dots.last().copy(alpha = 0.9f)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(dots.last().copy(alpha = 0.45f)),
                )
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Check,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) colors.primary else colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
