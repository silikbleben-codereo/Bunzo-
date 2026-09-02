package com.example.data.model

data class Branch(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val addressAr: String = "",
    val addressEn: String = "",
    val phone: String = "",
    val mobile: String = "",
    val email: String = "",
    val imageUrl: String = "",
    val latitude: Double = 33.5138,
    val longitude: Double = 36.2765,
    val openingHoursAr: String = "يومياً 11:00 ص - 02:00 ليلاً",
    val openingHoursEn: String = "Daily 11:00 AM - 02:00 AM",
    val isOpen: Boolean = true,
    val assignedStaffIds: List<String> = emptyList()
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "nameAr" to nameAr,
            "nameEn" to nameEn,
            "addressAr" to addressAr,
            "addressEn" to addressEn,
            "phone" to phone,
            "mobile" to mobile,
            "email" to email,
            "imageUrl" to imageUrl,
            "latitude" to latitude,
            "longitude" to longitude,
            "openingHoursAr" to openingHoursAr,
            "openingHoursEn" to openingHoursEn,
            "isOpen" to isOpen,
            "assignedStaffIds" to assignedStaffIds
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestore(docId: String, data: Map<String, Any?>?): Branch {
            if (data == null) return Branch(id = docId)
            return Branch(
                id = docId,
                nameAr = data["nameAr"] as? String ?: "",
                nameEn = data["nameEn"] as? String ?: "",
                addressAr = data["addressAr"] as? String ?: "",
                addressEn = data["addressEn"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                mobile = data["mobile"] as? String ?: "",
                email = data["email"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String ?: "",
                latitude = (data["latitude"] as? Number)?.toDouble() ?: 33.5138,
                longitude = (data["longitude"] as? Number)?.toDouble() ?: 36.2765,
                openingHoursAr = data["openingHoursAr"] as? String ?: "يومياً 11:00 ص - 02:00 ليلاً",
                openingHoursEn = data["openingHoursEn"] as? String ?: "Daily 11:00 AM - 02:00 AM",
                isOpen = data["isOpen"] as? Boolean ?: true,
                assignedStaffIds = (data["assignedStaffIds"] as? List<String>) ?: emptyList()
            )
        }
    }
}
