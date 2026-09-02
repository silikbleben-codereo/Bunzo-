package com.example.utils

import java.text.NumberFormat
import java.util.Locale

object PriceFormatter {
    private val numberFormat = NumberFormat.getNumberInstance(Locale("ar", "SY")).apply {
        maximumFractionDigits = 0
    }
    private val englishNumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    fun formatPrice(amount: Double, inArabic: Boolean = true): String {
        val formatted = if (inArabic) {
            numberFormat.format(amount)
        } else {
            englishNumberFormat.format(amount)
        }
        return if (inArabic) "$formatted ل.س" else "$formatted SYP"
    }

    fun formatPrice(amount: Int, inArabic: Boolean = true): String {
        return formatPrice(amount.toDouble(), inArabic)
    }
}
