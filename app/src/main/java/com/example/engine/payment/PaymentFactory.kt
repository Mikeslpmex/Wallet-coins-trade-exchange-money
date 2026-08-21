package com.example.engine.payment

import com.example.data.model.GatewayProvider
import com.example.data.model.TransactionType
import com.example.data.model.TransferenciaEntity
import com.example.data.model.TransactionStatus
import com.example.data.model.RiskLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class PaymentExecutionResult(
    val success: Boolean,
    val referenciaId: String,
    val barcodeNumber: String = "",
    val googleCardCode: String = "",
    val clabeSpei: String = "",
    val mpPreferenceId: String = "",
    val txHash: String = "",
    val totalACobrar: Double,
    val comisionPlataforma: Double,
    val oxxoComision: Double = 0.0,
    val mensaje: String,
    val error: String? = null
)

object PaymentFactory {

    fun procesarPago(
        proveedor: GatewayProvider,
        tipo: TransactionType,
        usuarioId: String,
        destinatario: String,
        monto: Double,
        moneda: String,
        evaluacionNivel: RiskLevel
    ): PaymentExecutionResult {
        val refId = when (proveedor) {
            GatewayProvider.OXXO_PAY -> "OX-${System.currentTimeMillis().toString().takeLast(10)}"
            GatewayProvider.GOOGLE_PLAY_CARD -> "GPLY-${System.currentTimeMillis().toString().takeLast(8)}"
            GatewayProvider.MERCADO_PAGO -> "MP-${System.currentTimeMillis().toString().takeLast(8)}"
            GatewayProvider.BBVA_SPEI -> "SPEI-${System.currentTimeMillis().toString().takeLast(8)}"
            GatewayProvider.AMAZON_PAY -> "AMZN-${System.currentTimeMillis().toString().takeLast(8)}"
            GatewayProvider.BITSO_CRYPTO -> "BITSO-${System.currentTimeMillis().toString().takeLast(8)}"
            GatewayProvider.BINANCE_PAY -> "BNB-${System.currentTimeMillis().toString().takeLast(8)}"
        }

        if (evaluacionNivel == RiskLevel.ALTO) {
            return PaymentExecutionResult(
                success = false,
                referenciaId = refId,
                totalACobrar = monto,
                comisionPlataforma = 0.0,
                mensaje = "Operación rechazada por el motor antifraude NeuraForge.",
                error = "Transferencia bloqueada por seguridad preventiva"
            )
        }

        return when (proveedor) {
            GatewayProvider.OXXO_PAY -> {
                // Generate 14-digit OXXO Pay Barcode Number
                val randomDigits = (1..14).map { Random.nextInt(0, 10) }.joinToString("")
                val oxxoFee = 15.0
                val platformFee = (monto * 0.035) // 3.5% monetization
                val total = monto + oxxoFee + platformFee
                PaymentExecutionResult(
                    success = true,
                    referenciaId = refId,
                    barcodeNumber = randomDigits,
                    oxxoComision = oxxoFee,
                    comisionPlataforma = platformFee,
                    totalACobrar = total,
                    mensaje = "Ficha OXXO Pay generada con éxito. Escanea el código de barras en caja OXXO para pagar $$total MXN (incluye comisión OXXO de $$oxxoFee MXN)."
                )
            }
            GatewayProvider.GOOGLE_PLAY_CARD -> {
                // Generate Google Play Card Serial (e.g., GPLY-4819-2094-1184)
                val chunk1 = Random.nextInt(1000, 9999)
                val chunk2 = Random.nextInt(1000, 9999)
                val chunk3 = Random.nextInt(1000, 9999)
                val cardCode = "GPLY-$chunk1-$chunk2-$chunk3"
                val barcode = "${chunk1}${chunk2}${chunk3}"
                val platformFee = (monto * 0.035)
                val total = monto + platformFee
                PaymentExecutionResult(
                    success = true,
                    referenciaId = refId,
                    googleCardCode = cardCode,
                    barcodeNumber = barcode,
                    comisionPlataforma = platformFee,
                    totalACobrar = total,
                    mensaje = "Tarjeta digital Google Play emitida instantáneamente ($cardCode). Listo para recargar saldo de Play Store / Google Pay."
                )
            }
            GatewayProvider.MERCADO_PAGO -> {
                val prefId = "pref_${Random.nextInt(10000000, 99999999)}"
                val platformFee = (monto * 0.029) + 4.0
                val total = monto
                PaymentExecutionResult(
                    success = true,
                    referenciaId = refId,
                    mpPreferenceId = prefId,
                    comisionPlataforma = platformFee,
                    totalACobrar = total,
                    mensaje = "Preferencia Checkout Mercado Pago creada ($prefId). Listo para procesar cobro QR o Link."
                )
            }
            GatewayProvider.BBVA_SPEI -> {
                val clabe = "012" + (1..15).map { Random.nextInt(0, 10) }.joinToString("")
                PaymentExecutionResult(
                    success = true,
                    referenciaId = refId,
                    clabeSpei = clabe,
                    totalACobrar = monto,
                    comisionPlataforma = 0.0,
                    mensaje = "CLABE Interbancaria BBVA generada ($clabe). Transferencia interbancaria SPEI acreditada en segundos."
                )
            }
            GatewayProvider.AMAZON_PAY -> {
                val chunk1 = Random.nextInt(1000, 9999)
                val chunk2 = Random.nextInt(1000, 9999)
                val cardCode = "AMZN-$chunk1-$chunk2"
                PaymentExecutionResult(
                    success = true,
                    referenciaId = refId,
                    googleCardCode = cardCode,
                    totalACobrar = monto,
                    comisionPlataforma = monto * 0.025,
                    mensaje = "Vale digital Amazon Pay generado ($cardCode)."
                )
            }
            GatewayProvider.BITSO_CRYPTO, GatewayProvider.BINANCE_PAY -> {
                val simulatedHash = "0x" + (1..64).map { "0123456789abcdef".random() }.joinToString("")
                PaymentExecutionResult(
                    success = true,
                    referenciaId = refId,
                    txHash = simulatedHash,
                    totalACobrar = monto,
                    comisionPlataforma = 0.0,
                    mensaje = "Transferencia crypto procesada exitosamente en ${proveedor.name}. Hash: ${simulatedHash.take(16)}..."
                )
            }
        }
    }
}
