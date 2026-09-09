package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WeActPin
import com.example.ui.theme.*

@Composable
fun WeActHeaderVisualizer(
  weActPins: List<WeActPin>,
  selectedPin: WeActPin?,
  onSelectPin: (WeActPin) -> Unit,
  modifier: Modifier = Modifier
) {
  val topPins = weActPins.filter { it.header == "H_TOP" }.sortedBy { it.pinNumber }
  val bottomPins = weActPins.filter { it.header == "H_BOTTOM" }.sortedBy { it.pinNumber }
  val scrollState = rememberScrollState()

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = TechSurfaceElevated),
    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(OutlineDark, ElectricCyan.copy(alpha = 0.3f))))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "POSISI HEADER FISIK WeAct STM32WB55",
            style = MaterialTheme.typography.labelLarge,
            color = ElectricCyan,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Dilihat dari sisi komponen: USB di kiri, Antena di kanan",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryDark
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = SafetyGreen.copy(alpha = 0.15f),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(SafetyGreen, ElectricCyan)))
        ) {
          Text(
            text = "35 HEADER PIN",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = SafetyGreen,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Scrollable Board Visualizer
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(scrollState)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF09111C))
          .border(2.dp, Color(0xFF1B2C42), RoundedCornerShape(12.dp))
          .padding(14.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // USB Connector on Left
          Box(
            modifier = Modifier
              .width(28.dp)
              .height(90.dp)
              .background(Color(0xFF2E3E55), RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
              .border(1.5.dp, Color(0xFF455A75), RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "USB\nTYPE-C",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, lineHeight = 9.sp),
              color = TextPrimaryDark,
              textAlign = TextAlign.Center,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          // Main Header Area
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // H_TOP Row (15 pins)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              topPins.forEach { pin ->
                WeActPinDot(
                  pin = pin,
                  isSelected = pin.header == selectedPin?.header && pin.pinNumber == selectedPin?.pinNumber,
                  onClick = { onSelectPin(pin) }
                )
              }
              // Fill 5 empty spaces to match bottom 20 pins alignment
              repeat(5) {
                Box(modifier = Modifier.size(28.dp))
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Board Silkscreen Center Line
            Row(
              modifier = Modifier
                .width(680.dp)
                .height(30.dp)
                .background(Color(0xFF0E1A29), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF1E324D), RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "H_TOP: 15 Pin (Kiri ke Kanan: G, G, 3V3, 3V3, PB7 ... PB0, G)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextTertiaryDark
              )
              Text(
                text = "H_BOTTOM: 20 Pin (G, 5V, 5V, VBAT ... PB2, G)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextTertiaryDark
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // H_BOTTOM Row (20 pins)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              bottomPins.forEach { pin ->
                WeActPinDot(
                  pin = pin,
                  isSelected = pin.header == selectedPin?.header && pin.pinNumber == selectedPin?.pinNumber,
                  onClick = { onSelectPin(pin) }
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          // 2.4GHz BLE Antenna Zone on Right
          Box(
            modifier = Modifier
              .width(50.dp)
              .height(110.dp)
              .background(Color(0xFF162238), RoundedCornerShape(6.dp))
              .border(1.5.dp, HighVoltageRed.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
              .padding(4.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Zona Bebas",
                tint = HighVoltageRed,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "ANTENA\n2.4G BLE\n\nBEBAS\nLOGAM",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, lineHeight = 9.sp),
                color = HighVoltageRed,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Legend
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        LegendDot(color = ElectricCyan, text = "Logic/Sensor")
        LegendDot(color = SparkAmber, text = "Charger/HV")
        LegendDot(color = HighVoltageRed, text = "Dilarang 12V / Kritis")
        LegendDot(color = Color(0xFF64748B), text = "Cadangan")
      }
    }
  }
}

@Composable
private fun WeActPinDot(
  pin: WeActPin,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val pinColor = when {
    pin.warning != null && pin.warning.contains("DILARANG", ignoreCase = true) -> HighVoltageRed
    pin.name.contains("PA6") || pin.name.contains("PA7") || pin.name.contains("PA9") || pin.name.contains("PB8") -> SparkAmber
    pin.name.contains("GND") || pin.name == "G" -> GroundStarGold
    pin.name.contains("5V") || pin.name.contains("3V3") -> ElectricCyan
    pin.status == "CADANGAN" -> Color(0xFF475569)
    else -> ElectricCyan
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(28.dp)
      .clickable { onClick() }
  ) {
    Box(
      modifier = Modifier
        .size(26.dp)
        .clip(CircleShape)
        .background(if (isSelected) pinColor.copy(alpha = 0.4f) else Color(0xFF131F30))
        .border(
          width = if (isSelected) 2.dp else 1.dp,
          color = if (isSelected) Color.White else pinColor.copy(alpha = 0.8f),
          shape = CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "${pin.pinNumber}",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 8.sp,
          fontWeight = FontWeight.Bold
        ),
        color = Color.White
      )
    }

    Text(
      text = pin.name,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 7.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        fontFamily = FontFamily.Monospace
      ),
      color = if (isSelected) ElectricCyan else TextSecondaryDark,
      maxLines = 1
    )
  }
}

@Composable
private fun LegendDot(color: Color, text: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
      color = TextSecondaryDark
    )
  }
}
