package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GatewayProvider
import com.example.data.model.RiskLevel
import com.example.data.model.TransactionStatus
import com.example.ui.theme.*

@Composable
fun StatusBadge(status: TransactionStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        TransactionStatus.APROBADA -> Triple(Emerald500.copy(alpha = 0.2f), Emerald400, "APROBADA")
        TransactionStatus.COMPLETADA -> Triple(Emerald500.copy(alpha = 0.2f), Emerald400, "COMPLETADA")
        TransactionStatus.BLOQUEADA -> Triple(Rose500.copy(alpha = 0.2f), Rose500, "BLOQUEADA")
        TransactionStatus.REQUIERE_2FA -> Triple(Amber500.copy(alpha = 0.2f), Amber400, "REQUIERE 2FA")
        TransactionStatus.PENDIENTE -> Triple(Cyan400.copy(alpha = 0.2f), Cyan400, "PENDIENTE")
        TransactionStatus.EN_REVISION -> Triple(Amber500.copy(alpha = 0.2f), Amber400, "EN REVISIÓN")
        TransactionStatus.EXPIRADA -> Triple(Slate700, Slate400, "EXPIRADA")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun RiskBadge(level: RiskLevel, score: Int, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (level) {
        RiskLevel.BAJO -> Triple(Emerald500.copy(alpha = 0.15f), Emerald400, "Bajo ($score/100)")
        RiskLevel.MEDIO -> Triple(Amber500.copy(alpha = 0.15f), Amber400, "Medio ($score/100)")
        RiskLevel.ALTO -> Triple(Rose500.copy(alpha = 0.2f), Rose500, "Alto ($score/100)")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(textColor)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProviderIconBadge(provider: GatewayProvider, modifier: Modifier = Modifier) {
    val (icon, tint, label, bg) = when (provider) {
        GatewayProvider.MERCADO_PAGO -> Quadruple(Icons.Default.QrCode2, MercadoPagoBlue, "Mercado Pago", Slate850)
        GatewayProvider.OXXO_PAY -> Quadruple(Icons.Default.ReceiptLong, Rose500, "OXXO Pay", Slate850)
        GatewayProvider.GOOGLE_PLAY_CARD -> Quadruple(Icons.Default.CardGiftcard, ElectricBlue, "Google Play", Slate850)
        GatewayProvider.BBVA_SPEI -> Quadruple(Icons.Default.AccountBalance, Indigo500, "BBVA SPEI", Slate850)
        GatewayProvider.AMAZON_PAY -> Quadruple(Icons.Default.ShoppingCart, Amber500, "Amazon Pay", Slate850)
        GatewayProvider.BITSO_CRYPTO -> Quadruple(Icons.Default.CurrencyBitcoin, Amber400, "Bitso Crypto", Slate850)
        GatewayProvider.BINANCE_PAY -> Quadruple(Icons.Default.Token, Amber500, "Binance Pay", Slate850)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, Slate700, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(16.dp))
        Text(text = label, color = Slate200, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun BarcodeVisualizer(
    barcodeNumber: String,
    modifier: Modifier = Modifier
) {
    val cleanDigits = barcodeNumber.ifEmpty { "93028491039218" }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("barcode_canvas")
        ) {
            val totalWidth = size.width
            val barCount = 55
            val step = totalWidth / barCount

            for (i in 0 until barCount) {
                // Pseudo-random bar pattern based on digits
                val digitIndex = i % cleanDigits.length
                val charVal = cleanDigits[digitIndex].code
                val isThick = (charVal + i) % 3 == 0
                val isSpace = (charVal + i) % 5 == 0

                if (!isSpace) {
                    val barThickness = if (isThick) step * 0.7f else step * 0.35f
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x = i * step, y = 0f),
                        size = Size(width = barThickness, height = size.height)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Formatted 14-digit spacing e.g. 9302 8491 0392 18
        val formatted = cleanDigits.chunked(4).joinToString(" ")
        Text(
            text = formatted,
            color = Color.Black,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Slate700, Slate800)))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Text(
                text = value,
                color = Slate50,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(text = subtitle, color = iconColor, fontSize = 10.sp)
        }
    }
}
