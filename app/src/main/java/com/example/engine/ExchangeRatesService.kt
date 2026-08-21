package com.example.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class LiveMarketRates(
    val usdToMxn: Double = 19.85,
    val ethToUsd: Double = 3120.0,
    val btcToUsd: Double = 64500.0,
    val usdtToUsd: Double = 1.00,
    val isLive: Boolean = false,
    val source: String = "Tasas de Referencia Bancaria / Cripto"
)

object ExchangeRatesService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun fetchCurrentRates(): LiveMarketRates = withContext(Dispatchers.IO) {
        var usdMxn = 19.85
        var ethUsd = 3150.0
        var btcUsd = 64800.0
        var liveSuccess = false

        try {
            val requestFiat = Request.Builder()
                .url("https://api.exchangerate-api.com/v4/latest/USD")
                .build()

            val responseFiat = client.newCall(requestFiat).execute()
            if (responseFiat.isSuccessful) {
                responseFiat.body?.string()?.let { jsonStr ->
                    val root = JSONObject(jsonStr)
                    val rates = root.getJSONObject("rates")
                    if (rates.has("MXN")) {
                        usdMxn = rates.getDouble("MXN")
                        liveSuccess = true
                    }
                }
            }
        } catch (e: Exception) {
            // Safe fallback if offline
        }

        try {
            val requestCrypto = Request.Builder()
                .url("https://api.coingecko.com/api/v3/simple/price?ids=ethereum,bitcoin,tether&vs_currencies=usd")
                .build()

            val responseCrypto = client.newCall(requestCrypto).execute()
            if (responseCrypto.isSuccessful) {
                responseCrypto.body?.string()?.let { jsonStr ->
                    val root = JSONObject(jsonStr)
                    if (root.has("ethereum")) {
                        ethUsd = root.getJSONObject("ethereum").getDouble("usd")
                    }
                    if (root.has("bitcoin")) {
                        btcUsd = root.getJSONObject("bitcoin").getDouble("usd")
                    }
                    liveSuccess = true
                }
            }
        } catch (e: Exception) {
            // Keep current crypto estimates
        }

        LiveMarketRates(
            usdToMxn = usdMxn,
            ethToUsd = ethUsd,
            btcToUsd = btcUsd,
            usdtToUsd = 1.00,
            isLive = liveSuccess,
            source = if (liveSuccess) "Live Feed API (ExchangeRate / CoinGecko)" else "Referencia Segura Offline"
        )
    }
}
