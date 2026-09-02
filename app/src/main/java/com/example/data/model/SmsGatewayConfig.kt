package com.example.data.model

enum class SmsGatewayProvider(val key: String, val labelAr: String, val defaultEndpoint: String) {
    SIMULATOR_SECURE("simulator", "محاكي الأمان الذاتي (اختبارات فورية)", ""),
    SYRIATEL("syriatel", "سيريتل بوابة الأعمال (Syriatel B2B SMS)", "https://b2b.syriatel.sy/api/sms/send"),
    MTN("mtn", "إم تي إن للأعمال (MTN Syria Enterprise)", "https://enterprise.mtn.sy/api/v1/send"),
    TWILIO("twilio", "بوابة دولية (Twilio Global Gateway)", "https://api.twilio.com/2010-04-01/Accounts/"),
    CUSTOM_WEBHOOK("webhook", "بوابة مخصصة (Custom REST Webhook)", "https://api.bunzo-fastfood.sy/sms/dispatch");

    companion object {
        fun fromKey(key: String?): SmsGatewayProvider {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: SIMULATOR_SECURE
        }
    }
}

data class SmsGatewayConfig(
    val provider: SmsGatewayProvider = SmsGatewayProvider.SIMULATOR_SECURE,
    val senderId: String = "BUNZO-SYRIA",
    val apiKey: String = "",
    val apiSecret: String = "",
    val apiUrl: String = "",
    val isEnabled: Boolean = true,
    val totalSmsSentCount: Int = 142,
    val lastDeliveryStatus: String = "SUCCESS"
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "provider" to provider.key,
            "senderId" to senderId,
            "apiKey" to apiKey,
            "apiSecret" to apiSecret,
            "apiUrl" to apiUrl,
            "isEnabled" to isEnabled,
            "totalSmsSentCount" to totalSmsSentCount,
            "lastDeliveryStatus" to lastDeliveryStatus
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>?): SmsGatewayConfig {
            if (map == null) return SmsGatewayConfig()
            return SmsGatewayConfig(
                provider = SmsGatewayProvider.fromKey(map["provider"] as? String),
                senderId = map["senderId"] as? String ?: "BUNZO-SYRIA",
                apiKey = map["apiKey"] as? String ?: "",
                apiSecret = map["apiSecret"] as? String ?: "",
                apiUrl = map["apiUrl"] as? String ?: "",
                isEnabled = (map["isEnabled"] as? Boolean) ?: true,
                totalSmsSentCount = (map["totalSmsSentCount"] as? Number)?.toInt() ?: 142,
                lastDeliveryStatus = map["lastDeliveryStatus"] as? String ?: "SUCCESS"
            )
        }
    }
}
