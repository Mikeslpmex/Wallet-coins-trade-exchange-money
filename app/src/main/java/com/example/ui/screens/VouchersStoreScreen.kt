package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GatewayProvider
import com.example.data.model.TransactionStatus
import com.example.data.model.TransferenciaEntity
import com.example.ui.components.BarcodeVisualizer
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayShieldViewModel
import com.example.ui.viewmodel.UiState
import java.util.Locale

@Composable
fun VouchersStoreScreen(
    viewModel: PayShieldViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedProduct by remember { mutableStateOf("GOOGLE_PLAY") } // "GOOGLE_PLAY", "OXXO_PAY", "AMAZON_PAY"
    var selectedAmount by remember { mutableStateOf(200.0) }
    var customAmountText by remember { mutableStateOf("") }
    var recipientEmail by remember { mutableStateOf("usuario.gamer@gmail.com") }

    val effectiveAmount = if (customAmountText.isNotEmpty()) {
        customAmountText.toDoubleOrNull() ?: selectedAmount
    } else {
        selectedAmount
    }

    // Monetization calculations
    val oxxoFee = if (selectedProduct == "OXXO_PAY") 15.0 else 0.0
    val platformFee = effectiveAmount * 0.035 // 3.5%
    val totalToPay = effectiveAmount + oxxoFee + platformFee

    val latestVoucher = uiState.lastGeneratedVoucher ?: uiState.transferencias.firstOrNull {
        it.proveedor == GatewayProvider.GOOGLE_PLAY_CARD || it.proveedor == GatewayProvider.OXXO_PAY
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Emisión de Tarjetas Digitales & Vouchers",
                color = Slate50,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Genera códigos instantáneos Google Play y fichas OXXO Pay con código de barras para pago en tienda física.",
                color = Slate400,
                fontSize = 12.sp
            )
        }

        // Product Selector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Slate900)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val products = listOf(
                Triple("GOOGLE_PLAY", "Google Play", Icons.Default.CardGiftcard),
                Triple("OXXO_PAY", "OXXO Pay", Icons.Default.Storefront),
                Triple("AMAZON_PAY", "Amazon Card", Icons.Default.ShoppingCart)
            )
            products.forEach { (key, label, icon) ->
                val isSelected = selectedProduct == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ElectricBlue else Color.Transparent)
                        .clickable { selectedProduct = key }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isSelected) Slate950 else Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = label,
                            color = if (isSelected) Slate950 else Slate400,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Voucher Generator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Slate700, Slate800)))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "1. Selecciona el Monto de Recarga (MXN)",
                    color = Slate200,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Quick Amount chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val amounts = listOf(100.0, 200.0, 500.0, 1000.0)
                    amounts.forEach { amt ->
                        val isSel = (effectiveAmount == amt && customAmountText.isEmpty())
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Cyan400.copy(alpha = 0.2f) else Slate850)
                                .border(1.dp, if (isSel) Cyan400 else Slate700, RoundedCornerShape(8.dp))
                            .clickable {
                                selectedAmount = amt
                                customAmountText = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$${amt.toInt()}",
                            color = if (isSel) Cyan400 else Slate200,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Custom amount & email
            OutlinedTextField(
                value = customAmountText,
                onValueChange = { customAmountText = it },
                label = { Text("Monto personalizado (MXN)", fontSize = 11.sp) },
                placeholder = { Text("Ej. 350.00", color = Slate600) },
                modifier = Modifier.fillMaxWidth().testTag("input_voucher_custom_amount"),
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

            if (selectedProduct == "GOOGLE_PLAY") {
                OutlinedTextField(
                    value = recipientEmail,
                    onValueChange = { recipientEmail = it },
                    label = { Text("Correo destino para activación", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("input_voucher_email"),
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

            // Breakdown Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate850)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Valor Nominal del Saldo:", color = Slate400, fontSize = 11.sp)
                    Text("$${String.format(Locale.US, "%,.2f", effectiveAmount)} MXN", color = Slate200, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                if (oxxoFee > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Comisión de Caja OXXO:", color = Rose500, fontSize = 11.sp)
                        Text("+$${String.format(Locale.US, "%.2f", oxxoFee)} MXN", color = Rose500, fontSize = 11.sp)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Margen Monetización (3.5%):", color = Amber400, fontSize = 11.sp)
                    Text("+$${String.format(Locale.US, "%.2f", platformFee)} MXN", color = Amber400, fontSize = 11.sp)
                }
                Divider(color = Slate700, modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total a Cobrar al Cliente:", color = Slate50, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("$${String.format(Locale.US, "%,.2f", totalToPay)} MXN", color = ElectricBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            // Generate Button
            Button(
                onClick = {
                    if (selectedProduct == "OXXO_PAY") {
                        viewModel.generateOxxoPayVoucher("usuario_tienda", effectiveAmount, "Ficha en Tienda")
                    } else if (selectedProduct == "GOOGLE_PLAY") {
                        viewModel.generateGooglePlayCard("usuario_gamer", effectiveAmount, recipientEmail)
                    } else {
                        viewModel.executeTransferencia(
                            usuarioId = "usuario_amazon",
                            destinatario = "Amazon Pay Balance",
                            monto = effectiveAmount,
                            moneda = "MXN",
                            proveedor = GatewayProvider.AMAZON_PAY,
                            tipo = com.example.data.model.TransactionType.GOOGLE_CARD_PURCHASE
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_emitir_tarjeta_voucher")
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = Slate950)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedProduct == "OXXO_PAY") "Generar Ficha OXXO Pay con Código de Barras" else "Emitir Tarjeta Digital Instantánea",
                    color = Slate950,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }

    // Active Voucher Holographic Preview
    if (latestVoucher != null) {
        VoucherPreviewCard(
            voucher = latestVoucher,
            onSimulateOxxoPay = { viewModel.simularPagoOxxo(latestVoucher) },
            onCopyCode = { code ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Código Voucher", code))
                Toast.makeText(context, "Código copiado al portapapeles", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
}

@Composable
fun VoucherPreviewCard(
    voucher: TransferenciaEntity,
    onSimulateOxxoPay: () -> Unit,
    onCopyCode: (String) -> Unit
) {
    val isOxxo = voucher.proveedor == GatewayProvider.OXXO_PAY
    val isGoogle = voucher.proveedor == GatewayProvider.GOOGLE_PLAY_CARD

    val cardGradient = if (isOxxo) {
        Brush.linearGradient(listOf(Color(0xFF991B1B), Color(0xFF7F1D1D), Color(0xFF1E293B)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF0C4A6E), Color(0xFF0369A1), Color(0xFF1E293B)))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voucher_preview_card"),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Cyan400, ElectricBlue)))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Brand Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isOxxo) Icons.Default.Storefront else Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = if (isOxxo) Rose500 else Cyan400,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = if (isOxxo) "OXXO PAY® COMPROBANTE DIGITAL" else "GOOGLE PLAY CARD DIGITAL",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Folio: ${voucher.referenciaId}",
                                color = Slate300,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    StatusBadge(status = voucher.estado)
                }

                // Balance Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("Monto Recarga:", color = Slate300, fontSize = 10.sp)
                        Text(
                            text = "$${String.format(Locale.US, "%,.2f", voucher.monto)} MXN",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (voucher.totalACobrar > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total a Pagar en Caja:", color = Amber400, fontSize = 10.sp)
                            Text(
                                text = "$${String.format(Locale.US, "%,.2f", voucher.totalACobrar)} MXN",
                                color = Amber400,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Barcode Visualizer for in-store cashier scanning
                val codeToDisplay = if (isGoogle) voucher.googleCardCode else voucher.barcodeNumber
                BarcodeVisualizer(barcodeNumber = if (isOxxo) voucher.barcodeNumber else voucher.barcodeNumber)

                // Serial Code text box
                if (isGoogle && voucher.googleCardCode.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { onCopyCode(voucher.googleCardCode) }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CÓDIGO DE CANJE PLAY STORE:", color = Cyan400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(voucher.googleCardCode, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = Cyan400, modifier = Modifier.size(18.dp))
                    }
                }

                // Instructions Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("Instrucciones para el Usuario:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    if (isOxxo) {
                        Text("1. Acude a cualquier tienda OXXO en México.", color = Slate300, fontSize = 10.sp)
                        Text("2. Muestra este código de barras al cajero para escanear.", color = Slate300, fontSize = 10.sp)
                        Text("3. Paga el monto exacto en efectivo más la comisión de $15 MXN.", color = Slate300, fontSize = 10.sp)
                    } else {
                        Text("1. Abre Google Play Store > Canjear Código.", color = Slate300, fontSize = 10.sp)
                        Text("2. Pega el número de serie anterior para recargar saldo de inmediato.", color = Slate300, fontSize = 10.sp)
                    }
                }

                // Cashier simulation button
                if (isOxxo && voucher.estado == TransactionStatus.PENDIENTE) {
                    Button(
                        onClick = onSimulateOxxoPay,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp).testTag("btn_simular_cajero_oxxo")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simular Escaneo y Pago en Caja OXXO", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
