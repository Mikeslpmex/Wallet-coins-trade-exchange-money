package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PayShieldDatabase
import com.example.data.model.*
import com.example.engine.*
import com.example.engine.payment.PaymentExecutionResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiState(
    val transferencias: List<TransferenciaEntity> = emptyList(),
    val tesoreroRegistros: List<TesoreroRegistroEntity> = emptyList(),
    val umbrales: List<AntifraudUmbralEntity> = emptyList(),
    val ipBlacklist: List<IpBlacklistEntity> = emptyList(),
    val tesoreroBalanceUsd: Double = 9750.0,
    val liveRates: LiveMarketRates = LiveMarketRates(),
    val bloqueadasCount: Int = 0,
    val requiere2FACount: Int = 0,
    val selectedTransferencia: TransferenciaEntity? = null,
    val aiAnalisisResult: String? = null,
    val isAiLoading: Boolean = false,
    val terminalLogs: List<String> = listOf(
        "🤖 Lucy v2.0 Telegram Bot inicializada.",
        "🔒 NeuraForge Antifraud Engine v1.0.0 activo.",
        "⚡ OrionCash conectores: Mercado Pago, OXXO Pay, Google Cards, BBVA, Amazon, Bitso listos.",
        "💡 Escribe /help para ver comandos disponibles."
    ),
    val lastGeneratedVoucher: TransferenciaEntity? = null,
    val messageSnackbar: String? = null
)

class PayShieldViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PayShieldDatabase.getDatabase(application, viewModelScope)
    private val transferenciaDao = database.transferenciaDao()
    private val tesoreroDao = database.tesoreroDao()
    private val umbralDao = database.antifraudUmbralDao()
    private val ipDao = database.ipBlacklistDao()

    val antifraudEngine = NeuraforgeAntifraudEngine(transferenciaDao, ipDao)
    val orionCash = OrionCash(antifraudEngine, database)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Observe transferencias
        viewModelScope.launch {
            transferenciaDao.getAllTransferencias().collect { list ->
                _uiState.update { it.copy(transferencias = list) }
            }
        }

        // Observe tesorero
        viewModelScope.launch {
            tesoreroDao.getAllRegistros().collect { list ->
                val latest = list.firstOrNull()?.balanceNuevoUSD ?: 9750.0
                _uiState.update { it.copy(tesoreroRegistros = list, tesoreroBalanceUsd = latest) }
            }
        }

        // Observe umbrales
        viewModelScope.launch {
            umbralDao.getAllUmbrales().collect { list ->
                _uiState.update { it.copy(umbrales = list) }
            }
        }

        // Observe IP Blacklist
        viewModelScope.launch {
            ipDao.getAllBlacklist().collect { list ->
                _uiState.update { it.copy(ipBlacklist = list) }
            }
        }

        // Observe counts
        viewModelScope.launch {
            transferenciaDao.getBloqueadasCount().collect { count ->
                _uiState.update { it.copy(bloqueadasCount = count) }
            }
        }
        viewModelScope.launch {
            transferenciaDao.getRequiere2FACount().collect { count ->
                _uiState.update { it.copy(requiere2FACount = count) }
            }
        }

        // Fetch live rates
        refreshMarketRates()
    }

    fun refreshMarketRates() {
        viewModelScope.launch {
            val rates = ExchangeRatesService.fetchCurrentRates()
            _uiState.update { it.copy(liveRates = rates) }
        }
    }

    fun executeTransferencia(
        usuarioId: String,
        destinatario: String,
        monto: Double,
        moneda: String,
        proveedor: GatewayProvider,
        tipo: TransactionType,
        ipOrigen: String? = "192.168.1.45",
        redCrypto: String? = null
    ) {
        viewModelScope.launch {
            val result = orionCash.transferir(
                usuarioId = usuarioId,
                destinatario = destinatario,
                monto = monto,
                moneda = moneda,
                proveedor = proveedor,
                tipo = tipo,
                ipOrigen = ipOrigen,
                redCrypto = redCrypto
            )

            val logMsg = if (result.success) {
                "✅ [OrionCash] Transferencia ${result.transferencia.referenciaId} procesada exitosamente (${monto} $moneda -> $destinatario via ${proveedor.name})."
            } else {
                "🚨 [NeuraForge ALERTA] Transferencia BLOQUEADA por riesgo alto: ${result.evaluacion.alertas.joinToString(", ")}"
            }

            _uiState.update {
                it.copy(
                    lastGeneratedVoucher = result.transferencia,
                    terminalLogs = it.terminalLogs + logMsg,
                    messageSnackbar = if (result.success) "Operación ${result.transferencia.referenciaId} completada" else "Operación bloqueada por módulo Antifraude"
                )
            }
        }
    }

    fun generateOxxoPayVoucher(usuarioId: String, monto: Double, concepto: String) {
        executeTransferencia(
            usuarioId = usuarioId,
            destinatario = "Tienda OXXO ($concepto)",
            monto = monto,
            moneda = "MXN",
            proveedor = GatewayProvider.OXXO_PAY,
            tipo = TransactionType.OXXO_CASH_VOUCHER
        )
    }

    fun generateGooglePlayCard(usuarioId: String, monto: Double, recipientEmail: String) {
        executeTransferencia(
            usuarioId = usuarioId,
            destinatario = "Google Play ($recipientEmail)",
            monto = monto,
            moneda = "MXN",
            proveedor = GatewayProvider.GOOGLE_PLAY_CARD,
            tipo = TransactionType.GOOGLE_CARD_PURCHASE
        )
    }

    fun simularPagoOxxo(transferencia: TransferenciaEntity) {
        viewModelScope.launch {
            val updated = orionCash.simularPagoEnOxxo(transferencia)
            _uiState.update {
                it.copy(
                    selectedTransferencia = updated,
                    terminalLogs = it.terminalLogs + "🏪 [OXXO Pay] Escaneo en caja validado para ${transferencia.referenciaId}. Saldo acreditado.",
                    messageSnackbar = "Pago OXXO acreditado con éxito"
                )
            }
        }
    }

    fun aprobar2FA(transferencia: TransferenciaEntity) {
        viewModelScope.launch {
            val updated = orionCash.confirmar2FA(transferencia)
            _uiState.update {
                it.copy(
                    selectedTransferencia = updated,
                    terminalLogs = it.terminalLogs + "🔐 [Lucy 2FA] Autorización manual aprobada para ${transferencia.referenciaId}.",
                    messageSnackbar = "Transferencia 2FA aprobada"
                )
            }
        }
    }

    fun selectTransferencia(transferencia: TransferenciaEntity?) {
        _uiState.update { it.copy(selectedTransferencia = transferencia, aiAnalisisResult = null) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(messageSnackbar = null) }
    }

    fun analyzeWithAI(transferencia: TransferenciaEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true) }
            val evaluacion = AntifraudEvaluation(
                nivel = transferencia.nivelRiesgo,
                puntuacion = transferencia.puntuacionRiesgo,
                alertas = if (transferencia.alertasRiesgo.isNotEmpty()) transferencia.alertasRiesgo.split("; ") else emptyList(),
                aprobada = (transferencia.estado == TransactionStatus.APROBADA || transferencia.estado == TransactionStatus.COMPLETADA),
                requiere2FA = (transferencia.estado == TransactionStatus.REQUIERE_2FA)
            )

            val text = antifraudEngine.consultarAnalisisGeminiAI(
                usuarioId = transferencia.usuarioId,
                destinatario = transferencia.destinatario,
                monto = transferencia.monto,
                moneda = transferencia.moneda,
                proveedor = transferencia.proveedor.name,
                evaluacion = evaluacion
            )

            _uiState.update {
                it.copy(
                    isAiLoading = false,
                    aiAnalisisResult = text
                )
            }
        }
    }

    fun toggleUmbral(umbral: AntifraudUmbralEntity) {
        viewModelScope.launch {
            umbralDao.updateUmbral(umbral.copy(habilitado = !umbral.habilitado))
        }
    }

    fun addBlacklistIp(ip: String, motivo: String) {
        viewModelScope.launch {
            if (ip.isNotBlank()) {
                ipDao.insertIp(IpBlacklistEntity(ip.trim(), motivo.ifEmpty { "Agregada manualmente" }))
                _uiState.update {
                    it.copy(terminalLogs = it.terminalLogs + "🚫 [Antifraude] IP $ip añadida a la lista negra.")
                }
            }
        }
    }

    fun removeBlacklistIp(ip: IpBlacklistEntity) {
        viewModelScope.launch {
            ipDao.removeIp(ip)
            _uiState.update {
                it.copy(terminalLogs = it.terminalLogs + "✅ [Antifraude] IP ${ip.ipAddress} removida de la lista negra.")
            }
        }
    }

    fun executeTelegramBotCommand(commandText: String) {
        val cmd = commandText.trim()
        if (cmd.isEmpty()) return

        val newLogs = _uiState.value.terminalLogs.toMutableList()
        newLogs.add("👤 Usuario: $cmd")

        val parts = cmd.split(" ").filter { it.isNotBlank() }
        val action = parts.getOrNull(0)?.lowercase()

        when (action) {
            "/transferir" -> {
                // /transferir <destinatario> <monto> <moneda>
                if (parts.size < 4) {
                    newLogs.add("❌ Uso: /transferir <destinatario> <monto> <moneda> [proveedor]")
                } else {
                    val dest = parts[1]
                    val monto = parts[2].toDoubleOrNull() ?: 100.0
                    val moneda = parts[3].uppercase()
                    val proveedor = if (moneda in listOf("ETH", "BTC", "USDT")) GatewayProvider.BITSO_CRYPTO else GatewayProvider.MERCADO_PAGO
                    executeTransferencia(
                        usuarioId = "telegram_user_99",
                        destinatario = dest,
                        monto = monto,
                        moneda = moneda,
                        proveedor = proveedor,
                        tipo = if (moneda in listOf("ETH", "BTC", "USDT")) TransactionType.CRYPTO_WITHDRAWAL else TransactionType.PAYMENT_GATEWAY
                    )
                }
            }
            "/oxxopay" -> {
                // /oxxopay <monto>
                val monto = parts.getOrNull(1)?.toDoubleOrNull() ?: 250.0
                generateOxxoPayVoucher("telegram_user_99", monto, "Recarga Bot")
                newLogs.add("🎟️ Ficha OXXO Pay solicitada por $$monto MXN.")
            }
            "/googlecard" -> {
                // /googlecard <monto> <email>
                val monto = parts.getOrNull(1)?.toDoubleOrNull() ?: 200.0
                val email = parts.getOrNull(2) ?: "usuario.bot@gmail.com"
                generateGooglePlayCard("telegram_user_99", monto, email)
                newLogs.add("🎁 Tarjeta Google Play emitida para $email por $$monto MXN.")
            }
            "/reporte_transferencias" -> {
                val total = _uiState.value.transferencias.size
                val bloqueadas = _uiState.value.bloqueadasCount
                val saldo = _uiState.value.tesoreroBalanceUsd
                newLogs.add("📊 REPORTE NEURAFORGE & ORIONCASH:\n• Total Transacciones: $total\n• Bloqueadas Antifraude: $bloqueadas\n• Saldo Bóveda Tesorería: $$saldo USD")
            }
            "/antifraude_status" -> {
                newLogs.add("🛡️ ESTADO MOTOR ANTIFRAUDE:\n• Umbrales Activos: ${_uiState.value.umbrales.count { it.habilitado }}\n• IPs en Blacklist: ${_uiState.value.ipBlacklist.size}\n• Motor Heurístico + Gemini AI: OPERACIONAL")
            }
            "/help" -> {
                newLogs.add("📋 COMANDOS TELEGRAM LUCY BOT:\n• /transferir <destinatario> <monto> <moneda>\n• /oxxopay <monto>\n• /googlecard <monto> <email>\n• /reporte_transferencias\n• /antifraude_status")
            }
            else -> {
                newLogs.add("❓ Comando no reconocido. Escribe /help para ver la lista.")
            }
        }

        _uiState.update { it.copy(terminalLogs = newLogs) }
    }
}
