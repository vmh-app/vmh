package eu.kanade.presentation.more.onboarding

import android.content.ActivityNotFoundException
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.screen.SettingsDataScreen
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.flow.collectLatest
import mihon.app.di.appGraph
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

internal class StorageStep : OnboardingStep {

    private var _isComplete by mutableStateOf(false)

    override val isComplete: Boolean
        get() = _isComplete

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val colors = MaterialTheme.colorScheme

        val storagePref = remember { context.appGraph.storagePreferences.baseStorageDirectory }
        val pickStorageLocation = SettingsDataScreen.storageLocationPicker(storagePref)
        val location = SettingsDataScreen.storageLocationText(storagePref)
        val isSet = storagePref.isSet()

        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Tarjeta visual que demuestra la creación de las carpetas
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
                            painter = painterResource(R.drawable.ic_vmh_folder),
                            tint = colors.primary,
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (isSet) {
                                    location
                                } else {
                                    stringResource(
                                        MR.strings.onboarding_storage_selected_label,
                                    )
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
                            )
                            Text(
                                text = stringResource(MR.strings.vmh_onb_base_location),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.outlineVariant.copy(alpha = 0.4f)),
                    )

                    // Estructura creada
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StorageSubFolder("downloads", colors.secondary)
                        StorageSubFolder("backups", colors.tertiary)
                    }
                }
            }

            VmhStepAction(
                text = stringResource(MR.strings.onboarding_storage_action_select),
                icon = painterResource(R.drawable.ic_vmh_folder),
                onClick = {
                    try {
                        pickStorageLocation.launch(null)
                    } catch (e: ActivityNotFoundException) {
                        context.toast(MR.strings.file_picker_error)
                    }
                },
            )
        }

        LaunchedEffect(Unit) {
            storagePref.changes()
                .collectLatest { _isComplete = storagePref.isSet() }
        }
    }

    @Composable
    private fun StorageSubFolder(name: String, tint: androidx.compose.ui.graphics.Color) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_vmh_folder),
                contentDescription = null,
                tint = tint.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
