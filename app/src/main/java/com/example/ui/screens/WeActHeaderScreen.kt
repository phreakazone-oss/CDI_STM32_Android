package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WiringDataProvider
import com.example.model.WeActPin
import com.example.ui.components.WeActHeaderVisualizer
import com.example.ui.theme.*
import com.example.viewmodel.WiringViewModel

@Composable
fun WeActHeaderScreen(
  viewModel: WiringViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var selectedFilter by remember { mutableStateOf("SEMUA") }
  val allPins = WiringDataProvider.weActPins

  val filteredPins = remember(selectedFilter) {
    when (selectedFilter) {
      "H_TOP" -> allPins.filter { it.header == "H_TOP" }
      "H_BOTTOM" -> allPins.filter { it.header == "H_BOTTOM" }
      "KRITIS" -> allPins.filter { it.isCritical || it.warning != null }
      else -> allPins
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(TechDarkBg)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
  ) {
    item {
      WeActHeaderVisualizer(
        weActPins = allPins,
        selectedPin = uiState.selectedWeActPin,
        onSelectPin = { pin ->
          viewModel.selectWeActPin(pin)
        }
      )
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("SEMUA", "H_TOP", "H_BOTTOM", "KRITIS").forEach { filter ->
          FilterChip(
            selected = selectedFilter == filter,
            onClick = { selectedFilter = filter },
            label = { Text(filter, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = ElectricCyan,
              selectedLabelColor = Color.Black,
              containerColor = TechSurfaceElevated,
              labelColor = TextPrimaryDark
            )
          )
        }
      }
    }

    items(filteredPins, key = { "${it.header}_${it.pinNumber}" }) { pin ->
      WeActPinCard(
        pin = pin,
        isSelected = pin.header == uiState.selectedWeActPin?.header && pin.pinNumber == uiState.selectedWeActPin?.pinNumber,
        onClick = { viewModel.selectWeActPin(pin) }
      )
    }
  }
}

@Composable
fun WeActPinCard(
  pin: WeActPin,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val isVbatDanger = pin.warning?.contains("DILARANG", ignoreCase = true) == true

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isVbatDanger) Color(0xFF2A1010) else if (isSelected) Color(0xFF142436) else TechSurfaceElevated
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        listOf(
          if (isVbatDanger) HighVoltageRed else if (isSelected) ElectricCyan else OutlineDark,
          if (isVbatDanger) HighVoltageOrange else if (isSelected) ElectricCyanMuted else OutlineDark
        )
      )
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isVbatDanger) HighVoltageRed.copy(alpha = 0.25f) else ElectricCyan.copy(alpha = 0.15f),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(if (isVbatDanger) HighVoltageRed else ElectricCyan, ElectricCyan))
            )
          ) {
            Text(
              text = "${pin.header} • Pin ${pin.pinNumber}",
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              style = MaterialTheme.typography.labelSmall,
              color = if (isVbatDanger) HighVoltageRed else ElectricCyan,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Text(
            text = pin.name,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimaryDark,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Surface(
          shape = RoundedCornerShape(4.dp),
          color = when (pin.direction) {
            "INPUT (ANALOG)", "INPUT" -> SparkAmber.copy(alpha = 0.15f)
            "OUTPUT (PWM)", "OUTPUT" -> SafetyGreen.copy(alpha = 0.15f)
            "POWER" -> HighVoltageRed.copy(alpha = 0.15f)
            "GND" -> GroundStarGold.copy(alpha = 0.15f)
            else -> Color(0xFF475569).copy(alpha = 0.2f)
          }
        ) {
          Text(
            text = pin.direction,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = when (pin.direction) {
              "INPUT (ANALOG)", "INPUT" -> SparkAmber
              "OUTPUT (PWM)", "OUTPUT" -> SafetyGreen
              "POWER" -> HighVoltageRed
              "GND" -> GroundStarGold
              else -> Color(0xFF94A3B8)
            },
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Final Destination Net
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF0C1622))
          .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Tujuan Akhir: ",
          style = MaterialTheme.typography.labelSmall,
          color = TextTertiaryDark
        )
        Text(
          text = pin.finalDestination,
          style = MaterialTheme.typography.labelSmall,
          color = ElectricCyan,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      // Full Connection Path
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Jalur Lengkap: ${pin.fullPath}",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
        color = TextPrimaryDark
      )

      // Warning Box
      if (pin.warning != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = HighVoltageRed.copy(alpha = 0.15f)),
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(HighVoltageRed, HighVoltageOrange))),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = HighVoltageRed,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = pin.warning,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = HighVoltageRed,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
