package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FullCumulativeCircuitSimulator
import com.example.ui.theme.*
import com.example.viewmodel.WiringViewModel
import id.ns200.cdir7.CdiViewModel
import id.ns200.cdir7.ui.screens.QuickSetupGuideScreen
import id.ns200.cdir7.ui.theme.CarbonDark
import id.ns200.cdir7.ui.theme.CardBackground
import id.ns200.cdir7.ui.theme.BorderSubtle
import id.ns200.cdir7.ui.theme.SurfacePanel
import id.ns200.cdir7.ui.theme.MotecOrange
import id.ns200.cdir7.ui.theme.RacingLime
import id.ns200.cdir7.ui.theme.SensorAmber
import id.ns200.cdir7.ui.theme.TextMuted

enum class WorkshopSubTab(
    val title: String,
    val icon: ImageVector,
    val badge: String
) {
    STEPS("Langkah Solder", Icons.Default.Build, "18 STEP"),
    PCB_SIM("Simulator PCB", Icons.Default.GridView, "25x30"),
    HARNESS_J1("Soket J1", Icons.Default.Cable, "16 PIN"),
    WEACT_MCU("Header WeAct", Icons.Default.Memory, "40 PIN"),
    BOM_LIST("Daftar Belanja", Icons.Default.ShoppingCart, "BOM"),
    PINOUT_LIB("Katalog Part", Icons.Default.Layers, "SPECS"),
    COMMISSION("Komisi CDI", Icons.Default.CheckCircle, "STAGE 0-5")
}

@Composable
fun WiringWorkshopHubScreen(
    wiringViewModel: WiringViewModel,
    cdiViewModel: CdiViewModel,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(WorkshopSubTab.STEPS) }
    val currentStep by wiringViewModel.currentStep.collectAsState()
    val verificationRecords by wiringViewModel.verificationRecords.collectAsState()
    val verificationProgress by wiringViewModel.verificationProgress.collectAsState()
    val verifiedStepIds = remember(verificationRecords) {
        verificationRecords.filter { it.value.isVerified }.keys
    }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonDark)
    ) {
        // Workshop Sub-navigation bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle),
            color = SurfacePanel
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WorkshopSubTab.entries.forEach { subTab ->
                    val isSelected = activeSubTab == subTab
                    val pillColor = when (subTab) {
                        WorkshopSubTab.STEPS -> ElectricCyan
                        WorkshopSubTab.PCB_SIM -> SensorAmber
                        WorkshopSubTab.HARNESS_J1 -> RacingLime
                        WorkshopSubTab.WEACT_MCU -> ElectricCyan
                        WorkshopSubTab.BOM_LIST -> MotecOrange
                        WorkshopSubTab.PINOUT_LIB -> ElectricCyan
                        WorkshopSubTab.COMMISSION -> RacingLime
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { activeSubTab = subTab }
                            .testTag("subtab_${subTab.name.lowercase()}"),
                        color = if (isSelected) pillColor.copy(alpha = 0.18f) else CardBackground,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) pillColor else BorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = subTab.icon,
                                contentDescription = null,
                                tint = if (isSelected) pillColor else TextSecondaryDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = subTab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextPrimaryDark else TextSecondaryDark,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSelected) pillColor.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = if (subTab == WorkshopSubTab.STEPS) "${verificationProgress.first}/${verificationProgress.second}" else subTab.badge,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) pillColor else TextMuted,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active content screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (activeSubTab) {
                WorkshopSubTab.STEPS -> {
                    TutorialStepScreen(viewModel = wiringViewModel)
                }
                WorkshopSubTab.PCB_SIM -> {
                    Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        FullCumulativeCircuitSimulator(
                            currentStep = currentStep,
                            allSteps = wiringViewModel.allSteps,
                            verifiedStepIds = verifiedStepIds,
                            onSelectStep = { wiringViewModel.selectStep(it) },
                            onClose = { activeSubTab = WorkshopSubTab.STEPS }
                        )
                    }
                }
                WorkshopSubTab.HARNESS_J1 -> {
                    HarnessPinsScreen(viewModel = wiringViewModel)
                }
                WorkshopSubTab.WEACT_MCU -> {
                    WeActHeaderScreen(viewModel = wiringViewModel)
                }
                WorkshopSubTab.BOM_LIST -> {
                    BomChecklistScreen(viewModel = wiringViewModel)
                }
                WorkshopSubTab.PINOUT_LIB -> {
                    ComponentLibraryScreen(viewModel = wiringViewModel)
                }
                WorkshopSubTab.COMMISSION -> {
                    QuickSetupGuideScreen(viewModel = cdiViewModel)
                }
            }
        }
    }
}
