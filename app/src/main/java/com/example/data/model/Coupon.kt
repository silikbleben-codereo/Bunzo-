package com.example.data.model

data class Coupon(
    val id: String = "",
    val code: String = "",
    val discountPercent: Int = 0,
    val discountAmount: Double = 0.0,
    val minOrderAmount: Double = 0.0,
    val descriptionAr: String = "",
    val isActive: Boolean = true
) {
    fun calculateDiscount(subtotal: Double): Double {
        if (subtotal < minOrderAmount) return 0.0
        return if (discountPercent > 0) {
            (subtotal * discountPercent / 100.0)
        } else {
            discountAmount.coerceAtMost(subtotal)
        }
    }

    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "code" to code,
            "discountPercent" to discountPercent,
            "discountAmount" to discountAmount,
            "minOrderAmount" to minOrderAmount,
            "descriptionAr" to descriptionAr,
            "isActive" to isActive
        )
    }

    companion object {
        fun fromFirestore(docId: String, data: Map<String, Any?>?): Coupon {
            if (data == null) return Coupon(id = docId)
            return Coupon(
                id = docId,
                code = data["code"] as? String ?: "",
                discountPercent = (data["discountPercent"] as? Number)?.toInt() ?: 0,
                discountAmount = (data["discountAmount"] as? Number)?.toDouble() ?: 0.0,
                minOrderAmount = (data["minOrderAmount"] as? Number)?.toDouble() ?: 0.0,
                descriptionAr = data["descriptionAr"] as? String ?: "",
                isActive = data["isActive"] as? Boolean ?: true
            )
        }
    }
}
