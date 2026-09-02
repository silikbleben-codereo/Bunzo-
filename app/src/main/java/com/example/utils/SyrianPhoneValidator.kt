package com.example.utils

/**
 * Enterprise Utility for validating and normalizing Syrian phone numbers (+963).
 * Syrian mobile numbers start with 09 (e.g. 093x, 094x, 095x, 096x, 098x, 099x)
 * International E.164 format: +9639xxxxxxxx (9 digits after +963)
 */
object SyrianPhoneValidator {
    const val SYRIA_COUNTRY_CODE = "+963"
    const val SYRIA_FLAG = "🇸🇾"
    const val COUNTRY_NAME_AR = "سوريا"
    const val COUNTRY_NAME_EN = "Syria"

    // Valid Syrian mobile operator prefixes: 093, 094, 095, 096, 098, 099
    private val SYRIAN_MOBILE_PREFIXES = listOf("093", "094", "095", "096", "098", "099", "93", "94", "95", "96", "98", "99")

    /**
     * Normalizes any input phone number into international E.164 format for Syria (+9639xxxxxxxx).
     * Examples:
     * "0933123456" -> "+963933123456"
     * "933123456"  -> "+963933123456"
     * "+963933123456" -> "+963933123456"
     * "00963933123456" -> "+963933123456"
     */
    fun normalizeToInternational(rawPhone: String): String {
        val clean = rawPhone.replace("\\s+".toRegex(), "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
        if (clean.isEmpty()) return ""
        return when {
            clean.startsWith("+963") -> clean
            clean.startsWith("00963") -> "+963" + clean.removePrefix("00963")
            clean.startsWith("963") -> "+$clean"
            clean.startsWith("09") -> "+963" + clean.removePrefix("0")
            clean.startsWith("9") && clean.length == 9 -> "+963$clean"
            else -> {
                if (clean.startsWith("0")) "+963" + clean.removePrefix("0")
                else "+963$clean"
            }
        }
    }

    /**
     * Converts normalized +9639xxxxxxxx to local readable format 09xxxxxxxx
     */
    fun toLocalDisplayFormat(phone: String): String {
        val normalized = normalizeToInternational(phone)
        return if (normalized.startsWith("+963")) {
            "0" + normalized.removePrefix("+963")
        } else {
            phone
        }
    }

    /**
     * Validates if the given string is a valid Syrian mobile number.
     * Must have exactly 9 digits after +963 starting with 9 (total 10 digits in local format: 09xxxxxxxx).
     */
    fun isValidSyrianNumber(phone: String): Boolean {
        val normalized = normalizeToInternational(phone)
        // Must start with +9639 and have 13 chars total: +963 9xx xxx xxx
        val regex = "^\\+9639[3-9][0-9]{7}$".toRegex()
        return regex.matches(normalized)
    }

    /**
     * Returns formatted error message in Arabic if invalid, or null if valid.
     */
    fun getValidationError(phone: String): String? {
        val clean = phone.trim()
        if (clean.isEmpty()) {
            return "يرجى إدخال رقم الهاتف السوري"
        }
        val normalized = normalizeToInternational(clean)
        if (!normalized.startsWith("+9639")) {
            return "يجب أن يبدأ الرقم بـ 09 (مثال: 0933123456)"
        }
        if (normalized.length != 13) {
            return "رقم الهاتف السوري يجب أن يتألف من 10 أرقام (09xxxxxxxx)"
        }
        if (!isValidSyrianNumber(clean)) {
            return "الرقم لا يتبع للشبكات السورية المعتمدة (093, 094, 095, 096, 098, 099)"
        }
        return null
    }
}
