package com.example.model

enum class PcbBoard(val title: String, val size: String, val colorCode: Long) {
  PCB_LOGIC("PCB Logic & Sensor", "7x9 cm Single-Layer", 0xFF00E5FF),
  PCB_POWER("PCB Power & HV", "Minimal 5x7 cm (Clearance >= 6mm)", 0xFFFF9E0B),
  AUDIO_BOARD("Modul Audio Opsional", "Board Terpisah PAM8610", 0xFFB388FF),
  HARNESS_PIGTAIL("Pigtail Harness NS200", "Soket 12-Pin Original", 0xFF00E676)
}

enum class VerificationType {
  MULTIMETER_VOLT,
  MULTIMETER_CONTINUITY,
  MULTIMETER_OHM,
  VISUAL_INSPECTION,
  JUMPER_STATE
}

data class StepComponent(
  val ref: String,
  val name: String,
  val spec: String,
  val pinDescription: String
)

data class PinConnection(
  val id: String,
  val fromNode: String,
  val toNode: String,
  val wireColorHex: Long,
  val wireLabel: String,
  val solderTip: String,
  val isHighVoltage: Boolean = false
)

data class WiringStep(
  val id: String,
  val stageId: Int,
  val stageTitle: String,
  val stepNumber: String,
  val title: String,
  val board: PcbBoard,
  val sourcePin: String,
  val targetPin: String,
  val components: List<StepComponent>,
  val schematicTrace: String,
  val perfboardTips: List<String>,
  val pinLegGuide: String,
  val verificationRequirement: String,
  val verificationType: VerificationType,
  val expectedValue: String,
  val criticalSafetyWarning: String? = null,
  val pinConnections: List<PinConnection> = emptyList()
)

data class HarnessPin(
  val pinNumber: Int,
  val wireColor: String,
  val name: String,
  val direction: String,
  val completePath: String,
  val destination: String,
  val status: String,
  val isConnected: Boolean = true,
  val detailGuide: String
)

data class WeActPin(
  val header: String, // "H_TOP" or "H_BOTTOM"
  val pinNumber: Int,
  val name: String,
  val direction: String,
  val fullPath: String,
  val finalDestination: String,
  val status: String,
  val isCritical: Boolean = false,
  val warning: String? = null
)

data class PinLeg(
  val pinNumber: String,
  val name: String,
  val description: String
)

data class ComponentPinout(
  val ref: String,
  val name: String,
  val packageType: String,
  val ratingSpec: String,
  val pinLegs: List<PinLeg>,
  val orientationGuide: String,
  val donorPsuRule: String,
  val safetyNotice: String? = null
)

data class VerificationRecord(
  val stepId: String,
  val isVerified: Boolean = false,
  val measuredValue: String = "",
  val userNotes: String = "",
  val verifiedTimestamp: Long = 0L
)

data class BomItem(
  val id: String,
  val section: String,
  val ref: String,
  val qty: String,
  val spec: String,
  val source: String,
  val notes: String,
  val isAcquired: Boolean = false
)

data class QuickSetupStep(
  val stepNumber: Int,
  val stageName: String,
  val connectionCondition: String,
  val appAction: String,
  val outputCondition: String,
  val proceedCriteria: String,
  val stopHazard: String
)
