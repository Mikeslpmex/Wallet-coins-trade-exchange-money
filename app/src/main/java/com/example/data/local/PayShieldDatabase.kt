package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransferenciaEntity::class,
        TesoreroRegistroEntity::class,
        AntifraudUmbralEntity::class,
        IpBlacklistEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PayShieldDatabase : RoomDatabase() {
    abstract fun transferenciaDao(): TransferenciaDao
    abstract fun tesoreroDao(): TesoreroDao
    abstract fun antifraudUmbralDao(): AntifraudUmbralDao
    abstract fun ipBlacklistDao(): IpBlacklistDao

    companion object {
        @Volatile
        private var INSTANCE: PayShieldDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PayShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PayShieldDatabase::class.java,
                    "neuraforge_orion_gateway.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: PayShieldDatabase) {
            val umbralDao = db.antifraudUmbralDao()
            val ipDao = db.ipBlacklistDao()
            val tesoreroDao = db.tesoreroDao()
            val transDao = db.transferenciaDao()

            // Neuraforge Antifraud Default Umbrales
            val defaultUmbrales = listOf(
                AntifraudUmbralEntity(
                    clave = "maximo_monto",
                    nombre = "Monto Máximo Permitido (USD / MXN)",
                    descripcion = "Umbral límite por operación antes de elevar el riesgo (+20 pts).",
                    valorNumerico = 5000.0,
                    habilitado = true
                ),
                AntifraudUmbralEntity(
                    clave = "destinatario_nuevo",
                    nombre = "Alerta Destinatario Nuevo",
                    descripcion = "Activa alerta si el usuario nunca ha transferido antes a este destino (+30 pts).",
                    valorNumerico = 1.0,
                    habilitado = true
                ),
                AntifraudUmbralEntity(
                    clave = "frecuencia_maxima",
                    nombre = "Frecuencia Máxima (24h)",
                    descripcion = "Límite de transferencias en un período de 24 horas (+25 pts si excede).",
                    valorNumerico = 3.0,
                    habilitado = true
                ),
                AntifraudUmbralEntity(
                    clave = "oxxo_comision_fija",
                    nombre = "Comisión Fija OXXO Pay (MXN)",
                    descripcion = "Comisión cobrada en caja de tienda OXXO al escanear código de barras.",
                    valorNumerico = 15.0,
                    habilitado = true
                ),
                AntifraudUmbralEntity(
                    clave = "monetizacion_google_card_pct",
                    nombre = "Margen Monetización Tarjetas Digitales (%)",
                    descripcion = "Comisión de pasarela aplicada a tarjetas Google Play / Amazon / Vouchers.",
                    valorNumerico = 3.5,
                    habilitado = true
                )
            )
            umbralDao.insertUmbrales(defaultUmbrales)

            // IP Blacklist (from Python script reference + known proxy nodes)
            val initialBlacklist = listOf(
                IpBlacklistEntity("192.168.1.1", "IP de prueba en lista negra Neuraforge"),
                IpBlacklistEntity("185.220.101.5", "Nodo de salida Tor / Proxy anónimo detectado"),
                IpBlacklistEntity("45.154.255.89", "Data Center con alta tasa de fraude")
            )
            initialBlacklist.forEach { ipDao.insertIp(it) }

            // Tesorero Balance
            tesoreroDao.insertRegistro(
                TesoreroRegistroEntity(
                    montoUSD = 10000.0,
                    concepto = "Fondeo Inicial Bóveda OrionCash",
                    categoria = "FONDEO_INICIAL",
                    balanceNuevoUSD = 10000.0,
                    destino = "Bóveda Central Neuraforge"
                )
            )
            tesoreroDao.insertRegistro(
                TesoreroRegistroEntity(
                    montoUSD = -250.0,
                    concepto = "Retiro ETH Bitso (0x20a218EABE)",
                    categoria = "RETIRO_CRYPTO",
                    balanceNuevoUSD = 9750.0,
                    destino = "0x20a218EABE02Fe04ff0Fe499BA0e7D4d8128795B"
                )
            )

            // Sample Gateway & Card transactions
            val initialTrans = listOf(
                TransferenciaEntity(
                    referenciaId = "OX-93028491039218",
                    usuarioId = "user_mendozap",
                    destinatario = "Tienda OXXO / Conciliación",
                    monto = 500.0,
                    moneda = "MXN",
                    proveedor = GatewayProvider.OXXO_PAY,
                    tipo = TransactionType.OXXO_CASH_VOUCHER,
                    estado = TransactionStatus.PENDIENTE,
                    nivelRiesgo = RiskLevel.BAJO,
                    puntuacionRiesgo = 10,
                    alertasRiesgo = "Ficha de pago OXXO Pay generada con código de barras de 14 dígitos",
                    barcodeNumber = "93028491039218",
                    oxxoComision = 15.0,
                    comisionPlataforma = 17.5,
                    totalACobrar = 532.5,
                    notas = "Paga en caja OXXO escaneando el código de barras."
                ),
                TransferenciaEntity(
                    referenciaId = "GPLY-882394812049",
                    usuarioId = "user_alex99",
                    destinatario = "Google Play Balance (Email: alex.gamer@gmail.com)",
                    monto = 200.0,
                    moneda = "MXN",
                    proveedor = GatewayProvider.GOOGLE_PLAY_CARD,
                    tipo = TransactionType.GOOGLE_CARD_PURCHASE,
                    estado = TransactionStatus.COMPLETADA,
                    nivelRiesgo = RiskLevel.BAJO,
                    puntuacionRiesgo = 5,
                    alertasRiesgo = "Código digital activado y verificado",
                    googleCardCode = "GPLY-8823-9481-2049",
                    barcodeNumber = "882394812049",
                    comisionPlataforma = 7.0,
                    totalACobrar = 207.0,
                    notas = "Tarjeta digital Google Play generada al instante para recarga."
                ),
                TransferenciaEntity(
                    referenciaId = "MP-PREF-84920491",
                    usuarioId = "user_carlos_dev",
                    destinatario = "Checkout Mercado Pago",
                    monto = 1200.0,
                    moneda = "MXN",
                    proveedor = GatewayProvider.MERCADO_PAGO,
                    tipo = TransactionType.PAYMENT_GATEWAY,
                    estado = TransactionStatus.APROBADA,
                    nivelRiesgo = RiskLevel.BAJO,
                    puntuacionRiesgo = 12,
                    alertasRiesgo = "Pago aprobado por Mercado Pago con tarjeta de crédito",
                    mpPreferenceId = "pref_84920491823",
                    notas = "Cobro procesado vía Checkout Pro Mercado Pago."
                ),
                TransferenciaEntity(
                    referenciaId = "BBVA-SPEI-77192",
                    usuarioId = "user_mariana",
                    destinatario = "CLABE: 012180015682910483 (BBVA Bancomer)",
                    monto = 3500.0,
                    moneda = "MXN",
                    proveedor = GatewayProvider.BBVA_SPEI,
                    tipo = TransactionType.SPEI_DEPOSIT,
                    estado = TransactionStatus.COMPLETADA,
                    nivelRiesgo = RiskLevel.BAJO,
                    puntuacionRiesgo = 15,
                    alertasRiesgo = "Transferencia SPEI liquidada en tiempo real",
                    clabeSpei = "012180015682910483",
                    notas = "Depósito interbancario SPEI BBVA liquidado con folio CEP."
                ),
                TransferenciaEntity(
                    referenciaId = "ALERT-BLOCKED-901",
                    usuarioId = "user_unknown_bot",
                    destinatario = "0x8576acc5c05d6ce88f4e49bf65bdf0c62f91353c",
                    monto = 6800.0,
                    moneda = "USDT",
                    proveedor = GatewayProvider.BITSO_CRYPTO,
                    tipo = TransactionType.CRYPTO_WITHDRAWAL,
                    estado = TransactionStatus.BLOQUEADA,
                    nivelRiesgo = RiskLevel.ALTO,
                    puntuacionRiesgo = 85,
                    alertasRiesgo = "Monto excede el límite permitido; IP origen en lista negra (192.168.1.1); Destinatario en lista negra de mixers",
                    ipOrigen = "192.168.1.1",
                    notas = "🚨 Bloqueada automáticamente por NeuraForge Antifraud Engine."
                )
            )
            initialTrans.forEach { transDao.insertTransferencia(it) }
        }
    }
}
