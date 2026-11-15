package eu.kanade.presentation.more.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import eu.kanade.presentation.util.rememberRequestPackageInstallsPermissionState
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.util.system.launchRequestPackageInstallsPermission
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Check
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

internal class PermissionStep : OnboardingStep {

    private var notificationGranted by mutableStateOf(false)
    private var batteryGranted by mutableStateOf(false)

    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val installGranted = rememberRequestPackageInstallsPermissionState()

        DisposableEffect(lifecycleOwner.lifecycle) {
            val observer = object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                            PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }
                    batteryGranted = context.getSystemService<PowerManager>()!!
                        .isIgnoringBatteryOptimizations(context.packageName)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Explicamos qué desbloquea el permiso en lugar de explicar técnicamente el permiso en sí.
            VmhPermissionRow(
                painter = painterResource(R.drawable.ic_vmh_install),
                title = stringResource(MR.strings.vmh_onb_perm_ext_title),
                subtitle = stringResource(MR.strings.vmh_onb_perm_ext_desc),
                granted = installGranted,
                onButtonClick = {
                    context.launchRequestPackageInstallsPermission()
                },
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionRequester = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = {},
                )
                VmhPermissionRow(
                    painter = painterResource(R.drawable.ic_vmh_bell),
                    title = stringResource(MR.strings.vmh_onb_perm_notif_title),
                    subtitle = stringResource(MR.strings.vmh_onb_perm_notif_desc),
                    granted = notificationGranted,
                    onButtonClick = { permissionRequester.launch(Manifest.permission.POST_NOTIFICATIONS) },
                )
            }

            VmhPermissionRow(
                painter = painterResource(R.drawable.ic_vmh_battery),
                title = stringResource(MR.strings.vmh_onb_perm_battery_title),
                subtitle = stringResource(MR.strings.vmh_onb_perm_battery_desc),
                granted = batteryGranted,
                onButtonClick = {
                    @SuppressLint("BatteryLife")
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    context.startActivity(intent)
                },
            )
        }
    }

    @Composable
    private fun VmhPermissionRow(
        painter: Painter,
        title: String,
        subtitle: String,
        granted: Boolean,
        onButtonClick: () -> Unit,
    ) {
        val colors = MaterialTheme.colorScheme

        // Si está concedido, usamos color menta (tertiary) para indicar un estado final exitoso.
        val mainColor = if (granted) colors.tertiary else colors.primary

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceContainerHighest)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VmhIconTile(
                painter = painter,
                tint = mainColor,
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(16.dp))

            if (granted) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(mainColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Check,
                        contentDescription = null,
                        tint = mainColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            } else {
                OutlinedButton(
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.outlineVariant),
                    onClick = onButtonClick,
                ) {
                    Text(
                        text = stringResource(MR.strings.vmh_onb_perm_allow),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
