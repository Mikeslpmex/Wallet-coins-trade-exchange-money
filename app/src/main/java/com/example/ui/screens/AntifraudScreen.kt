package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AntifraudUmbralEntity
import com.example.data.model.IpBlacklistEntity
import com.example.data.model.TransactionStatus
import com.example.ui.components.RiskBadge
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayShieldViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun AntifraudScreen(
    viewModel: PayShieldViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    var newIpText by remember { mutableStateOf("") }
    var newIpReason by remember { mutableStateOf("") }

    val pending2FA = remember(uiState.transferencias) {
        uiState.transferencias.filter { it.estado == TransactionStatus.REQUIERE_2FA }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Brand Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Rose500, Indigo500)))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Rose500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Rose500, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text(
                            text = "NEURAFORGEAИ® ANTIFRAUD ENGINE",
                            color = Slate50,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Versión: 1.0.0 • Autor: NeuraforgeAI® Analytics",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Motor de detección en tiempo real para OrionCash & Multi-Gateway",
                            color = Cyan400,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Operaciones Bloqueadas",
                    value = "${uiState.bloqueadasCount}",
                    subtitle = "Riesgo Alto (>=70 pts)",
                    icon = Icons.Default.Block,
                    iconColor = Rose500,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Cola 2FA (Lucy Bot)",
                    value = "${uiState.requiere2FACount}",
                    subtitle = "Riesgo Medio (40-69 pts)",
                    icon = Icons.Default.VerifiedUser,
                    iconColor = Amber400,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2FA Review Queue (If any pending)
        if (pending2FA.isNotEmpty()) {
            item {
                Text(
                    text = "🚨 Cola de Confirmación 2FA Pendiente (${pending2FA.size})",
                    color = Amber400,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            items(pending2FA, key = { it.id }) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Amber500.copy(alpha = 0.5f)))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(tx.referenciaId, color = Slate400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            RiskBadge(level = tx.nivelRiesgo, score = tx.puntuacionRiesgo)
                        }
                        Text(
                            text = "${tx.destinatario} • $${tx.monto} ${tx.moneda}",
                            color = Slate50,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Alertas: ${tx.alertasRiesgo}",
                            color = Amber400,
                            fontSize = 11.sp
                        )
                        Button(
                            onClick = { viewModel.aprobar2FA(tx) },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(34.dp).testTag("btn_aprobar_2fa_card_${tx.id}")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Slate950, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aprobar Autorización 2FA de Lucy", color = Slate950, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Umbrales Configuration Section
        item {
            Text(
                text = "Umbrales & Reglas Heurísticas Activas",
                color = Slate50,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.umbrales, key = { it.clave }) { umbral ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate800))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(umbral.nombre, color = Slate50, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(umbral.descripcion, color = Slate400, fontSize = 11.sp)
                        if (umbral.valorNumerico > 0) {
                            Text(
                                text = "Valor configurado: ${if (umbral.clave.contains("monto")) "$${umbral.valorNumerico.toInt()} USD" else if (umbral.clave.contains("pct")) "${umbral.valorNumerico}%" else "${umbral.valorNumerico.toInt()}"}",
                                color = Cyan400,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Switch(
                        checked = umbral.habilitado,
                        onCheckedChange = { viewModel.toggleUmbral(umbral) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Slate950,
                            checkedTrackColor = ElectricBlue,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = Slate800
                        ),
                        modifier = Modifier.testTag("switch_umbral_${umbral.clave}")
                    )
                }
            }
        }

        // IP Blacklist Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Lista Negra de IPs (Neuraforge Blacklist)",
                    color = Slate50,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                // Add IP Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newIpText,
                                onValueChange = { newIpText = it },
                                placeholder = { Text("Ej. 192.168.1.1 o IP proxy", color = Slate600, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("input_blacklist_ip"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate850,
                                    unfocusedContainerColor = Slate850,
                                    focusedBorderColor = Rose500,
                                    unfocusedBorderColor = Slate700,
                                    focusedTextColor = Slate50,
                                    unfocusedTextColor = Slate50
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (newIpText.isNotBlank()) {
                                        viewModel.addBlacklistIp(newIpText, newIpReason)
                                        newIpText = ""
                                        newIpReason = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(48.dp).testTag("btn_agregar_ip_blacklist")
                            ) {
                                Text("Añadir", color = Slate50, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // List of Blacklisted IPs
        items(uiState.ipBlacklist, key = { it.ipAddress }) { ip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate800))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = Rose500, modifier = Modifier.size(16.dp))
                        Column {
                            Text(ip.ipAddress, color = Slate50, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text(ip.motivo, color = Slate400, fontSize = 10.sp)
                        }
                    }
                    IconButton(
                        onClick = { viewModel.removeBlacklistIp(ip) },
                        modifier = Modifier.testTag("btn_remover_ip_${ip.ipAddress}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Slate400, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
