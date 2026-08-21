package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.GatewayProvider
import com.example.data.model.TransactionType
import com.example.ui.components.ProviderIconBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayShieldViewModel

@Composable
fun NewTransferDialog(
    viewModel: PayShieldViewModel,
    onDismiss: () -> Unit
) {
    var selectedProvider by remember { mutableStateOf(GatewayProvider.OXXO_PAY) }
    var usuarioId by remember { mutableStateOf("usuario_demo_101") }
    var destinatario by remember { mutableStateOf("Cajero OXXO / Recarga de Saldo") }
    var montoText by remember { mutableStateOf("500") }
    var moneda by remember { mutableStateOf("MXN") }
    var ipOrigen by remember { mutableStateOf("192.168.1.45") }
    var redCrypto by remember { mutableStateOf("ETH") }

    val monto = montoText.toDoubleOrNull() ?: 0.0

    // Dynamic pre-flight risk calculation
    val isHighRiskAmount = (moneda == "USD" && monto > 5000) || (moneda == "MXN" && monto > 100000)
    val isBlacklistedIp = (ipOrigen == "192.168.1.1")
    val estimatedScore = (if (isHighRiskAmount) 20 else 0) + (if (isBlacklistedIp) 30 else 0) + 30 // new recipient

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("dialog_new_transfer"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nueva Operación de Pago / Retiro",
                        color = Slate50,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Slate400)
                    }
                }

                // Provider Selector
                Text("Selecciona el Proveedor / Pasarela:", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val providers = listOf(
                        GatewayProvider.OXXO_PAY to "OXXO Pay (Ficha con Código de Barras)",
                        GatewayProvider.GOOGLE_PLAY_CARD to "Tarjeta Google Play / Google Pay",
                        GatewayProvider.MERCADO_PAGO to "Mercado Pago (Checkout Pro / QR)",
                        GatewayProvider.BBVA_SPEI to "BBVA Bancomer SPEI Interbancario",
                        GatewayProvider.AMAZON_PAY to "Amazon Pay / Vale Digital",
                        GatewayProvider.BITSO_CRYPTO to "Bitso Crypto (ETH, BTC, USDT)"
                    )
                    providers.forEach { (prov, desc) ->
                        val isSel = selectedProvider == prov
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Slate800 else Slate850)
                                .clickable {
                                    selectedProvider = prov
                                    when (prov) {
                                        GatewayProvider.OXXO_PAY -> {
                                            moneda = "MXN"
                                            destinatario = "Cajero OXXO / Conciliación"
                                        }
                                        GatewayProvider.GOOGLE_PLAY_CARD -> {
                                            moneda = "MXN"
                                            destinatario = "Google Play Balance (usuario@gmail.com)"
                                        }
                                        GatewayProvider.MERCADO_PAGO -> {
                                            moneda = "MXN"
                                            destinatario = "Checkout Mercado Pago"
                                        }
                                        GatewayProvider.BBVA_SPEI -> {
                                            moneda = "MXN"
                                            destinatario = "CLABE: 012180015682910483 (BBVA)"
                                        }
                                        GatewayProvider.AMAZON_PAY -> {
                                            moneda = "MXN"
                                            destinatario = "Amazon Pay Balance"
                                        }
                                        GatewayProvider.BITSO_CRYPTO -> {
                                            moneda = "USDT"
                                            destinatario = "0x20a218EABE02Fe04ff0Fe499BA0e7D4d8128795B"
                                        }
                                        else -> {}
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = isSel,
                                onClick = { selectedProvider = prov },
                                colors = RadioButtonDefaults.colors(selectedColor = ElectricBlue, unselectedColor = Slate600)
                            )
                            Column {
                                ProviderIconBadge(provider = prov)
                                Text(desc, color = Slate400, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Monto & Moneda
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = montoText,
                        onValueChange = { montoText = it },
                        label = { Text("Monto", fontSize = 11.sp) },
                        modifier = Modifier.weight(1.5f).testTag("input_new_transfer_monto"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate850,
                            unfocusedContainerColor = Slate850,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate50,
                            unfocusedTextColor = Slate50
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = moneda,
                        onValueChange = { moneda = it.uppercase() },
                        label = { Text("Moneda", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).testTag("input_new_transfer_moneda"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate850,
                            unfocusedContainerColor = Slate850,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate50,
                            unfocusedTextColor = Slate50
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                // Destinatario
                OutlinedTextField(
                    value = destinatario,
                    onValueChange = { destinatario = it },
                    label = { Text("Destino / Email / Wallet / CLABE", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("input_new_transfer_dest"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate850,
                        unfocusedContainerColor = Slate850,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate50,
                        unfocusedTextColor = Slate50
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Usuario Emisor & IP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = usuarioId,
                        onValueChange = { usuarioId = it },
                        label = { Text("ID Usuario", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate850,
                            unfocusedContainerColor = Slate850,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate50,
                            unfocusedTextColor = Slate50
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ipOrigen,
                        onValueChange = { ipOrigen = it },
                        label = { Text("IP Origen", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).testTag("input_new_transfer_ip"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate850,
                            unfocusedContainerColor = Slate850,
                            focusedBorderColor = if (isBlacklistedIp) Rose500 else ElectricBlue,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate50,
                            unfocusedTextColor = Slate50
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                // Pre-flight Risk Preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (estimatedScore >= 70) Rose500.copy(alpha = 0.2f) else if (estimatedScore >= 40) Amber500.copy(alpha = 0.2f) else Emerald500.copy(alpha = 0.2f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Pre-evaluación Neuraforge Antifraude:",
                        color = if (estimatedScore >= 70) Rose500 else if (estimatedScore >= 40) Amber400 else Emerald400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Score estimado: $estimatedScore/100 • ${if (estimatedScore >= 70) "🚨 ALTO (Se bloqueará automáticamente)" else if (estimatedScore >= 40) "🔐 MEDIO (Requerirá 2FA en Lucy Bot)" else "✅ BAJO (Aprobada para ejecución directa)"}",
                        color = Slate200,
                        fontSize = 10.sp
                    )
                }

                // Submit Button
                Button(
                    onClick = {
                        val tipo = when (selectedProvider) {
                            GatewayProvider.OXXO_PAY -> TransactionType.OXXO_CASH_VOUCHER
                            GatewayProvider.GOOGLE_PLAY_CARD -> TransactionType.GOOGLE_CARD_PURCHASE
                            GatewayProvider.BITSO_CRYPTO, GatewayProvider.BINANCE_PAY -> TransactionType.CRYPTO_WITHDRAWAL
                            GatewayProvider.BBVA_SPEI -> TransactionType.SPEI_DEPOSIT
                            else -> TransactionType.PAYMENT_GATEWAY
                        }
                        viewModel.executeTransferencia(
                            usuarioId = usuarioId,
                            destinatario = destinatario,
                            monto = monto,
                            moneda = moneda,
                            proveedor = selectedProvider,
                            tipo = tipo,
                            ipOrigen = ipOrigen,
                            redCrypto = if (selectedProvider == GatewayProvider.BITSO_CRYPTO) redCrypto else null
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("btn_confirmar_nueva_transferencia")
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Slate950)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ejecutar Operación con NeuraForge", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
