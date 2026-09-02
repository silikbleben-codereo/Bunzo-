package com.example.data.model

data class Product(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val descriptionAr: String = "",
    val descriptionEn: String = "",
    val price: Double = 0.0,
    val oldPrice: Double? = null,
    val discountPercent: Int = 0,
    val categoryId: String = "",
    val categoryNameAr: String = "",
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val isOffer: Boolean = false,
    val barcode: String = "",
    val unit: String = "وجبة",
    val sizes: List<ProductSize> = emptyList(),
    val extras: List<ProductExtra> = emptyList(),
    val rating: Float = 4.8f,
    val reviewCount: Int = 120,
    val prepTimeMinutes: Int = 15,
    val calories: Int = 450
) {
    val hasDiscount: Boolean
        get() = oldPrice != null && oldPrice > price

    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "nameAr" to nameAr,
            "nameEn" to nameEn,
            "descriptionAr" to descriptionAr,
            "descriptionEn" to descriptionEn,
            "price" to price,
            "oldPrice" to oldPrice,
            "discountPercent" to discountPercent,
            "categoryId" to categoryId,
            "categoryNameAr" to categoryNameAr,
            "imageUrl" to imageUrl,
            "isAvailable" to isAvailable,
            "isFeatured" to isFeatured,
            "isOffer" to isOffer,
            "barcode" to barcode,
            "unit" to unit,
            "sizes" to sizes.map { it.toMap() },
            "extras" to extras.map { it.toMap() },
            "rating" to rating.toDouble(),
            "reviewCount" to reviewCount,
            "prepTimeMinutes" to prepTimeMinutes,
            "calories" to calories
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestore(docId: String, data: Map<String, Any?>?): Product {
            if (data == null) return Product(id = docId)
            val sizesRaw = (data["sizes"] as? List<Map<String, Any?>>) ?: emptyList()
            val extrasRaw = (data["extras"] as? List<Map<String, Any?>>) ?: emptyList()

            return Product(
                id = docId,
                nameAr = data["nameAr"] as? String ?: "",
                nameEn = data["nameEn"] as? String ?: "",
                descriptionAr = data["descriptionAr"] as? String ?: "",
                descriptionEn = data["descriptionEn"] as? String ?: "",
                price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                oldPrice = (data["oldPrice"] as? Number)?.toDouble(),
                discountPercent = (data["discountPercent"] as? Number)?.toInt() ?: 0,
                categoryId = data["categoryId"] as? String ?: "",
                categoryNameAr = data["categoryNameAr"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String ?: "",
                isAvailable = data["isAvailable"] as? Boolean ?: true,
                isFeatured = data["isFeatured"] as? Boolean ?: false,
                isOffer = data["isOffer"] as? Boolean ?: false,
                barcode = data["barcode"] as? String ?: "",
                unit = data["unit"] as? String ?: "وجبة",
                sizes = sizesRaw.map { ProductSize.fromMap(it) },
                extras = extrasRaw.map { ProductExtra.fromMap(it) },
                rating = (data["rating"] as? Number)?.toFloat() ?: 4.8f,
                reviewCount = (data["reviewCount"] as? Number)?.toInt() ?: 120,
                prepTimeMinutes = (data["prepTimeMinutes"] as? Number)?.toInt() ?: 15,
                calories = (data["calories"] as? Number)?.toInt() ?: 450
            )
        }
    }
}
