package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.GatewayProvider
import com.example.data.model.TransactionStatus
import com.example.data.model.TransferenciaEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayShieldViewModel
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GatewayScreen(
    viewModel: PayShieldViewModel,
    uiState: UiState,
    onOpenNewTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("TODOS") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(uiState.transferencias, selectedFilter, searchQuery) {
        uiState.transferencias.filter { item ->
            val matchFilter = when (selectedFilter) {
                "MERCADO_PAGO" -> item.proveedor == GatewayProvider.MERCADO_PAGO
                "OXXO_PAY" -> item.proveedor == GatewayProvider.OXXO_PAY
                "GOOGLE_PLAY" -> item.proveedor == GatewayProvider.GOOGLE_PLAY_CARD
                "BBVA_SPEI" -> item.proveedor == GatewayProvider.BBVA_SPEI
                "BITSO_CRYPTO" -> item.proveedor == GatewayProvider.BITSO_CRYPTO || item.proveedor == GatewayProvider.BINANCE_PAY
                "BLOQUEADAS" -> item.estado == TransactionStatus.BLOQUEADA
                "REQUIERE_2FA" -> item.estado == TransactionStatus.REQUIERE_2FA
                else -> true
            }
            val matchSearch = searchQuery.isEmpty() ||
                    item.referenciaId.contains(searchQuery, ignoreCase = true) ||
                    item.destinatario.contains(searchQuery, ignoreCase = true) ||
                    item.usuarioId.contains(searchQuery, ignoreCase = true) ||
                    item.googleCardCode.contains(searchQuery, ignoreCase = true) ||
                    item.barcodeNumber.contains(searchQuery, ignoreCase = true)

            matchFilter && matchSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // KPI Summary Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Bóveda Tesorería",
                value = "$${String.format(Locale.US, "%,.2f", uiState.tesoreroBalanceUsd)}",
                subtitle = "USD Disponibles",
                icon = Icons.Default.AccountBalanceWallet,
                iconColor = Cyan400,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Tipo de Cambio",
                value = "$${String.format(Locale.US, "%.2f", uiState.liveRates.usdToMxn)}",
                subtitle = "MXN/USD Live",
                icon = Icons.Default.TrendingUp,
                iconColor = Emerald400,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Alertas Antifraude",
                value = "${uiState.bloqueadasCount}",
                subtitle = "Bloqueos activos",
                icon = Icons.Default.Shield,
                iconColor = if (uiState.bloqueadasCount > 0) Rose500 else Emerald400,
                modifier = Modifier.weight(1f)
            )
        }

        // Action Header & Search
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registro de Pasarela & Vouchers",
                color = Slate50,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onOpenNewTransfer,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_nueva_transferencia")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Nueva Operación", color = Slate950, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar por referencia, usuario, tarjeta, OXXO barcode...", color = Slate400, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("search_gateway_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Slate900,
                unfocusedContainerColor = Slate900,
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = Slate700,
                focusedTextColor = Slate50,
                unfocusedTextColor = Slate50
            ),
            singleLine = true
        )

        // Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filters = listOf(
                "TODOS" to "Todos (${uiState.transferencias.size})",
                "OXXO_PAY" to "OXXO Pay",
                "GOOGLE_PLAY" to "Google Cards",
                "MERCADO_PAGO" to "Mercado Pago",
                "BBVA_SPEI" to "BBVA SPEI",
                "BITSO_CRYPTO" to "Crypto",
                "REQUIERE_2FA" to "2FA Pendiente (${uiState.requiere2FACount})",
                "BLOQUEADAS" to "Bloqueadas (${uiState.bloqueadasCount})"
            )
            items(filters) { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = key },
                    label = { Text(text = label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBlue,
                        selectedLabelColor = Slate950,
                        containerColor = Slate900,
                        labelColor = Slate200
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) ElectricBlue else Slate700
                    )
                )
            }
        }

        // Transactions Feed
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate900)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Slate600, modifier = Modifier.size(40.dp))
                    Text("No hay transacciones registradas con este filtro", color = Slate400, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    TransactionItemCard(
                        item = item,
                        onClick = { viewModel.selectTransferencia(item) },
                        onSimulateOxxo = { viewModel.simularPagoOxxo(item) },
                        onApprove2FA = { viewModel.aprobar2FA(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(
    item: TransferenciaEntity,
    onClick: () -> Unit,
    onSimulateOxxo: () -> Unit,
    onApprove2FA: () -> Unit
) {
    val dateStr = remember(item.createdAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("tx_card_${item.referenciaId}"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate800))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Provider, Reference & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProviderIconBadge(provider = item.proveedor)
                    Text(
                        text = item.referenciaId,
                        color = Slate400,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                StatusBadge(status = item.estado)
            }

            // Middle Row: Destination & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.destinatario,
                        color = Slate50,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "Usuario: ${item.usuarioId} • $dateStr",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (item.moneda == "MXN" || item.moneda == "USD") "$" else ""}${String.format(Locale.US, "%,.2f", item.monto)} ${item.moneda}",
                        color = Slate50,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (item.totalACobrar > item.monto) {
                        Text(
                            text = "Total: $${String.format(Locale.US, "%,.2f", item.totalACobrar)} MXN",
                            color = Cyan400,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Quick Info Bar: Barcode / Google Card / Risk
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate850)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.googleCardCode.isNotEmpty()) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(14.dp))
                        Text(text = item.googleCardCode, color = ElectricBlue, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    } else if (item.barcodeNumber.isNotEmpty()) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = Rose500, modifier = Modifier.size(14.dp))
                        Text(text = "OXXO: ${item.barcodeNumber}", color = Slate200, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    } else if (item.txHash.isNotEmpty()) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = Amber400, modifier = Modifier.size(14.dp))
                        Text(text = "Tx: ${item.txHash.take(12)}...", color = Amber400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    } else {
                        Text(text = "IP: ${item.ipOrigen}", color = Slate400, fontSize = 11.sp)
                    }
                }
                RiskBadge(level = item.nivelRiesgo, score = item.puntuacionRiesgo)
            }

            // Action Trigger Buttons (If OXXO pending or 2FA needed)
            if (item.estado == TransactionStatus.PENDIENTE && item.proveedor == GatewayProvider.OXXO_PAY) {
                Button(
                    onClick = onSimulateOxxo,
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp).testTag("btn_simular_pago_oxxo_${item.id}")
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simular Escaneo y Pago en Caja OXXO", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else if (item.estado == TransactionStatus.REQUIERE_2FA) {
                Button(
                    onClick = onApprove2FA,
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp).testTag("btn_aprobar_2fa_${item.id}")
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Slate950, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Aprobar Autorización 2FA (Lucy Bot)", color = Slate950, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
