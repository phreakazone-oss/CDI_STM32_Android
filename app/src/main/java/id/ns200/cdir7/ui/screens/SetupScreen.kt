package id.ns200.cdir7.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ns200.cdir7.CdiViewModel
import id.ns200.cdir7.SetupStage
import id.ns200.cdir7.ui.theme.*

/**
 * Pintu utama Setup CDI.
 *
 * STROBO TDC mempertahankan layout kalibrasi lama yang ringkas. TAHAPAN SETUP
 * membuka workflow komisi lengkap. Keduanya memakai CdiViewModel yang sama,
 * sehingga tombol pada kedua tampilan tetap mengirim perintah ke MCU dan
 * menunggu ACK yang sama.
 */
@Composable
fun SetupScreen(viewModel: CdiViewModel) {
    val quickSetupPage by viewModel.quickSetupPage.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    var selectedView by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle),
            color = SurfacePanel
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    SetupViewButton(
                        modifier = Modifier.weight(1f),
                        selected = selectedView == 0,
                        title = "STROBO TDC",
                        subtitle = "Kalibrasi timing",
                        icon = { Icon(Icons.Default.FlashOn, null, modifier = Modifier.size(16.dp)) },
                        onClick = { selectedView = 0 }
                    )
                    SetupViewButton(
                        modifier = Modifier.weight(1f),
                        selected = selectedView == 1,
                        title = "TAHAPAN SETUP",
                        subtitle = "Komisi ${quickSetupPage + 1}/6",
                        icon = { Icon(Icons.Default.FactCheck, null, modifier = Modifier.size(16.dp)) },
                        onClick = { selectedView = 1 }
                    )
                }

                Text(
                    text = if (selectedView == 0) {
                        "Layout strobo lama • kontrol PB9, offset dan simpan TDC tetap aktif"
                    } else {
                        "Tahap aktif: ${SetupStage.entries.getOrNull(quickSetupPage)?.label ?: telemetry.stage.label} • seluruh tombol tersinkron ke MCU"
                    },
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted,
                    maxLines = 1
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedView == 0) {
                StrobeScreen(viewModel = viewModel)
            } else {
                QuickSetupGuideScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun SetupViewButton(
    modifier: Modifier,
    selected: Boolean,
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val accent = if (selected) SensorAmber else TextMuted
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (selected) SensorAmber.copy(alpha = 0.14f) else CardBackground,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) SensorAmber else BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides accent
            ) { icon() }
            Column {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = accent
                )
                Text(
                    text = subtitle,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
            }
        }
    }
}
