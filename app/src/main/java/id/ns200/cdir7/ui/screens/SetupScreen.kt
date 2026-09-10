package id.ns200.cdir7.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ns200.cdir7.CdiViewModel
import id.ns200.cdir7.FirmwareRunMode
import id.ns200.cdir7.ScreenTab
import id.ns200.cdir7.SetupStage
import id.ns200.cdir7.Telemetry
import id.ns200.cdir7.ui.theme.*

/**
 * Wizard komisi ringkas pada tab utama Setup.
 *
 * Hanya tahap aktif yang dirender. Tahap TDC sengaja memakai StrobeScreen lama,
 * sedangkan Wiring -> Komisi CDI tetap memakai QuickSetupGuideScreen lengkap.
 * Keduanya berbagi CdiViewModel, state MCU, antrean perintah, dan ACK yang sama.
 */
@Composable
fun SetupScreen(viewModel: CdiViewModel) {
    val page by viewModel.quickSetupPage.collectAsState()
    val unlocked by viewModel.quickSetupUnlockedStage.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val message by viewModel.quickSetupMessage.collectAsState()
    val stage = SetupStage.entries.getOrNull(page) ?: SetupStage.BARU
    val visibleProgress = maxOf(unlocked, telemetry.setupStage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
    ) {
        CompactSetupHeader(
            stage = stage,
            visibleProgress = visibleProgress,
            message = message,
            onSelect = viewModel::selectQuickSetupPage
        )

        Box(modifier = Modifier.weight(1f)) {
            when (stage) {
                SetupStage.BARU -> BaruStage(viewModel, telemetry)
                SetupStage.PULSER -> PulserStage(viewModel, telemetry)
                SetupStage.TDC -> StrobeScreen(viewModel)
                SetupStage.TPS_CAL -> TpsStage(viewModel, telemetry)
                SetupStage.FIRST_START -> FirstStartStage(viewModel, telemetry)
                SetupStage.READY -> ReadyStage(viewModel, telemetry)
            }
        }
    }
}

@Composable
private fun CompactSetupHeader(
    stage: SetupStage,
    visibleProgress: Int,
    message: String,
    onSelect: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle),
        color = SurfacePanel
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "SETUP CDI",
                        color = MotecOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "TAHAP ${stage.code + 1}/6 • ${stage.label}",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    if (stage == SetupStage.READY) "SELESAI" else "IKUTI URUTAN",
                    color = if (stage == SetupStage.READY) RacingLime else SensorAmber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                SetupStage.entries.forEach { item ->
                    val selected = item == stage
                    val unlocked = item.code <= visibleProgress
                    val completed = item.code < visibleProgress && unlocked
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSelect(item.code) },
                        color = when {
                            selected -> MotecOrange.copy(alpha = 0.18f)
                            completed -> RacingLime.copy(alpha = 0.10f)
                            else -> CardBackground
                        },
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when {
                                selected -> MotecOrange
                                completed -> RacingLime.copy(alpha = 0.65f)
                                else -> BorderSubtle
                            }
                        )
                    ) {
                        Text(
                            text = "${item.code + 1} ${item.label}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            color = when {
                                selected -> MotecOrange
                                completed -> RacingLime
                                unlocked -> TextSecondary
                                else -> TextMuted
                            },
                            fontSize = 8.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Text(
                text = message,
                color = when {
                    message.startsWith("LULUS") || message.startsWith("DEMO LULUS") -> RacingLime
                    message.startsWith("GAGAL") -> RaceRedline
                    else -> ElectricCyan
                },
                fontSize = 9.sp,
                lineHeight = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun StageBody(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun StageCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                title,
                color = MotecOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                subtitle,
                color = TextSecondary,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                fontFamily = FontFamily.Monospace
            )
            content()
        }
    }
}

@Composable
private fun BaruStage(viewModel: CdiViewModel, t: Telemetry) {
    val connected by viewModel.isConnected.collectAsState()
    val demo by viewModel.isSimulationMode.collectAsState()
    val busy by viewModel.quickSetupPreflightBusy.collectAsState()
    val pending by viewModel.setupCommandPending.collectAsState()
    val fwMode by viewModel.firmwareMode.collectAsState()
    val isOemLearning by viewModel.isOemLearning.collectAsState()
    val oemCenterPulses by viewModel.oemCenterPulses.collectAsState()
    val oemSideSamples by viewModel.oemSideSamples.collectAsState()
    val isOemUnpluggedConfirmed by viewModel.isOemUnpluggedConfirmed.collectAsState()
    val isProVoltage by viewModel.isProVoltageConfigured.collectAsState()
    val targetHv by viewModel.targetHvVoltage.collectAsState()

    StageBody {
        StageCard(
            title = "1 • PEMERIKSAAN AWAL",
            subtitle = "Mesin mati, HV < 30V. Aplikasi memeriksa PING, STATUS, SETUP, RPM dan tegangan HV sebelum lanjut."
        ) {
            CompactStatusRow("BLE / MCU", if (connected || demo) "SIAP" else "BELUM TERHUBUNG", connected || demo)
            CompactStatusRow("RPM", "${t.rpm}", t.rpm == 0)
            CompactStatusRow("HV CENTER", "${t.hvCenter} V", t.hvCenter < 30)
            CompactStatusRow("HV SIDE", "${t.hvSide} V", t.hvSide < 30)
            Button(
                enabled = !busy,
                onClick = viewModel::startQuickSetupPreflight,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MotecOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (busy) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = CarbonDark)
                    Spacer(Modifier.width(7.dp))
                    Text("MENUNGGU RESPONS MCU...", color = CarbonDark)
                } else {
                    Text("PERIKSA & LANJUT PULSER", color = CarbonDark)
                }
            }
        }

        StageCard(
            title = "KONTROL MODE FIRMWARE R8",
            subtitle = "Pilih alur kerja CDI STM32. Mode DIY mandiri hanya aktif setelah konfirmasi OEM_UNPLUGGED (tidak ada takeover otomatis)."
        ) {
            // Mode selector tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    FirmwareRunMode.OEM_LEARN to "OEM LEARN",
                    FirmwareRunMode.MANUAL to "MANUAL",
                    FirmwareRunMode.DIY to "DIY"
                ).forEach { (m, label) ->
                    val isSelected = fwMode == m
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { viewModel.setFirmwareMode(m) },
                        color = if (isSelected) MotecOrange.copy(alpha = 0.2f) else SurfacePanel,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MotecOrange else BorderSubtle
                        )
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(vertical = 7.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) MotecOrange else TextSecondary
                        )
                    }
                }
            }

            when (fwMode) {
                FirmwareRunMode.OEM_LEARN -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfacePanel, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "ALUR OEM LEARN (BACA TIMING PASIF PB3/PB4)",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "STM32 membaca sinyal pengapian CDI OEM secara pasif melalui PB3 (Center) & PB4 (Side). Mesin hidup menggunakan CDI OEM.",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            lineHeight = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        CompactStatusRow("PULSA OEM CENTER (PB3)", "$oemCenterPulses pulsa", oemCenterPulses > 0)
                        CompactStatusRow("SAMPEL OEM SIDE (PB4)", "$oemSideSamples sampel", oemSideSamples > 0)
                        CompactStatusRow("STATUS BELAJAR", if (isOemLearning) "SEDANG MEREKAM..." else "SIAP", isOemLearning)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                enabled = !isOemLearning && !pending,
                                onClick = viewModel::startOemLearn,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MotecOrange),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(vertical = 5.dp)
                            ) {
                                Text("MULAI LEARN", color = CarbonDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                enabled = isOemLearning && !pending,
                                onClick = viewModel::stopOemLearn,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = RacingLime),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(vertical = 5.dp)
                            ) {
                                Text("SIMPAN & STOP", color = CarbonDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                FirmwareRunMode.DIY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfacePanel, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "MODE DIY (CDI MANDIRI - TANPA OEM)",
                            color = if (isOemUnpluggedConfirmed) RacingLime else SensorAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            if (isOemUnpluggedConfirmed) {
                                "Soket CDI OEM terkonfirmasi dilepas. CDI STM32 bekerja secara mandiri mengontrol pengapian."
                            } else {
                                "PERHATIAN KESELAMATAN: Mode DIY hanya aktif setelah CDI OEM dicabut dari harness (OEM_UNPLUGGED). Tidak ada takeover otomatis."
                            },
                            color = TextSecondary,
                            fontSize = 9.sp,
                            lineHeight = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (!isOemUnpluggedConfirmed) {
                            Button(
                                enabled = !pending,
                                onClick = viewModel::confirmOemUnplugged,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = RacingLime),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("KONFIRMASI OEM_UNPLUGGED & AKTIFKAN DIY", color = CarbonDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = RacingLime, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("OEM_UNPLUGGED Dikonfirmasi • DIY Aktif", color = RacingLime, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
                FirmwareRunMode.MANUAL -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfacePanel, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "MODE MANUAL (STROBO / TDC DARURAT)",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "Mempertahankan setup strobo/TDC lama untuk kondisi CDI OEM rusak atau mati total.",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Target Voltage Selector R8
            Spacer(Modifier.height(4.dp))
            Text(
                "TARGET TEGANGAN HV R8 (NORMAL 285 V / PRO 345 V)",
                color = SensorAmber,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            CompactStatusRow("TEGANGAN TERPILIH", "$targetHv V (${if (isProVoltage) "PRO 345V" else "NORMAL 285V"})", true)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    enabled = !pending,
                    onClick = { viewModel.setHvVoltageMode(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isProVoltage) MotecOrange else SurfacePanel
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 5.dp)
                ) {
                    Text("NORMAL 285 V", color = if (!isProVoltage) CarbonDark else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    enabled = !pending,
                    onClick = { viewModel.setHvVoltageMode(true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isProVoltage) ElectricCyan else SurfacePanel
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 5.dp)
                ) {
                    Text("PRO 345 V", color = if (isProVoltage) CarbonDark else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PulserStage(viewModel: CdiViewModel, t: Telemetry) {
    val pending by viewModel.setupCommandPending.collectAsState()

    StageBody {
        StageCard(
            title = "2 • VERIFIKASI PULSER",
            subtitle = "JP_HV tetap dilepas. Starter 2–3 detik; RPM harus terbaca dan kualitas pulser dianjurkan ≥10."
        ) {
            CompactStatusRow("RPM LIVE", "${t.rpm}", t.rpm > 0)
            CompactStatusRow("PULSER QUALITY", "${t.pickupQuality}/100", t.pickupQuality >= 10)
            PulserAdvancedSettings(viewModel)
            Button(
                enabled = !pending,
                onClick = viewModel::confirmPulserPickup,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RacingLime),
                shape = RoundedCornerShape(8.dp)
            ) {
                PendingButtonText(pending, "KONFIRMASI PULSER & LANJUT TDC")
            }
        }
    }
}

@Composable
private fun TpsStage(viewModel: CdiViewModel, t: Telemetry) {
    val pending by viewModel.setupCommandPending.collectAsState()
    val closed by viewModel.tpsClosedAdc.collectAsState()
    val open by viewModel.tpsOpenAdc.collectAsState()

    StageBody {
        StageCard(
            title = "4 • KALIBRASI TPS",
            subtitle = "Mesin mati dan kontak ON. Simpan posisi gas tertutup dahulu, kemudian buka grip penuh dan simpan posisi 100%."
        ) {
            CompactStatusRow("TPS LIVE", "%.1f %%".format(t.tps / 10f), true)
            CompactStatusRow("ADC TERTUTUP", "$closed", closed > 0)
            CompactStatusRow("ADC TERBUKA", "$open", open > closed + 50)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    enabled = !pending,
                    onClick = viewModel::calibrateTpsClosed,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfacePanel),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("1. GAS TUTUP", color = TextPrimary, fontSize = 10.sp) }
                Button(
                    enabled = !pending,
                    onClick = viewModel::calibrateTpsOpen,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MotecOrange),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("2. GAS PENUH", color = CarbonDark, fontSize = 10.sp) }
            }
            if (pending) {
                Text("Menunggu ACK MCU sebelum tombol berikutnya aktif.", color = SensorAmber, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun FirstStartStage(viewModel: CdiViewModel, t: Telemetry) {
    val pending by viewModel.setupCommandPending.collectAsState()
    val firstStartHv by viewModel.firstStartHv.collectAsState()
    val ranLongEnough = t.firstStartSeconds >= 3
    val stoppedAndSafe = t.rpm == 0 && t.hvCenter < 30 && t.hvSide < 30
    val canSaveReady = ranLongEnough && stoppedAndSafe && !pending

    StageBody {
        StageCard(
            title = "5 • FIRST START AMAN (FIRMWARE R8)",
            subtitle = "Mode aman: 220V, CENTER saja, advance ≤10°, limiter 3.000 RPM. Di R8, status otomatis tersimpan setelah stabil 3 detik dan otomatis READY setelah mesin berhenti atau boot berikutnya."
        ) {
            CompactStatusRow("TARGET TEGANGAN", "$firstStartHv V", firstStartHv <= 220)
            CompactStatusRow("DURASI STABIL", "${t.firstStartSeconds} / 3 detik", ranLongEnough)
            CompactStatusRow("STATUS OTOMATIS R8", if (ranLongEnough) "TERPENUHI (≥3s) • OTOMATIS READY SAAT MATI" else "MENUNGGU STABIL (${t.firstStartSeconds}/3s)", ranLongEnough)
            CompactStatusRow("RPM SEKARANG", "${t.rpm}", t.rpm == 0)
            CompactStatusRow("HV CENTER / SIDE", "${t.hvCenter} / ${t.hvSide} V", stoppedAndSafe)
            Button(
                enabled = !pending,
                onClick = viewModel::prepareFirstStartMode,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MotecOrange),
                shape = RoundedCornerShape(8.dp)
            ) { PendingButtonText(pending, "SIAPKAN FIRST START") }
        }

        StageCard(
            title = "STATUS READY R8 (OTOMATIS / MANUAL)",
            subtitle = if (!ranLongEnough) {
                "Hidupkan mesin pada idle selama 3 detik. Firmware R8 akan otomatis mengunci kalibrasi aman."
            } else if (!stoppedAndSafe) {
                "Mesin telah stabil 3 detik! Matikan mesin (RPM 0 & HV <30 V) untuk transisi otomatis ke READY."
            } else {
                "Syarat terpenuhi. Sistem otomatis beralih ke READY (atau Anda dapat menekan simpan manual di bawah)."
            }
        ) {
            Button(
                enabled = canSaveReady,
                onClick = viewModel::confirmReadyCenterOnly,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RacingLime),
                shape = RoundedCornerShape(8.dp)
            ) { Text("SIMPAN READY • CENTER SAJA", color = CarbonDark, fontWeight = FontWeight.Bold) }
            Text(
                "Di Firmware R8: Setelah stabil 3 detik, saat mesin berhenti atau boot berikutnya CDI otomatis berstatus READY.",
                color = TextMuted,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun ReadyStage(viewModel: CdiViewModel, t: Telemetry) {
    val pending by viewModel.setupCommandPending.collectAsState()

    StageBody {
        StageCard(
            title = "6 • CDI READY",
            subtitle = "Konfigurasi awal tersimpan di flash A/B MCU. Boot berikutnya langsung memakai timing dan map tersimpan tanpa firmware lain."
        ) {
            CompactStatusRow("STATUS", if (t.ready) "READY" else "MENUNGGU SYNC", t.ready)
            CompactStatusRow("KOIL CENTER", if (t.centerEnabled) "AKTIF" else "NONAKTIF", t.centerEnabled)
            CompactStatusRow("KOIL SIDE", if (t.sideEnabled) "AKTIF" else "NONAKTIF", !t.sideEnabled)
            Button(
                onClick = { viewModel.setTab(ScreenTab.MAPS) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MotecOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Tune, null, tint = CarbonDark)
                Spacer(Modifier.width(7.dp))
                Text("BUKA MAP PENGAPIAN", color = CarbonDark)
            }
            OutlinedButton(
                enabled = !pending,
                onClick = viewModel::resetSetupWorkflow,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, null, tint = RaceRedline)
                Spacer(Modifier.width(7.dp))
                Text("RESET SELURUH SETUP", color = RaceRedline)
            }
        }
    }
}

@Composable
private fun CompactStatusRow(label: String, value: String, ok: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePanel, RoundedCornerShape(6.dp))
            .padding(horizontal = 9.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(
            value,
            color = if (ok) RacingLime else SensorAmber,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun PendingButtonText(pending: Boolean, text: String) {
    if (pending) {
        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = CarbonDark)
        Spacer(Modifier.width(7.dp))
        Text("MENUNGGU ACK MCU...", color = CarbonDark, fontSize = 10.sp)
    } else {
        Icon(Icons.Default.Check, null, tint = CarbonDark, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = CarbonDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
