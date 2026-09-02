package com.example.data.model

data class AuditLog(
    val id: String = "",
    val actorId: String = "",
    val actorName: String = "",
    val actorRole: UserRole = UserRole.ADMIN,
    val action: String = "",
    val targetType: String = "", // "PRODUCT", "ORDER", "USER", "SETTINGS", "CATEGORY", "STAFF"
    val targetId: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "actorId" to actorId,
            "actorName" to actorName,
            "actorRole" to actorRole.roleKey,
            "action" to action,
            "targetType" to targetType,
            "targetId" to targetId,
            "description" to description,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromFirestore(docId: String, data: Map<String, Any?>?): AuditLog {
            if (data == null) return AuditLog(id = docId)
            return AuditLog(
                id = docId,
                actorId = data["actorId"] as? String ?: "",
                actorName = data["actorName"] as? String ?: "",
                actorRole = UserRole.fromKey(data["actorRole"] as? String),
                action = data["action"] as? String ?: "",
                targetType = data["targetType"] as? String ?: "",
                targetId = data["targetId"] as? String ?: "",
                description = data["description"] as? String ?: "",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
