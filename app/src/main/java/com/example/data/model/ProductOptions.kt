package com.example.data.model

data class ProductSize(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val priceDelta: Double = 0.0,
    val isDefault: Boolean = false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "nameAr" to nameAr,
            "nameEn" to nameEn,
            "priceDelta" to priceDelta,
            "isDefault" to isDefault
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): ProductSize {
            return ProductSize(
                id = map["id"] as? String ?: "",
                nameAr = map["nameAr"] as? String ?: "",
                nameEn = map["nameEn"] as? String ?: "",
                priceDelta = (map["priceDelta"] as? Number)?.toDouble() ?: 0.0,
                isDefault = map["isDefault"] as? Boolean ?: false
            )
        }
    }
}

data class ProductExtra(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val price: Double = 0.0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "nameAr" to nameAr,
            "nameEn" to nameEn,
            "price" to price
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): ProductExtra {
            return ProductExtra(
                id = map["id"] as? String ?: "",
                nameAr = map["nameAr"] as? String ?: "",
                nameEn = map["nameEn"] as? String ?: "",
                price = (map["price"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}
