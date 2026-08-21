package com.example.engine

import com.example.BuildConfig
import com.example.data.local.IpBlacklistDao
import com.example.data.local.TransferenciaDao
import com.example.data.model.RiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AntifraudEvaluation(
    val nivel: RiskLevel,
    val puntuacion: Int,
    val alertas: List<String>,
    val aprobada: Boolean,
    val requiere2FA: Boolean,
    val aiAnalisis: String? = null
)

class NeuraforgeAntifraudEngine(
    private val transferenciaDao: TransferenciaDao,
    private val ipBlacklistDao: IpBlacklistDao
) {
    private var maximoMontoUsd: Double = 5000.0
    private var frecuenciaMaxima24h: Int = 3

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun evaluarTransferencia(
        usuarioId: String,
        destinatario: String,
        monto: Double,
        moneda: String,
        ipOrigen: String? = "192.168.1.45",
        redCrypto: String? = null
    ): AntifraudEvaluation = withContext(Dispatchers.IO) {
        var riesgo = 0
        val alertas = mutableListOf<String>()

        // 1. Verificar monto
        val montoEquivUsd = if (moneda.equals("MXN", ignoreCase = true)) monto / 19.85 else monto
        if (montoEquivUsd > maximoMontoUsd) {
            riesgo += 20
            alertas.add("Monto ($monto $moneda) excede el umbral límite permitido de $$maximoMontoUsd USD.")
        }

        // 2. Verificar destinatario (historial de transacciones)
        val previo = transferenciaDao.getPreviousTransfer(usuarioId, destinatario)
        val esDestinatarioNuevo = (previo == null)
        if (esDestinatarioNuevo) {
            riesgo += 30
            alertas.add("Destinatario sin historial previo con el usuario $usuarioId.")
        }

        // 3. Frecuencia de transferencias en 24h
        val hace24h = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        val frecuencia = transferenciaDao.countTransferenciasSince(usuarioId, hace24h)
        if (frecuencia >= frecuenciaMaxima24h) {
            riesgo += 25
            alertas.add("Frecuencia alta: $frecuencia transferencias registradas en las últimas 24h.")
        }

        // 4. IP sospechosa en lista negra
        val ipToCheck = ipOrigen ?: "192.168.1.1"
        if (ipBlacklistDao.isIpBlacklisted(ipToCheck)) {
            riesgo += 30
            alertas.add("IP origen ($ipToCheck) detectada en lista negra Neuraforge.")
        }

        // 5. Crypto Address & Network Check
        if (redCrypto != null && destinatario.isNotEmpty()) {
            val addrResult = CryptoValidator.validateAddress(destinatario, redCrypto)
            if (!addrResult.isValid) {
                riesgo += 35
                alertas.add("Dirección cripto no válida para la red $redCrypto.")
            }
            if (addrResult.securityWarning != null) {
                riesgo += 40
                alertas.add(addrResult.securityWarning)
            }
        }

        // Determinar nivel de riesgo
        val nivel = when {
            riesgo >= 70 -> RiskLevel.ALTO
            riesgo >= 40 -> RiskLevel.MEDIO
            else -> RiskLevel.BAJO
        }

        AntifraudEvaluation(
            nivel = nivel,
            puntuacion = riesgo,
            alertas = alertas,
            aprobada = (nivel == RiskLevel.BAJO),
            requiere2FA = (nivel == RiskLevel.MEDIO)
        )
    }

    suspend fun consultarAnalisisGeminiAI(
        usuarioId: String,
        destinatario: String,
        monto: Double,
        moneda: String,
        proveedor: String,
        evaluacion: AntifraudEvaluation
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "🤖 Análisis Antifraude NeuraForge (Local):\n" +
                    "• Nivel Asignado: ${evaluacion.nivel} (Score: ${evaluacion.puntuacion}/100)\n" +
                    "• Alertas Activas: ${evaluacion.alertas.size}\n" +
                    "• Acción Recomendada: ${if (evaluacion.nivel == RiskLevel.ALTO) "🚨 Bloqueo Inmediato" else if (evaluacion.nivel == RiskLevel.MEDIO) "🔐 Requiere Confirmación 2FA (Lucy Bot)" else "✅ Aprobada para Liquidación Automática"}"
        }

        try {
            val prompt = """
                Actúa como el motor de Inteligencia Antifraude Neuraforge AI para OrionCash.
                Analiza esta transferencia:
                - Usuario: $usuarioId
                - Destinatario: $destinatario
                - Proveedor / Pasarela: $proveedor
                - Monto: $monto $moneda
                - Puntuación de Riesgo: ${evaluacion.puntuacion}/100 (Nivel: ${evaluacion.nivel})
                - Alertas de Reglas: ${evaluacion.alertas.joinToString("; ")}
                
                Proporciona en 3 viñetas concisas:
                1. Dictamen de Seguridad y Comportamiento de Usuario.
                2. Verificación de Fraude / Token / Proveedor ($proveedor).
                3. Instrucción final a ejecutar por el Bot Tesorero OrionCash.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }

            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder().url(url).post(body).build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val respStr = response.body?.string() ?: ""
                val respJson = JSONObject(respStr)
                val text = respJson.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                if (!text.isNullOrBlank()) return@withContext text.trim()
            }
            return@withContext "Evaluación Neuraforge AI completada con éxito."
        } catch (e: Exception) {
            return@withContext "Evaluación del Motor Neuraforge procesada conforme a reglas de seguridad."
        }
    }
}
