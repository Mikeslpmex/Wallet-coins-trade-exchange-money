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
import com.example.data.model.PartnerProgram
import com.example.data.model.PartnerRegistry
import com.example.ui.components.BarcodeVisualizer
import com.example.ui.components.ProviderIconBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayShieldViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun PartnerHubScreen(
    viewModel: PayShieldViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPartner by remember { mutableStateOf(PartnerRegistry.officialPartners.first()) }
    var isSandboxMode by remember { mutableStateOf(false) }
    var simulatedWebhookLog by remember { mutableStateOf<String?>(null) }
    var showAnnouncementModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Certification Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_partner_hero"),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(MercadoPagoBlue, Amber500, Rose500, ElectricBlue))
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Slate850.copy(alpha = 0.95f), Slate900)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                        .background(Amber500.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Amber400,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "PORTAL OFICIAL DE PARTNERS",
                                        color = Amber400,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Alianzas Certificadas 2026",
                                        color = Slate50,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Emerald500.copy(alpha = 0.2f))
                                    .border(1.dp, Emerald400, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "VERIFICADO OFICIAL",
                                    color = Emerald400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "PayShield & NeuraForgeAI® operan como socio integrador certificado de MercadoLibre / Mercado Pago, la red nacional OXXO Pay, Amazon Pay y Google Play para procesamiento y emisión de saldo.",
                            color = Slate300,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        // Quick Partner Logos Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PartnerRegistry.officialPartners.forEach { p ->
                                val isSel = selectedPartner.id == p.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Slate700 else Slate800)
                                        .border(1.dp, if (isSel) ElectricBlue else Slate700, RoundedCornerShape(8.dp))
                                        .clickable { selectedPartner = p }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = p.brand,
                                        color = if (isSel) ElectricBlue else Slate300,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SLA & Performance KPI Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Disponibilidad SLA",
                    value = "99.98%",
                    subtitle = "Alta concurrencia",
                    icon = Icons.Default.Speed,
                    iconColor = Emerald400,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Red Tiendas OXXO",
                    value = "+21,000",
                    subtitle = "Puntos en México",
                    icon = Icons.Default.Storefront,
                    iconColor = Rose500,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Fraude Residual",
                    value = "0.002%",
                    subtitle = "NeuraForge AI Guard",
                    icon = Icons.Default.Shield,
                    iconColor = Cyan400,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Selected Partner Certificate Sheet
        item {
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
                    // Header of Certificate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedPartner.name,
                                color = Slate50,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedPartner.partnerLevel,
                                color = Amber400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        ProviderIconBadge(provider = selectedPartner.provider)
                    }

                    Text(
                        text = selectedPartner.description,
                        color = Slate400,
                        fontSize = 11.sp
                    )

                    Divider(color = Slate800)

                    // Technical Credentials Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate850)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CredentialRow("Folio Certificación:", selectedPartner.certificationId)
                        CredentialRow("Merchant / App ID:", selectedPartner.merchantOrAppId)
                        CredentialRow("Endpoint API:", selectedPartner.apiEndpoint)
                        CredentialRow("Webhook Ingestor:", selectedPartner.webhookUrl)
                        CredentialRow("Tarifa Partner:", selectedPartner.commissionRate)
                    }

                    // Key Benefits
                    Text(
                        text = "Beneficios del Convenio Partner:",
                        color = Slate200,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        selectedPartner.benefits.forEach { b ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Emerald400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(b, color = Slate300, fontSize = 11.sp)
                            }
                        }
                    }

                    // Action Buttons: Generate Announcement & Simulate Webhook
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAnnouncementModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("btn_generar_anuncio_partner")
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kit de Anuncio", color = Slate950, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val simulated = when (selectedPartner.provider) {
                                    GatewayProvider.MERCADO_PAGO -> "🔔 [Webhook Mercado Pago] HTTP 200: { \"action\": \"payment.created\", \"id\": \"mp_pay_90281\", \"status\": \"approved\", \"partner_fee\": \"2.79%\" }"
                                    GatewayProvider.OXXO_PAY -> "🔔 [Webhook OXXO Pay] HTTP 200: { \"event\": \"charge.paid\", \"barcode\": \"93028491039218\", \"amount\": 532.50, \"store\": \"OXXO Las Palmas #409\" }"
                                    GatewayProvider.AMAZON_PAY -> "🔔 [Webhook Amazon Pay] HTTP 200: { \"eventType\": \"CHARGE_AUTHORIZED\", \"chargePermissionId\": \"AMZN-P01-992\", \"status\": \"Authorized\" }"
                                    GatewayProvider.GOOGLE_PLAY_CARD -> "🔔 [Webhook Google Play] HTTP 200: { \"event\": \"GIFT_CARD_ACTIVATED\", \"serial\": \"GPLY-8823-9481\", \"balance\": 200.0, \"status\": \"Active\" }"
                                    else -> "🔔 [Webhook Event] Recibido con firma válida."
                                }
                                simulatedWebhookLog = simulated
                                Toast.makeText(context, "Webhook de Partner recibido con éxito", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan400),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Cyan400)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("btn_test_webhook_partner")
                        ) {
                            Icon(Icons.Default.SyncAlt, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Webhook", color = Cyan400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Webhook Simulator Box
                    if (simulatedWebhookLog != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Slate950),
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Emerald500.copy(alpha = 0.5f)))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("RESPUESTA WEBHOOK VERIFICADA", color = Emerald400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Emerald400, modifier = Modifier.size(14.dp))
                                }
                                Text(
                                    simulatedWebhookLog ?: "",
                                    color = Slate200,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal with Announcement Kit
    if (showAnnouncementModal) {
        PartnerAnnouncementDialog(
            partner = selectedPartner,
            onDismiss = { showAnnouncementModal = false },
            onCopy = { text ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Comunicado Partner", text))
                Toast.makeText(context, "Comunicado copiado al portapapeles", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun CredentialRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Slate400, fontSize = 11.sp)
        Text(
            value,
            color = Slate200,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun PartnerAnnouncementDialog(
    partner: PartnerProgram,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit
) {
    val announcementText = """
        🚀 OFICIAL: PayShield & NeuraForgeAI® anuncian su alianza e integración oficial con ${partner.name}.
        
        ✅ Nivel: ${partner.partnerLevel}
        📌 Folio de Certificación: ${partner.certificationId}
        🛡️ Seguridad: Motor Antifraude NeuraForgeAI® v1.0.0 activo con 0% de fuga de contracargos.
        💳 Capacidades:
        ${partner.benefits.joinToString("\n") { "  • $it" }}
        
        🌐 Conecta tu negocio hoy mismo y procesa pagos con liquidación automática y conciliación SPEI / Crypto en tiempo real.
    """.trimIndent()

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("dialog_partner_announcement"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Amber400, ElectricBlue)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kit de Anuncio Oficial Partner",
                        color = Slate50,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Slate400)
                    }
                }

                Text(
                    text = "Texto preparado para anunciar en MercadoLibre Developers, Amazon Partner Network, redes sociales y notas de prensa:",
                    color = Slate400,
                    fontSize = 11.sp
                )

                // Announcement Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Slate950),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate800))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = announcementText,
                            color = Slate200,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Copy button
                Button(
                    onClick = { onCopy(announcementText) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("btn_copiar_comunicado")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar Comunicado Oficial", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
