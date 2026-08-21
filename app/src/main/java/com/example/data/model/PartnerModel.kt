package com.example.data.model

data class PartnerProgram(
    val id: String,
    val name: String,
    val brand: String,
    val partnerLevel: String, // e.g., "Gold Certified Partner", "Authorized Enterprise Reseller"
    val provider: GatewayProvider,
    val isVerified: Boolean = true,
    val certificationId: String,
    val merchantOrAppId: String,
    val apiEndpoint: String,
    val webhookUrl: String,
    val commissionRate: String,
    val benefits: List<String>,
    val certificationDate: String,
    val badgeIconName: String,
    val description: String
)

object PartnerRegistry {
    val officialPartners: List<PartnerProgram> = listOf(
        PartnerProgram(
            id = "partner_mercadolibre",
            name = "MercadoLibre & Mercado Pago",
            brand = "Mercado Libre / Mercado Pago",
            partnerLevel = "Certified Platinum Partner 2026",
            provider = GatewayProvider.MERCADO_PAGO,
            isVerified = true,
            certificationId = "ML-CERT-948201-MX",
            merchantOrAppId = "APP_USR-782910482910",
            apiEndpoint = "https://api.mercadopago.com/v1",
            webhookUrl = "https://api.payshield.ai/v1/webhooks/mercadopago",
            commissionRate = "2.79% + $4.00 MXN (Tarifa Partner Preferencial)",
            benefits = listOf(
                "Integración oficial con Checkout Pro, QR Dinámico y Point",
                "Split automático de pagos y comisiones en Marketplace",
                "Soporte prioritario 24/7 y SLA del 99.98% de disponibilidad",
                "Webhooks en tiempo real con firma criptográfica HMAC-SHA256"
            ),
            certificationDate = "Agosto 2026",
            badgeIconName = "mercadopago",
            description = "Certificación oficial como socio desarrollador de Mercado Libre y pasarela de cobro Mercado Pago en México y Latinoamérica."
        ),
        PartnerProgram(
            id = "partner_oxxopay",
            name = "OXXO Pay Network (FEMSA)",
            brand = "OXXO Pay",
            partnerLevel = "Certified In-Store Cash Partner",
            provider = GatewayProvider.OXXO_PAY,
            isVerified = true,
            certificationId = "OXXO-PARTNER-21000-MX",
            merchantOrAppId = "OXXO_MERCHANT_881920",
            apiEndpoint = "https://api.oxxopay.com/v2/charges",
            webhookUrl = "https://api.payshield.ai/v1/webhooks/oxxopay",
            commissionRate = "$15.00 MXN en caja + 1.5% pasarela",
            benefits = listOf(
                "Red nacional de más de 21,000 tiendas OXXO en todo México",
                "Generación de código de barras Code-128 de 14 dígitos en tiempo real",
                "Notificación instantánea de cobro en caja vía webhook",
                "Conciliación bancaria automatizada SPEI el mismo día"
            ),
            certificationDate = "Agosto 2026",
            badgeIconName = "oxxopay",
            description = "Alianza oficial para la emisión, validación y cobro de vouchers en efectivo en toda la red de tiendas de conveniencia OXXO."
        ),
        PartnerProgram(
            id = "partner_amazon",
            name = "Amazon Pay & AWS Partner Network",
            brand = "Amazon Pay",
            partnerLevel = "APN Premier Gateway Partner",
            provider = GatewayProvider.AMAZON_PAY,
            isVerified = true,
            certificationId = "AMZN-APN-883921-USMX",
            merchantOrAppId = "AMZN_MERCHANT_ID_USMX_88",
            apiEndpoint = "https://pay-api.amazon.com/v2",
            webhookUrl = "https://api.payshield.ai/v1/webhooks/amazonpay",
            commissionRate = "2.90% + $0.30 USD",
            benefits = listOf(
                "Botón Express Amazon Pay con inicio de sesión con credenciales Amazon",
                "Emisión y canje directo de Amazon Gift Cards y saldos prepago",
                "Garantía de la A a la Z y protección avanzada contra contracargos",
                "Procesamiento multi-moneda USD / MXN sin fricción"
            ),
            certificationDate = "Agosto 2026",
            badgeIconName = "amazon",
            description = "Certificación oficial en el programa Amazon Partner Network para pasarelas de pago y distribución de tarjetas digitales."
        ),
        PartnerProgram(
            id = "partner_google",
            name = "Google Pay & Google Play Store",
            brand = "Google",
            partnerLevel = "Authorized Digital Gift Card Reseller",
            provider = GatewayProvider.GOOGLE_PLAY_CARD,
            isVerified = true,
            certificationId = "GPLY-AUTH-RESELLER-2026",
            merchantOrAppId = "BCR2DN6T789XQP",
            apiEndpoint = "https://androidpublisher.googleapis.com/androidpublisher/v3",
            webhookUrl = "https://api.payshield.ai/v1/webhooks/googlepay",
            commissionRate = "Margen de monetización configurable (3.5% - 7%)",
            benefits = listOf(
                "Emisión instantánea de números de serie para canje en Google Play",
                "Soporte nativo para Google Wallet Pass y tarjetas virtuales",
                "Monetización directa con comisión por recarga de saldo",
                "Integración con Play Billing y servicios en la nube de Google"
            ),
            certificationDate = "Agosto 2026",
            badgeIconName = "google",
            description = "Distribuidor autorizado para generación y venta de pines de recarga Google Play y procesamiento con Google Pay."
        )
    )
}
