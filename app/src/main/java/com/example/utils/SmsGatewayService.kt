package com.example.utils

import com.example.data.model.SmsGatewayConfig
import com.example.data.model.SmsGatewayProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SmsLogEntry(
    val id: String,
    val phone: String,
    val message: String,
    val provider: SmsGatewayProvider,
    val timestamp: Long,
    val status: String,
    val referenceId: String
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale("ar", "SY"))
            return sdf.format(Date(timestamp))
        }
}

object SmsGatewayService {
    private val _configState = MutableStateFlow(SmsGatewayConfig())
    val configState: StateFlow<SmsGatewayConfig> = _configState.asStateFlow()

    private val _smsLogs = MutableStateFlow<List<SmsLogEntry>>(
        listOf(
            SmsLogEntry(
                id = "sms_01",
                phone = "+963944123456",
                message = "رمز التحقق لتطبيق بونزوا هو: 482910. صالح لمدة 5 دقائق.",
                provider = SmsGatewayProvider.SYRIATEL,
                timestamp = System.currentTimeMillis() - 3600000,
                status = "DELIVERED",
                referenceId = "SYR-982341"
            ),
            SmsLogEntry(
                id = "sms_02",
                phone = "+963991876543",
                message = "رمز الدخول الخاص بك: 620194. لا تشارك الرمز مع أحد.",
                provider = SmsGatewayProvider.MTN,
                timestamp = System.currentTimeMillis() - 1800000,
                status = "DELIVERED",
                referenceId = "MTN-551029"
            )
        )
    )
    val smsLogs: StateFlow<List<SmsLogEntry>> = _smsLogs.asStateFlow()

    fun updateConfig(newConfig: SmsGatewayConfig) {
        _configState.value = newConfig
    }

    suspend fun dispatchSms(phone: String, message: String): Result<String> {
        val currentConfig = _configState.value
        val normalizedPhone = SyrianPhoneValidator.normalizeToInternational(phone)
        val refId = "SMS-${currentConfig.provider.key.uppercase()}-${(100000..999999).random()}"

        val log = SmsLogEntry(
            id = "sms_${System.currentTimeMillis()}",
            phone = normalizedPhone,
            message = message,
            provider = currentConfig.provider,
            timestamp = System.currentTimeMillis(),
            status = "DELIVERED",
            referenceId = refId
        )

        _smsLogs.value = listOf(log) + _smsLogs.value
        _configState.value = currentConfig.copy(
            totalSmsSentCount = currentConfig.totalSmsSentCount + 1,
            lastDeliveryStatus = "SUCCESS"
        )

        return Result.success(refId)
    }

    suspend fun sendTestSms(phone: String): Result<String> {
        val error = SyrianPhoneValidator.getValidationError(phone)
        if (error != null) {
            return Result.failure(IllegalArgumentException(error))
        }
        val message = "تجربة ربط بوابة الرسائل بنجاح من مطعم بونزوا سوريا 🍔. رمز الاختبار: ${(100000..999999).random()}"
        return dispatchSms(phone, message)
    }
}
