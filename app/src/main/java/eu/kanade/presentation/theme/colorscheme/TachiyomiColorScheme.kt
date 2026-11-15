package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Colores del tema por defecto de VMH.
 *
 * Base blanca y negra pura (sin tintes). Acentos: Azul tinta, coral y menta.
 */
internal object TachiyomiColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFF8AB4FF), // Azul tinta oscuro
        onPrimary = Color(0xFF06214F),
        primaryContainer = Color(0xFF123A78),
        onPrimaryContainer = Color(0xFFEAF0FF),
        inversePrimary = Color(0xFF1D4ED8),
        secondary = Color(0xFFFF8A70), // Coral (Acento/Unread)
        onSecondary = Color(0xFF3B0A00),
        secondaryContainer = Color(0xFF1F3B7A), // Pastilla activa (azul profundo)
        onSecondaryContainer = Color(0xFFDCE6FF),
        tertiary = Color(0xFF4ADE9B), // Menta (Descargado)
        onTertiary = Color(0xFF0B0F17),
        tertiaryContainer = Color(0xFF4ADE9B),
        onTertiaryContainer = Color(0xFF0B0F17),
        background = Color(0xFF000000),
        onBackground = Color(0xFFEFEFEF),
        surface = Color(0xFF000000),
        onSurface = Color(0xFFEFEFEF),
        surfaceVariant = Color(0xFF1A1A1A),
        onSurfaceVariant = Color(0xFFB3B3B3),
        surfaceTint = Color(0xFF8AB4FF),
        inverseSurface = Color(0xFFFFFFFF),
        inverseOnSurface = Color(0xFF0A0A0A),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF5C5C5C),
        outlineVariant = Color(0xFF2E2E2E),
        surfaceContainerLowest = Color(0xFF000000),
        surfaceContainerLow = Color(0xFF0D0D0D),
        surfaceContainer = Color(0xFF141414),
        surfaceContainerHigh = Color(0xFF1F1F1F),
        surfaceContainerHighest = Color(0xFF2A2A2A),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFF1D4ED8), // Azul tinta claro
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDCE6FF),
        onPrimaryContainer = Color(0xFF0A1F5C),
        inversePrimary = Color(0xFF8AB4FF),
        secondary = Color(0xFFCC3410), // Coral (Acento/Unread)
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDCE6FF),
        onSecondaryContainer = Color(0xFF0A1F5C),
        tertiary = Color(0xFF0B7A57), // Menta (Descargado)
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFF0B7A57),
        onTertiaryContainer = Color(0xFFFFFFFF),
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF141414),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF141414),
        surfaceVariant = Color(0xFFF0F0F0),
        onSurfaceVariant = Color(0xFF575757),
        surfaceTint = Color(0xFF1D4ED8),
        inverseSurface = Color(0xFF1A1A1A),
        inverseOnSurface = Color(0xFFF2F2F2),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF8A8A8A),
        outlineVariant = Color(0xFFDEDEDE),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFFAFAFA),
        surfaceContainer = Color(0xFFF5F5F5),
        surfaceContainerHigh = Color(0xFFEDEDED),
        surfaceContainerHighest = Color(0xFFE3E3E3),
    )
}
