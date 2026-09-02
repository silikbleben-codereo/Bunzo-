package com.example.data.model

data class BannerOffer(
    val id: String = "",
    val titleAr: String = "",
    val titleEn: String = "",
    val subtitleAr: String = "",
    val subtitleEn: String = "",
    val badgeAr: String = "",
    val imageUrl: String = "",
    val targetProductId: String? = null,
    val targetCategoryId: String? = null,
    val isActive: Boolean = true
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "titleAr" to titleAr,
            "titleEn" to titleEn,
            "subtitleAr" to subtitleAr,
            "subtitleEn" to subtitleEn,
            "badgeAr" to badgeAr,
            "imageUrl" to imageUrl,
            "targetProductId" to targetProductId,
            "targetCategoryId" to targetCategoryId,
            "isActive" to isActive
        )
    }

    companion object {
        fun fromFirestore(docId: String, data: Map<String, Any?>?): BannerOffer {
            if (data == null) return BannerOffer(id = docId)
            return BannerOffer(
                id = docId,
                titleAr = data["titleAr"] as? String ?: "",
                titleEn = data["titleEn"] as? String ?: "",
                subtitleAr = data["subtitleAr"] as? String ?: "",
                subtitleEn = data["subtitleEn"] as? String ?: "",
                badgeAr = data["badgeAr"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String ?: "",
                targetProductId = data["targetProductId"] as? String,
                targetCategoryId = data["targetCategoryId"] as? String,
                isActive = data["isActive"] as? Boolean ?: true
            )
        }
    }
}

data class AppSetting(
    val restaurantNameAr: String = "مطعم بونزوا",
    val restaurantNameEn: String = "Bunzo Restaurant",
    val adminPhone: String = "+963949159274",
    val defaultPhone: String = "+963949159274",
    val whatsappNumber: String = "+963949159274",
    val facebookUrl: String = "https://facebook.com/bunzo.syria",
    val instagramUrl: String = "https://instagram.com/bunzo.syria",
    val twitterUrl: String = "https://twitter.com/bunzo_syria",
    val websiteUrl: String = "https://bunzo.restaurant",
    val minOrderAmount: Double = 15000.0,
    val deliveryFee: Double = 5000.0,
    val isStoreOpen: Boolean = true,
    val workingHoursAr: String = "يومياً 11:00 ص - 02:00 ليلاً",
    val currencyAr: String = "ل.س",
    val announcementAr: String = "أهلاً بكم في مطعم بونزوا - خدمة التوصيل السريع متاحة الآن لكافة أنحاء دمشق!"
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "restaurantNameAr" to restaurantNameAr,
            "restaurantNameEn" to restaurantNameEn,
            "adminPhone" to adminPhone,
            "defaultPhone" to defaultPhone,
            "whatsappNumber" to whatsappNumber,
            "facebookUrl" to facebookUrl,
            "instagramUrl" to instagramUrl,
            "twitterUrl" to twitterUrl,
            "websiteUrl" to websiteUrl,
            "minOrderAmount" to minOrderAmount,
            "deliveryFee" to deliveryFee,
            "isStoreOpen" to isStoreOpen,
            "workingHoursAr" to workingHoursAr,
            "currencyAr" to currencyAr,
            "announcementAr" to announcementAr
        )
    }

    companion object {
        fun fromFirestore(data: Map<String, Any?>?): AppSetting {
            if (data == null) return AppSetting()
            return AppSetting(
                restaurantNameAr = data["restaurantNameAr"] as? String ?: "مطعم بونزوا",
                restaurantNameEn = data["restaurantNameEn"] as? String ?: "Bunzo Restaurant",
                adminPhone = data["adminPhone"] as? String ?: "+963949159274",
                defaultPhone = data["defaultPhone"] as? String ?: "+963949159274",
                whatsappNumber = data["whatsappNumber"] as? String ?: "+963949159274",
                facebookUrl = data["facebookUrl"] as? String ?: "https://facebook.com/bunzo.syria",
                instagramUrl = data["instagramUrl"] as? String ?: "https://instagram.com/bunzo.syria",
                twitterUrl = data["twitterUrl"] as? String ?: "https://twitter.com/bunzo_syria",
                websiteUrl = data["websiteUrl"] as? String ?: "https://bunzo.restaurant",
                minOrderAmount = (data["minOrderAmount"] as? Number)?.toDouble() ?: 15000.0,
                deliveryFee = (data["deliveryFee"] as? Number)?.toDouble() ?: 5000.0,
                isStoreOpen = data["isStoreOpen"] as? Boolean ?: true,
                workingHoursAr = data["workingHoursAr"] as? String ?: "يومياً 11:00 ص - 02:00 ليلاً",
                currencyAr = data["currencyAr"] as? String ?: "ل.س",
                announcementAr = data["announcementAr"] as? String ?: "أهلاً بكم في مطعم بونزوا"
            )
        }
    }
}
