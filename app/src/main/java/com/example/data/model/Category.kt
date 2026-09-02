package com.example.data.model

data class Category(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val imageUrl: String = "",
    val productCount: Int = 0,
    val iconName: String = "burger",
    val sortOrder: Int = 0,
    val isActive: Boolean = true
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "nameAr" to nameAr,
            "nameEn" to nameEn,
            "imageUrl" to imageUrl,
            "productCount" to productCount,
            "iconName" to iconName,
            "sortOrder" to sortOrder,
            "isActive" to isActive
        )
    }

    companion object {
        fun fromFirestore(docId: String, data: Map<String, Any?>?): Category {
            if (data == null) return Category(id = docId)
            return Category(
                id = docId,
                nameAr = data["nameAr"] as? String ?: "",
                nameEn = data["nameEn"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String ?: "",
                productCount = (data["productCount"] as? Number)?.toInt() ?: 0,
                iconName = data["iconName"] as? String ?: "burger",
                sortOrder = (data["sortOrder"] as? Number)?.toInt() ?: 0,
                isActive = data["isActive"] as? Boolean ?: true
            )
        }
    }
}
