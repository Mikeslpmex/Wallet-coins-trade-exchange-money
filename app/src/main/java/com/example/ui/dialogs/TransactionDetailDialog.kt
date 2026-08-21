package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.GatewayProvider
import com.example.data.model.TransactionStatus
import com.example.data.model.TransferenciaEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayShieldViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionDetailDialog(
    transferencia: TransferenciaEntity,
    viewModel: PayShieldViewModel,
    aiResult: String?,
    isAiLoading: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateStr = remember(transferencia.createdAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(transferencia.createdAt))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("dialog_transaction_detail"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dialog Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProviderIconBadge(provider = transferencia.proveedor)
                        Text(
                            text = "Detalle de Operación",
                            color = Slate50,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Slate400)
                    }
                }

                // Reference ID & Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate850)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("FOLIO REFERENCIA:", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(transferencia.referenciaId, color = Slate50, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    StatusBadge(status = transferencia.estado)
                }

                // Amount & Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Monto Nominal:", color = Slate400, fontSize = 11.sp)
                        Text(
                            text = "$${String.format(Locale.US, "%,.2f", transferencia.monto)} ${transferencia.moneda}",
                            color = Slate50,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (transferencia.totalACobrar > 0 && transferencia.totalACobrar != transferencia.monto) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total a Cobrar:", color = Cyan400, fontSize = 11.sp)
                            Text(
                                text = "$${String.format(Locale.US, "%,.2f", transferencia.totalACobrar)} MXN",
                                color = Cyan400,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Barcode or Digital Code Visualizer
                if (transferencia.barcodeNumber.isNotEmpty()) {
                    Text("Código de Barras / Ficha:", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    BarcodeVisualizer(barcodeNumber = transferencia.barcodeNumber)
                }

                if (transferencia.googleCardCode.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricBlue.copy(alpha = 0.15f))
                            .border(1.dp, ElectricBlue, RoundedCornerShape(8.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Google Card", transferencia.googleCardCode))
                                Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                            }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CÓDIGO DIGITAL PLAY STORE:", color = ElectricBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(transferencia.googleCardCode, color = Slate50, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                    }
                }

                // Breakdown Metadata
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate850)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DetailRow("Destinatario:", transferencia.destinatario)
                    DetailRow("Usuario Emisor:", transferencia.usuarioId)
                    DetailRow("IP Origen:", transferencia.ipOrigen)
                    DetailRow("Fecha y Hora:", dateStr)
                    if (transferencia.clabeSpei.isNotEmpty()) DetailRow("CLABE SPEI:", transferencia.clabeSpei)
                    if (transferencia.mpPreferenceId.isNotEmpty()) DetailRow("Mercado Pago Pref:", transferencia.mpPreferenceId)
                    if (transferencia.txHash.isNotEmpty()) DetailRow("Crypto TxHash:", "${transferencia.txHash.take(16)}...")
                }

                // Antifraud Evaluation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Evaluación Neuraforge Antifraude:", color = Slate200, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            RiskBadge(level = transferencia.nivelRiesgo, score = transferencia.puntuacionRiesgo)
                        }
                        Text(
                            text = transferencia.alertasRiesgo,
                            color = if (transferencia.puntuacionRiesgo >= 40) Amber400 else Slate400,
                            fontSize = 11.sp
                        )
                    }
                }

                // Gemini AI Deep Inspection Section
                Button(
                    onClick = { viewModel.analyzeWithAI(transferencia) },
                    enabled = !isAiLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("btn_gemini_ai_audit")
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analizando patrones con Gemini AI...", color = Color.White, fontSize = 11.sp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auditoría Inteligente Gemini AI (Deep Risk)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (aiResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate950),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Indigo500.copy(alpha = 0.5f)))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = Indigo500, modifier = Modifier.size(16.dp))
                                Text("Dictamen Neuraforge AI Analytics:", color = Indigo500, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(aiResult, color = Slate200, fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }

                // Action simulation buttons
                if (transferencia.estado == TransactionStatus.PENDIENTE && transferencia.proveedor == GatewayProvider.OXXO_PAY) {
                    Button(
                        onClick = {
                            viewModel.simularPagoOxxo(transferencia)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simular Escaneo en Tienda OXXO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (transferencia.estado == TransactionStatus.REQUIERE_2FA) {
                    Button(
                        onClick = {
                            viewModel.aprobar2FA(transferencia)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Slate950)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aprobar Autorización 2FA (Lucy Bot)", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Slate400, fontSize = 11.sp)
        Text(value, color = Slate200, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
    }
}
