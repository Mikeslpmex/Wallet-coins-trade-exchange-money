package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GatewayProvider {
    MERCADO_PAGO,
    OXXO_PAY,
    BBVA_SPEI,
    AMAZON_PAY,
    GOOGLE_PLAY_CARD,
    BITSO_CRYPTO,
    BINANCE_PAY
}

enum class TransactionType {
    PAYMENT_GATEWAY,
    OXXO_CASH_VOUCHER,
    GOOGLE_CARD_PURCHASE,
    CRYPTO_WITHDRAWAL,
    CLOSING_CYCLE_MP,
    SPEI_DEPOSIT
}

enum class RiskLevel {
    BAJO,
    MEDIO,
    ALTO
}

enum class TransactionStatus {
    PENDIENTE,
    REQUIERE_2FA,
    EN_REVISION,
    APROBADA,
    COMPLETADA,
    BLOQUEADA,
    EXPIRADA
}

@Entity(tableName = "transferencias")
data class TransferenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val referenciaId: String,
    val usuarioId: String,
    val destinatario: String,
    val monto: Double,
    val moneda: String,
    val proveedor: GatewayProvider,
    val tipo: TransactionType,
    val estado: TransactionStatus,
    val nivelRiesgo: RiskLevel,
    val puntuacionRiesgo: Int,
    val alertasRiesgo: String, // JSON or comma separated alerts
    val ipOrigen: String = "192.168.1.45",
    val barcodeNumber: String = "", // For OXXO Pay / Google Play barcode
    val googleCardCode: String = "", // e.g. GPLY-8823-9481-2049
    val txHash: String = "", // For crypto on-chain verification
    val oxxoComision: Double = 15.0,
    val comisionPlataforma: Double = 0.0,
    val totalACobrar: Double = 0.0,
    val mpPreferenceId: String = "",
    val clabeSpei: String = "",
    val notas: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tesorero_registros")
data class TesoreroRegistroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val montoUSD: Double,
    val concepto: String,
    val categoria: String,
    val balanceNuevoUSD: Double,
    val destino: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "antifraud_umbrales")
data class AntifraudUmbralEntity(
    @PrimaryKey
    val clave: String,
    val nombre: String,
    val descripcion: String,
    val valorNumerico: Double = 0.0,
    val valorTexto: String = "",
    val habilitado: Boolean = true
)

@Entity(tableName = "ip_blacklist")
data class IpBlacklistEntity(
    @PrimaryKey
    val ipAddress: String,
    val motivo: String,
    val createdAt: Long = System.currentTimeMillis()
)
