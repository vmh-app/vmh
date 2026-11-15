package eu.kanade.presentation.more.guide

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.util.Screen
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.CollectionsBookmark
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Explore
import mihon.icons.materialsymbols.rounded.Search
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource

/**
 * Guía corta que se abre desde la biblioteca vacía.
 *
 * Antes ese botón abría el repositorio de extensiones, que no explica nada: aquí van los cuatro
 * pasos reales, cada uno con la ruta exacta dentro de la app.
 */
class GettingStartedScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(MR.strings.vmh_guide_title),
                    navigateUp = navigator::pop,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { contentPadding ->
            LazyColumn(
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                item {
                    Text(
                        text = stringResource(MR.strings.vmh_guide_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    )
                }
                steps.forEachIndexed { index, step ->
                    item(key = index) {
                        GuideStepCard(number = index + 1, step = step)
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun GuideStepCard(number: Int, step: GuideStep) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(step.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(step.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

private data class GuideStep(
    val titleRes: StringResource,
    val descriptionRes: StringResource,
    val icon: ImageVector,
)

private val steps = listOf(
    GuideStep(
        titleRes = MR.strings.vmh_guide_step1_title,
        descriptionRes = MR.strings.vmh_guide_step1_desc,
        icon = MaterialSymbols.Rounded.Explore,
    ),
    GuideStep(
        titleRes = MR.strings.vmh_guide_step2_title,
        descriptionRes = MR.strings.vmh_guide_step2_desc,
        icon = MaterialSymbols.Rounded.Download,
    ),
    GuideStep(
        titleRes = MR.strings.vmh_guide_step3_title,
        descriptionRes = MR.strings.vmh_guide_step3_desc,
        icon = MaterialSymbols.Rounded.Search,
    ),
    GuideStep(
        titleRes = MR.strings.vmh_guide_step4_title,
        descriptionRes = MR.strings.vmh_guide_step4_desc,
        icon = MaterialSymbols.Rounded.CollectionsBookmark,
    ),
)
