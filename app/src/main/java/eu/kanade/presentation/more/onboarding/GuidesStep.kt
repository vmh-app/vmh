package eu.kanade.presentation.more.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.R
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

internal class GuidesStep(
    private val onRestoreBackup: () -> Unit,
) : OnboardingStep {

    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        val colors = MaterialTheme.colorScheme

        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Tarjeta que detalla explícitamente lo que incluye la copia de seguridad.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceContainerHighest)
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VmhIconTile(
                            painter = painterResource(R.drawable.ic_vmh_restore),
                            tint = colors.secondary,
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(MR.strings.vmh_onb_guides_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
                            )
                            Text(
                                text = stringResource(MR.strings.vmh_onb_guides_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }

                    // Elementos a recuperar
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RestoreItem(stringResource(MR.strings.vmh_onb_restore_library))
                        RestoreItem(stringResource(MR.strings.vmh_onb_restore_history))
                        RestoreItem(stringResource(MR.strings.vmh_onb_restore_settings))
                    }
                }
            }

            VmhStepAction(
                text = stringResource(MR.strings.pref_restore_backup),
                icon = painterResource(R.drawable.ic_vmh_restore),
                onClick = onRestoreBackup,
            )
        }
    }

    @Composable
    private fun RestoreItem(name: String) {
        val colors = MaterialTheme.colorScheme
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_vmh_book),
                contentDescription = null,
                tint = colors.secondary.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
