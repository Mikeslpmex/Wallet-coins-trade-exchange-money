package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayShieldViewModel
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrionTerminalScreen(
    viewModel: PayShieldViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.terminalLogs.size) {
        if (uiState.terminalLogs.isNotEmpty()) {
            listState.animateScrollToItem(uiState.terminalLogs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Brand & Bot Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Indigo500.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = Indigo500, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("LUCY TELEGRAM BOT v2.0", color = Slate50, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("OrionCash Core + NeuraForge Antifraud Engine", color = Cyan400, fontSize = 10.sp)
                }
            }

            IconButton(onClick = { viewModel.refreshMarketRates() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar tasas", tint = Slate400)
            }
        }

        // Live Market Ticker Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("USD / MXN", color = Slate400, fontSize = 10.sp)
                    Text("$${String.format(Locale.US, "%.2f", uiState.liveRates.usdToMxn)}", color = Emerald400, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("ETH / USD", color = Slate400, fontSize = 10.sp)
                    Text("$${String.format(Locale.US, "%,.1f", uiState.liveRates.ethToUsd)}", color = Cyan400, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("BTC / USD", color = Slate400, fontSize = 10.sp)
                    Text("$${String.format(Locale.US, "%,.1f", uiState.liveRates.btcToUsd)}", color = Amber400, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("Saldo Bóveda", color = Slate400, fontSize = 10.sp)
                    Text("$${String.format(Locale.US, "%,.0f", uiState.tesoreroBalanceUsd)}", color = Slate50, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Terminal Output Console Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate800))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                // Console Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Rose500))
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Amber400))
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Emerald400))
                    }
                    Text("bash - lucy_telegram_v2.py (OrionCash)", color = Slate600, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }

                Divider(color = Slate800, modifier = Modifier.padding(bottom = 6.dp))

                // Console Log Lines
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.terminalLogs) { log ->
                        val color = when {
                            log.startsWith("👤") -> Cyan400
                            log.startsWith("✅") -> Emerald400
                            log.startsWith("🚨") -> Rose500
                            log.startsWith("📊") || log.startsWith("🛡️") -> Amber400
                            else -> Slate300
                        }
                        Text(
                            text = log,
                            color = color,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Quick Preset Commands
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val presets = listOf(
                "/oxxopay 500",
                "/googlecard 200",
                "/transferir 0x20a218EABE02Fe 150 USD",
                "/reporte_transferencias",
                "/antifraude_status",
                "/help"
            )
            items(presets) { cmd ->
                SuggestionChip(
                    onClick = {
                        commandInput = cmd
                        viewModel.executeTelegramBotCommand(cmd)
                        commandInput = ""
                    },
                    label = { Text(cmd, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Slate900,
                        labelColor = ElectricBlue
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = Slate700
                    )
                )
            }
        }

        // Command Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = { Text("Escribe un comando... (ej. /transferir, /oxxopay)", color = Slate600, fontSize = 11.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("input_telegram_command"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900,
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Slate50,
                    unfocusedTextColor = Slate50
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Button(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        viewModel.executeTelegramBotCommand(commandInput)
                        commandInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(48.dp).testTag("btn_enviar_comando_telegram")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Slate950, modifier = Modifier.size(16.dp))
            }
        }
    }
}
