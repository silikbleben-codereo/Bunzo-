package com.example.data.model

enum class UserRole(val roleKey: String, val labelAr: String) {
    CUSTOMER("customer", "زبون"),
    KITCHEN("kitchen", "شاشة المطبخ (KDS)"),
    ADMIN("admin", "مدير النظام (Admin)"),
    DELIVERY("delivery", "مندوب التوصيل"),
    MANAGER("manager", "مشرف الفرع"),
    OWNER("owner", "المالك");

    val isAdmin: Boolean
        get() = this == ADMIN || this == OWNER

    val isKitchen: Boolean
        get() = this == KITCHEN || this == ADMIN || this == OWNER || this == MANAGER

    val isStaff: Boolean
        get() = this != CUSTOMER

    val isManager: Boolean
        get() = this == MANAGER || this == ADMIN || this == OWNER

    companion object {
        fun fromKey(key: String?): UserRole {
            return entries.find { it.roleKey.equals(key, ignoreCase = true) } ?: CUSTOMER
        }
    }
}

data class User(
    val id: String = "",
    val name: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "", // Syrian E.164 (+9639xxxxxxxx)
    val email: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val phoneVerified: Boolean = false,
    val phoneVerifiedAt: Long? = null,
    val firebaseUid: String? = null,
    val country: String = "Syria",
    val region: String = "دمشق",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val token: String? = null,
    val isActive: Boolean = true
) {
    val fullName: String
        get() {
            if (name.isNotBlank()) return name.trim()
            val combined = "$firstName $lastName".trim()
            return combined.ifBlank { "مستخدم بونزوا" }
        }

    val phoneNumber: String
        get() = phone

    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to fullName,
            "firstName" to firstName,
            "lastName" to lastName,
            "phone" to phone,
            "email" to email,
            "role" to role.roleKey,
            "phoneVerified" to phoneVerified,
            "phoneVerifiedAt" to phoneVerifiedAt,
            "firebaseUid" to firebaseUid,
            "country" to country,
            "region" to region,
            "address" to address,
            "createdAt" to createdAt,
            "updatedAt" to System.currentTimeMillis(),
            "isActive" to isActive
        )
    }

    companion object {
        fun fromFirestore(docId: String, data: Map<String, Any?>?): User {
            if (data == null) return User(id = docId)
            return User(
                id = docId,
                name = data["name"] as? String ?: "",
                firstName = data["firstName"] as? String ?: "",
                lastName = data["lastName"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                email = data["email"] as? String ?: "",
                role = UserRole.fromKey(data["role"] as? String),
                phoneVerified = data["phoneVerified"] as? Boolean ?: false,
                phoneVerifiedAt = (data["phoneVerifiedAt"] as? Number)?.toLong(),
                firebaseUid = data["firebaseUid"] as? String,
                country = data["country"] as? String ?: "Syria",
                region = data["region"] as? String ?: "دمشق",
                address = data["address"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isActive = data["isActive"] as? Boolean ?: true
            )
        }
    }
}
