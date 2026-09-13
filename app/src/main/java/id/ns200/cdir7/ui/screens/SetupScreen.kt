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

        val fwMode by viewModel.firmwareMode.collectAsState()

        Box(modifier = Modifier.weight(1f)) {
            when (stage) {
                SetupStage.BARU -> BaruStage(viewModel, telemetry)
                SetupStage.PULSER -> PulserStage(viewModel, telemetry)
                SetupStage.TDC -> {
                    if (fwMode == FirmwareRunMode.MANUAL) {
                        StrobeScreen(viewModel)
                    } else {
                        OemLearnTdcCheckpointStage(viewModel, telemetry)
                    }
                }
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
    val oemLearnCoverage by viewModel.oemLearnCoverage.collectAsState()
    val oemRejectedPulses by viewModel.oemRejectedPulses.collectAsState()
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
                        CompactStatusRow("CAKUPAN / DITOLAK", "$oemLearnCoverage% / $oemRejectedPulses", oemCenterPulses >= 20)
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
                                enabled = isOemLearning && !pending && oemCenterPulses >= 20 && t.rpm == 0 && t.hvCenter < 30 && t.hvSide < 30,
                                onClick = viewModel::stopOemLearn,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = RacingLime),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(vertical = 5.dp)
                            ) {
                                Text("SIMPAN & STOP", color = CarbonDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // PANDUAN VISUAL WIRING & RANGKAIAN PENGAMAN SUNTIK KOIL & DAYA STM32
                        Spacer(modifier = Modifier.height(4.dp))
                        OemLearnSafetyWiringGuide()
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
            subtitle = "Pastikan output koil belum mengambil alih. Starter 2–3 detik; RPM harus terbaca dan kualitas pulser dianjurkan ≥10."
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
private fun OemLearnTdcCheckpointStage(viewModel: CdiViewModel, t: Telemetry) {
    val pending by viewModel.setupCommandPending.collectAsState()
    val isOemLearning by viewModel.isOemLearning.collectAsState()
    val oemCenterPulses by viewModel.oemCenterPulses.collectAsState()
    val oemLearnCoverage by viewModel.oemLearnCoverage.collectAsState()
    val oemRejectedPulses by viewModel.oemRejectedPulses.collectAsState()
    val oemSideSamples by viewModel.oemSideSamples.collectAsState()
    val isOemUnpluggedConfirmed by viewModel.isOemUnpluggedConfirmed.collectAsState()

    StageBody {
        StageCard(
            title = "3 • CHECKPOINT REKAM TIMING OEM (PB3/PB4)",
            subtitle = "Jalur OEM Learn: Mesin dinyalakan menggunakan CDI bawaan motor. STM32 merekam pulsa pengapian secara pasif via PB3 & PB4. Strobo flywheel manual tidak diperlukan."
        ) {
            CompactStatusRow("PULSA OEM CENTER (PB3)", "$oemCenterPulses pulsa", oemCenterPulses > 0)
            CompactStatusRow("CAKUPAN / DITOLAK", "$oemLearnCoverage% / $oemRejectedPulses", oemCenterPulses >= 20)
            CompactStatusRow("SAMPEL OEM SIDE (PB4)", "$oemSideSamples sampel", oemSideSamples > 0)
            CompactStatusRow("STATUS PEREKAMAN", if (isOemLearning) "SEDANG MEREKAM DARI CDI OEM..." else if (oemCenterPulses > 0) "TEREKAM (${oemCenterPulses} pulsa)" else "SIAP REKAM", isOemLearning || oemCenterPulses > 0)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    enabled = !isOemLearning && !pending,
                    onClick = viewModel::startOemLearn,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MotecOrange),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("1. MULAI REKAM", color = CarbonDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    enabled = isOemLearning && !pending && oemCenterPulses >= 20 && t.rpm == 0 && t.hvCenter < 30 && t.hvSide < 30,
                    onClick = viewModel::stopOemLearn,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RacingLime),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("2. SIMPAN & STOP", color = CarbonDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        StageCard(
            title = "CHECKPOINT: CABUT OUTPUT OEM",
            subtitle = "Setelah pulsa terekam, matikan mesin dan cabut soket kabel OEM dari koil. CDI STM32 akan mengambil alih pengapian secara mandiri (Mode DIY)."
        ) {
            CompactStatusRow("STATUS SOKET OEM", if (isOemUnpluggedConfirmed) "TERCABUT (DIY MANDIRI AKTIF)" else "MENUNGGU PENCABUTAN", isOemUnpluggedConfirmed)

            if (!isOemUnpluggedConfirmed) {
                Button(
                    enabled = !pending,
                    onClick = viewModel::confirmOemUnplugged,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RacingLime),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("KONFIRMASI OEM_UNPLUGGED & AKTIFKAN DIY", color = CarbonDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfacePanel, RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = RacingLime, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("OEM DILAPASKAN • Mode DIY Siap Pengujian", color = RacingLime, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        StageCard(
            title = "ALUR BERIKUTNYA",
            subtitle = "Lanjutkan kalibrasi TPS jika belum dilakukan, atau langsung ke pengujian First Start."
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.selectQuickSetupPage(SetupStage.TPS_CAL.code) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("4. KALIBRASI TPS", fontSize = 10.sp, color = MotecOrange)
                }
                Button(
                    enabled = isOemUnpluggedConfirmed,
                    onClick = {
                        viewModel.advanceSetupStage(SetupStage.FIRST_START.code)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isOemUnpluggedConfirmed) RacingLime else SurfacePanel),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("5. FIRST START", fontSize = 10.sp, color = if (isOemUnpluggedConfirmed) CarbonDark else TextMuted, fontWeight = FontWeight.Bold)
                }
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

/**
 * Komponen visualisasi interaktif rangkaian pengaman suntik koil (Pin 6 & 12)
 * dan rangkaian daya penyalaan STM32 (Pin 5) untuk Mode OEM Learn.
 */
@Composable
private fun OemLearnSafetyWiringGuide() {
    var isExpanded by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonDark.copy(alpha = 0.85f))
            .border(1.dp, if (selectedTab == 0) RacingLime.copy(alpha = 0.6f) else BorderSubtle, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header dengan tombol lipat (expand/collapse)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = SensorAmber,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "SKEMA RANGKAIAN PENGAMAN SUNTIK KOIL",
                    color = SensorAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        if (isExpanded) {
            // Kotak Bahaya Tegangan Tinggi
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RaceRedline.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .border(1.dp, RaceRedline.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Warning, null, tint = RaceRedline, modifier = Modifier.size(15.dp))
                    Text(
                        text = "BAHAYA: TEGANGAN DISCHARGE KOIL 200V - 400V+",
                        color = RaceRedline,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "DILARANG KERAS menyambung kabel Pin 12 (Center Coil) atau Pin 6 (Side Coil) langsung ke pin STM32! Tegangan induksi dapat melonjak >600V dan akan LANGSUNG MEMBAKAR mikrokontroler STM32WB55. Gunakan salah satu skema pengaman di bawah ini:",
                    color = TextPrimary,
                    fontSize = 9.sp,
                    lineHeight = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Tab Selector Scrollable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf(
                    0 to "1. PROBE OEM WAJIB",
                    1 to "2. MODUL YANG BOLEH",
                    2 to "3. PINOUT PC817",
                    3 to "4. DILARANG: DIVIDER",
                    4 to "5. CATU SERVICE 5V"
                ).forEach { (tabIdx, tabTitle) ->
                    val active = selectedTab == tabIdx
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { selectedTab = tabIdx },
                        color = if (active) ElectricCyan.copy(alpha = 0.2f) else SurfacePanel,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (active) ElectricCyan else BorderSubtle
                        )
                    ) {
                        Text(
                            text = tabTitle,
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 7.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 8.5.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            color = if (active) ElectricCyan else TextSecondary
                        )
                    }
                }
            }

            // Konten Skema Sesuai Tab
            when (selectedTab) {
                0 -> {
                    // TAB 1: Probe OEM terisolasi dengan komponen dan rating yang jelas
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfacePanel, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "PROBE OEM TERISOLASI — PC817 DISKRIT 2 KANAL",
                            color = RacingLime,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Gunakan rangkaian dengan nilai dan pin yang diketahui. Modul PC817 generik 5/12/24V tidak otomatis aman terhadap pulsa koil dan tidak boleh dipasang hanya berdasarkan nama modul.",
                            color = TextSecondary,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        // Netlist ringkas probe OEM CENTER dan SIDE
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CarbonDark, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "CENTER: J1.12 cabang Y → J_OEM_TAP.1 → 22k/1W → 22k/1W → 22k/1W → U_OEM1 pin 1\n" +
                                        "U_OEM1 pin 2 → J_OEM_TAP.3/J1.11; 1N4148 antiparalel: katoda pin 1, anoda pin 2\n" +
                                        "U_OEM1 pin 4 → H_TOP.9 PB3 + pull-up 4.7k ke 3V3; pin 3 → GND_LOGIC\n\n" +
                                        "SIDE: J1.6 cabang Y → J_OEM_TAP.2 → 3×22k/1W → U_OEM2 pin 1\n" +
                                        "U_OEM2 pin 2 → J_OEM_TAP.3; pin 4 → H_TOP.8 PB4 + 4.7k ke 3V3; pin 3 → GND_LOGIC\n\n" +
                                        "J1.8 dan J1.9 TETAP NC. Output DIY tidak tersambung selama CDI OEM menghidupkan koil.",
                                color = ElectricCyan,
                                fontSize = 7.5.sp,
                                lineHeight = 10.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "URUTAN:\n" +
                                    "1. Buat cabang Y non-destruktif dari J1.12, J1.6, dan J1.11 ke JST-XH 3-pin J_OEM_TAP.\n" +
                                    "2. Pasang tiga resistor 22k 1W seri per kanal untuk membagi disipasi/tegangan kerja.\n" +
                                    "3. Pasang 1N4148 tepat antiparalel pada LED input PC817.\n" +
                                    "4. Sisi output: pin 3 GND_LOGIC; pin 4 ke PB3/PB4 dan pull-up 4.7k ke 3V3.\n" +
                                    "5. Verifikasi dari aplikasi: accepted pulses dan coverage harus naik; bukan dari LED hias modul.",
                            color = TextPrimary,
                            fontSize = 8.5.sp,
                            lineHeight = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                1 -> {
                    // TAB 2: KATALOG MODUL PASARAN PENGGANTI SELURUH BLOK SISTEM CDI
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfacePanel, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "MODUL PASARAN YANG BOLEH DIGUNAKAN",
                            color = MotecOrange,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Hanya modul dengan fungsi, pinout, dan rating yang dapat diverifikasi. Blok pengapian/HV tidak boleh diasumsikan drop-in:",
                            color = TextSecondary,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CarbonDark, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "=== BOLEH: BUCK LM2596 / MP1584EN ===\n" +
                                        "• RUN: J1.5 melalui proteksi → buck 5.15V → SS34 → +5V_LOGIC.\n" +
                                        "• SERVICE: USB/power-bank 5V regulated → SS34 kedua → +5V_LOGIC.\n\n" +
                                        "=== BOLEH BERSYARAT: MODUL RELAY 1-CH 5V ===\n" +
                                        "• Hanya bila input HIGH 3.3V terverifikasi dan terminal COM/NO digunakan sebagai dry contact.\n" +
                                        "• Jangan menganggap semua modul optoisolasi memiliki pin/polaritas sama.\n\n" +
                                        "=== TIDAK DROP-IN ===\n" +
                                        "• Modul PC817 5/12/24V generik: resistor/LED/polaritas input berbeda; gunakan rangkaian PC817 diskrit pada tab 1/3.\n" +
                                        "• Modul boost HV generik, bridge 50Hz, dan modul sensor tegangan generik tidak mengikuti kontrol/feedback R8.",
                                color = RacingLime,
                                fontSize = 7.5.sp,
                                lineHeight = 10.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "ATURAN R8.2:\n" +
                                    "• Modul hanya menyederhanakan blok catu/relay; tidak menghapus kebutuhan proteksi dan netlist.\n" +
                                    "• Cocokkan label terminal terhadap skematik vendor sebelum menyolder.",
                            color = TextPrimary,
                            fontSize = 8.5.sp,
                            lineHeight = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                2 -> {
                    // TAB 3: OPTOCOUPLER ISOLASI TOTAL (SOLDER DISKRIT)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfacePanel, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "PINOUT WAJIB PC817C / EL817C DIP-4",
                            color = RacingLime,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Isolasi optik (cahaya) 100% melindungi STM32 dari spike tegangan tinggi CDI OEM.",
                            color = TextSecondary,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        // Diagram ASCII Optocoupler
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CarbonDark, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "=== [1] CENTER: J1.12 CABANG Y ke PB3 ===\n" +
                                        "J_OEM_TAP.1 ──[22k 1W]─[22k 1W]─[22k 1W]──▶ Pin 1 (Anoda PC817)\n" +
                                        "J_OEM_TAP.3 / J1.11 ───────────────────────▶ Pin 2 (Katoda PC817)\n" +
                                        "1N4148 antiparalel: KATODA ke Pin 1, ANODA ke Pin 2\n" +
                                        "Pin 4 (Kolektor PC817) ─┬─▶ WeAct H_TOP.9 (Pin PB3)\n" +
                                        "                        │   (Monitor Pulsa Center OEM)\n" +
                                        "WeAct 3V3 (H_TOP.3) ───[4.7kΩ Pull-up]\n" +
                                        "Pin 3 (Emitter PC817)  ───▶ WeAct H_TOP.1 (Pin GND)\n\n" +
                                        "=== [2] SIDE: J1.6 CABANG Y ke PB4 ===\n" +
                                        "J_OEM_TAP.2 ──[22k 1W]─[22k 1W]─[22k 1W]──▶ Pin 1 (Anoda PC817 #2)\n" +
                                        "J_OEM_TAP.3 / J1.11 ───────────────────────▶ Pin 2 (Katoda PC817 #2)\n" +
                                        "Pin 4 (Kolektor PC817 #2) ─┬─▶ WeAct H_TOP.8 (Pin PB4)\n" +
                                        "WeAct 3V3 (H_TOP.3) ──────[4.7kΩ Pull-up]\n" +
                                        "Pin 3 (Emitter PC817 #2)  ───▶ WeAct H_TOP.1 (Pin GND)",
                                color = ElectricCyan,
                                fontSize = 8.sp,
                                lineHeight = 11.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Daftar Komponen
                        Text(
                            text = "DAFTAR KOMPONEN DIBUTUHKAN:\n" +
                                    "• 2x IC Optocoupler PC817C / EL817C DIP-4\n" +
                                    "• 6x Resistor 22 kΩ 1 Watt Metal Film (3 seri per kanal)\n" +
                                    "• 2x Dioda 1N4148 (Dipasang antiparalel antara Pin 1 & 2 Optocoupler)\n" +
                                    "• 2x Resistor 4.7 kΩ 0.25W (Pull-up ke 3V3 WeAct)",
                            color = TextPrimary,
                            fontSize = 8.5.sp,
                            lineHeight = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                3 -> {
                    // TAB 4: VOLTAGE DIVIDER + BAT54S CLAMP
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfacePanel, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "DILARANG: DIVIDER LANGSUNG KE PB3/PB4",
                            color = SensorAmber,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Skema divider langsung tidak memberikan isolasi galvanik dan dapat memasukkan spike/ground bounce pengapian ke STM32. Tab ini dipertahankan sebagai peringatan, bukan alternatif rakitan.",
                            color = TextSecondary,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        // Diagram ASCII Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CarbonDark, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "JANGAN DIRAKIT:\n" +
                                        "J1.12/J1.6 ─X─ resistor divider ─X─ PB3/PB4\n\n" +
                                        "GUNAKAN:\n" +
                                        "J_OEM_TAP → 3×22k 1W → LED PC817 + 1N4148 antiparalel\n" +
                                        "Sisi transistor PC817 → pull-up 4.7k/3V3 → PB3 atau PB4.",
                                color = MotecOrange,
                                fontSize = 8.sp,
                                lineHeight = 11.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "ALASAN DITOLAK:\n" +
                                    "• Tidak ada isolasi galvanik.\n" +
                                    "• Rating tegangan kerja resistor dan energi transient sulit dijamin.\n" +
                                    "• Dioda clamp dapat mengalirkan energi spike ke rel 3V3.\n" +
                                    "• Firmware R8 mengharapkan pulsa digital bersih dari isolator.",
                            color = TextPrimary,
                            fontSize = 8.5.sp,
                            lineHeight = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                4 -> {
                    // TAB 5: CATU DAYA PENYALAAN STM32 SAAT MESIN HIDUP DENGAN CDI OEM
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfacePanel, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "CATU RUN + SERVICE 5V DENGAN DIODE-OR",
                            color = ElectricCyan,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Port SERVICE menjaga STM32/BLE hidup setelah kill switch menghentikan mesin, sehingga LEARN,STOP dapat disimpan pada RPM 0 dan HV <30V tanpa backfeed ke motor.",
                            color = TextSecondary,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        // Diagram ASCII Power Supply
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CarbonDark, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "RUN: J1.5 → FMAIN/proteksi → FLOGIC → LM2596 OUT+ 5.15V → anoda SS34 RUN\n" +
                                        "SERVICE: J_SERVICE_5V pin 1 (+5V regulated) → anoda SS34 SERVICE\n" +
                                        "Katoda/garis kedua SS34 bertemu → +5V_LOGIC → WeAct H_BOTTOM.2 (5V)\n" +
                                        "J_SERVICE_5V pin 2 + LM2596 OUT- → GND_STAR → WeAct H_BOTTOM.1/H_TOP.1\n\n" +
                                        "DILARANG: 12V aki atau adaptor laptop 19V ke J_SERVICE_5V.",
                                color = RacingLime,
                                fontSize = 8.sp,
                                lineHeight = 11.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "LANGKAH OEM LEARN:\n" +
                                    "1. Aktifkan SERVICE 5V dan pastikan BLE tetap terhubung.\n" +
                                    "2. Saat RPM 0/HV <30V tekan MULAI LEARN, lalu hidupkan CDI OEM.\n" +
                                    "3. Rekam hingga accepted ≥20 dan coverage memadai.\n" +
                                    "4. Hentikan mesin/kill switch; jangan matikan SERVICE.\n" +
                                    "5. Tunggu RPM 0 dan HV <30V, tekan SIMPAN & STOP, tunggu ACK.\n" +
                                    "6. Baru matikan SERVICE dan pindahkan konektor dari OEM ke CDI DIY.",
                            color = TextPrimary,
                            fontSize = 8.5.sp,
                            lineHeight = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
