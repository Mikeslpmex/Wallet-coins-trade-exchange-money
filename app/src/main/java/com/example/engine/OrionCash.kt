package com.example.engine

import com.example.data.local.PayShieldDatabase
import com.example.data.model.*
import com.example.engine.payment.PaymentExecutionResult
import com.example.engine.payment.PaymentFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class OrionTransferResult(
    val success: Boolean,
    val transferencia: TransferenciaEntity,
    val paymentResult: PaymentExecutionResult,
    val evaluacion: AntifraudEvaluation,
    val error: String? = null
)

class OrionCash(
    private val antifraud: NeuraforgeAntifraudEngine,
    private val database: PayShieldDatabase
) {
    suspend fun transferir(
        usuarioId: String,
        destinatario: String,
        monto: Double,
        moneda: String,
        proveedor: GatewayProvider,
        tipo: TransactionType,
        ipOrigen: String? = "192.168.1.45",
        redCrypto: String? = null
    ): OrionTransferResult = withContext(Dispatchers.IO) {
        // 1. Evaluar riesgo con NeuraForge Antifraud Engine
        val evaluacion = antifraud.evaluarTransferencia(
            usuarioId = usuarioId,
            destinatario = destinatario,
            monto = monto,
            moneda = moneda,
            ipOrigen = ipOrigen,
            redCrypto = redCrypto
        )

        // 2. Procesar pago según proveedor
        val paymentResult = PaymentFactory.procesarPago(
            proveedor = proveedor,
            tipo = tipo,
            usuarioId = usuarioId,
            destinatario = destinatario,
            monto = monto,
            moneda = moneda,
            evaluacionNivel = evaluacion.nivel
        )

        // 3. Determinar estado de la transferencia
        val estado = when {
            evaluacion.nivel == RiskLevel.ALTO -> TransactionStatus.BLOQUEADA
            evaluacion.nivel == RiskLevel.MEDIO -> TransactionStatus.REQUIERE_2FA
            !paymentResult.success -> TransactionStatus.BLOQUEADA
            tipo == TransactionType.OXXO_CASH_VOUCHER -> TransactionStatus.PENDIENTE
            else -> TransactionStatus.COMPLETADA
        }

        // 4. Registrar en base de datos Room
        val transferencia = TransferenciaEntity(
            referenciaId = paymentResult.referenciaId,
            usuarioId = usuarioId,
            destinatario = destinatario,
            monto = monto,
            moneda = moneda,
            proveedor = proveedor,
            tipo = tipo,
            estado = estado,
            nivelRiesgo = evaluacion.nivel,
            puntuacionRiesgo = evaluacion.puntuacion,
            alertasRiesgo = evaluacion.alertas.joinToString("; ").ifEmpty { "Verificación NeuraForge limpia (Riesgo Bajo)" },
            ipOrigen = ipOrigen ?: "192.168.1.45",
            barcodeNumber = paymentResult.barcodeNumber,
            googleCardCode = paymentResult.googleCardCode,
            txHash = paymentResult.txHash,
            oxxoComision = paymentResult.oxxoComision,
            comisionPlataforma = paymentResult.comisionPlataforma,
            totalACobrar = paymentResult.totalACobrar,
            mpPreferenceId = paymentResult.mpPreferenceId,
            clabeSpei = paymentResult.clabeSpei,
            notas = paymentResult.mensaje
        )

        val id = database.transferenciaDao().insertTransferencia(transferencia)
        val savedTransferencia = transferencia.copy(id = id)

        // 5. Si es egreso del tesorero y fue exitoso, registrar en tesorero
        if (estado == TransactionStatus.COMPLETADA && (tipo == TransactionType.CRYPTO_WITHDRAWAL || tipo == TransactionType.CLOSING_CYCLE_MP)) {
            val montoUsd = if (moneda.equals("MXN", ignoreCase = true)) monto / 19.85 else monto
            val curBalance = database.tesoreroDao().getLatestBalance() ?: 0.0
            val newBalance = curBalance - montoUsd
            database.tesoreroDao().insertRegistro(
                TesoreroRegistroEntity(
                    montoUSD = -montoUsd,
                    concepto = "Egreso OrionCash ($destinatario) - ${proveedor.name}",
                    categoria = "EGRESO_ORION",
                    balanceNuevoUSD = newBalance,
                    destino = destinatario
                )
            )
        }

        OrionTransferResult(
            success = (estado != TransactionStatus.BLOQUEADA),
            transferencia = savedTransferencia,
            paymentResult = paymentResult,
            evaluacion = evaluacion,
            error = if (estado == TransactionStatus.BLOQUEADA) "Transferencia bloqueada por seguridad preventiva" else null
        )
    }

    suspend fun confirmar2FA(transferencia: TransferenciaEntity): TransferenciaEntity = withContext(Dispatchers.IO) {
        val updated = transferencia.copy(
            estado = TransactionStatus.COMPLETADA,
            notas = "${transferencia.notas}\n[2FA Aprobado]: Confirmación manual de seguridad de Lucy validada."
        )
        database.transferenciaDao().updateTransferencia(updated)
        updated
    }

    suspend fun simularPagoEnOxxo(transferencia: TransferenciaEntity): TransferenciaEntity = withContext(Dispatchers.IO) {
        val updated = transferencia.copy(
            estado = TransactionStatus.COMPLETADA,
            notas = "✅ Pago acreditado en caja OXXO (Cajero #4092). Saldo recargado y conciliado con Mercado Pago / NeuraForge."
        )
        database.transferenciaDao().updateTransferencia(updated)
        updated
    }
}
