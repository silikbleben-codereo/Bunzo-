package com.example.data.model

enum class OrderStatus(val labelAr: String, val labelEn: String, val stepIndex: Int) {
    NEW("طلب جديد", "New", 0),
    ACCEPTED("تم قبول الطلب", "Accepted", 1),
    PREPARING("قيد التحضير", "Preparing", 2),
    READY("جاهز للتسليم", "Ready", 3),
    OUT_FOR_DELIVERY("جاري التوصيل", "Out for Delivery", 4),
    DELIVERED("تم التسليم", "Delivered", 5),
    CANCELLED("ملغي", "Cancelled", -1);

    companion object {
        fun fromString(value: String?): OrderStatus {
            if (value == null) return NEW
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: when (value.uppercase()) {
                "SENT" -> NEW
                "RECEIVED" -> ACCEPTED
                "PREPARATION" -> PREPARING
                "DELIVERY" -> OUT_FOR_DELIVERY
                "RECEIPT" -> DELIVERED
                else -> NEW
            }
        }
    }
}

enum class OrderType(val labelAr: String, val labelEn: String) {
    DELIVERY("توصيل للمنزل", "Delivery"),
    PICKUP("استلام من الفرع", "Pickup"),
    TABLE_ORDER("طلب داخل الصالة", "Table Order");

    companion object {
        fun fromString(value: String?): OrderType {
            if (value == null) return DELIVERY
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: DELIVERY
        }
    }
}

enum class PaymentMethod(val labelAr: String, val labelEn: String) {
    CASH("نقداً عند الاستلام", "Cash"),
    CARD("بطاقة إلكترونية", "Card"),
    PAY_AT_DOOR("دفع عند الباب (POS)", "Pay at Door");

    companion object {
        fun fromString(value: String?): PaymentMethod {
            if (value == null) return CASH
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: CASH
        }
    }
}

/**
 * Immutable snapshot of meal configuration at the time the order was placed.
 */
data class OrderItemRecord(
    val productId: String = "",
    val productNameAr: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0, // Snapshot price at time of order
    val selectedSizeNameAr: String? = null,
    val selectedExtrasDescription: String = "",
    val itemNotes: String = ""
) {
    val totalPrice: Double
        get() = unitPrice * quantity

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "productId" to productId,
            "productNameAr" to productNameAr,
            "quantity" to quantity,
            "unitPrice" to unitPrice,
            "selectedSizeNameAr" to selectedSizeNameAr,
            "selectedExtrasDescription" to selectedExtrasDescription,
            "itemNotes" to itemNotes
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): OrderItemRecord {
            return OrderItemRecord(
                productId = map["productId"] as? String ?: "",
                productNameAr = map["productNameAr"] as? String ?: "",
                quantity = (map["quantity"] as? Number)?.toInt() ?: 1,
                unitPrice = (map["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                selectedSizeNameAr = map["selectedSizeNameAr"] as? String,
                selectedExtrasDescription = map["selectedExtrasDescription"] as? String ?: "",
                itemNotes = map["itemNotes"] as? String ?: ""
            )
        }
    }
}

data class Order(
    val id: String = "",
    val orderNumber: String = "",
    val referenceNumber: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: OrderStatus = OrderStatus.NEW,
    val orderType: OrderType = OrderType.DELIVERY,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val items: List<OrderItemRecord> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val region: String = "دمشق",
    val address: String = "",
    val additionalAddress: String = "",
    val notes: String = "",
    val estimatedDeliveryMinutes: Int = 30,
    val branchId: String = "branch_damascus_mezzah",
    val branchNameAr: String = "فرع دمشق - المزة",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val assignedDriverId: String? = null,
    val assignedDriverName: String? = null,
    val paymentStatus: String = "PENDING",
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Sanitized Least-Privilege view strictly for Kitchen display screens (KDS).
     * Strips customer phone number, street address, and financials.
     */
    fun toKitchenView(): KitchenOrderView {
        return KitchenOrderView(
            id = id,
            orderNumber = orderNumber,
            customerName = customerName.ifBlank { "عميل بونزوا" },
            items = items.map {
                KitchenOrderItem(
                    productNameAr = it.productNameAr,
                    quantity = it.quantity,
                    selectedSizeNameAr = it.selectedSizeNameAr,
                    selectedExtrasDescription = it.selectedExtrasDescription,
                    itemNotes = it.itemNotes
                )
            },
            orderFoodNotes = notes,
            timestamp = timestamp,
            status = status,
            orderType = orderType,
            branchId = branchId
        )
    }

    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "orderNumber" to orderNumber,
            "referenceNumber" to referenceNumber,
            "date" to date,
            "timestamp" to timestamp,
            "status" to status.name,
            "orderType" to orderType.name,
            "paymentMethod" to paymentMethod.name,
            "items" to items.map { it.toMap() },
            "subtotal" to subtotal,
            "deliveryFee" to deliveryFee,
            "discountAmount" to discountAmount,
            "totalAmount" to totalAmount,
            "customerId" to customerId,
            "customerName" to customerName,
            "customerPhone" to customerPhone,
            "region" to region,
            "address" to address,
            "additionalAddress" to additionalAddress,
            "notes" to notes,
            "estimatedDeliveryMinutes" to estimatedDeliveryMinutes,
            "branchId" to branchId,
            "branchNameAr" to branchNameAr,
            "latitude" to latitude,
            "longitude" to longitude,
            "assignedDriverId" to assignedDriverId,
            "assignedDriverName" to assignedDriverName,
            "paymentStatus" to paymentStatus,
            "updatedAt" to System.currentTimeMillis()
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestore(docId: String, data: Map<String, Any?>?): Order {
            if (data == null) return Order(id = docId)
            val itemsRaw = (data["items"] as? List<Map<String, Any?>>) ?: emptyList()

            return Order(
                id = docId,
                orderNumber = data["orderNumber"] as? String ?: "#BNZ-${docId.takeLast(5)}",
                referenceNumber = data["referenceNumber"] as? String ?: "REF-963-${(1000..9999).random()}",
                date = data["date"] as? String ?: "",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                status = OrderStatus.fromString(data["status"] as? String),
                orderType = OrderType.fromString(data["orderType"] as? String),
                paymentMethod = PaymentMethod.fromString(data["paymentMethod"] as? String),
                items = itemsRaw.map { OrderItemRecord.fromMap(it) },
                subtotal = (data["subtotal"] as? Number)?.toDouble() ?: 0.0,
                deliveryFee = (data["deliveryFee"] as? Number)?.toDouble() ?: 0.0,
                discountAmount = (data["discountAmount"] as? Number)?.toDouble() ?: 0.0,
                totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                customerId = data["customerId"] as? String ?: (data["userId"] as? String ?: ""),
                customerName = data["customerName"] as? String ?: "",
                customerPhone = data["customerPhone"] as? String ?: "",
                region = data["region"] as? String ?: "دمشق",
                address = data["address"] as? String ?: "",
                additionalAddress = data["additionalAddress"] as? String ?: "",
                notes = data["notes"] as? String ?: "",
                estimatedDeliveryMinutes = (data["estimatedDeliveryMinutes"] as? Number)?.toInt() ?: 30,
                branchId = data["branchId"] as? String ?: "branch_damascus_mezzah",
                branchNameAr = data["branchNameAr"] as? String ?: "فرع دمشق - المزة",
                latitude = (data["latitude"] as? Number)?.toDouble(),
                longitude = (data["longitude"] as? Number)?.toDouble(),
                assignedDriverId = data["assignedDriverId"] as? String,
                assignedDriverName = data["assignedDriverName"] as? String,
                paymentStatus = data["paymentStatus"] as? String ?: "PENDING",
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Stripped and secure model for Kitchen display screens.
 * Contains only data necessary to prepare meals. Zero financial or customer contact data.
 */
data class KitchenOrderItem(
    val productNameAr: String = "",
    val quantity: Int = 1,
    val selectedSizeNameAr: String? = null,
    val selectedExtrasDescription: String = "",
    val itemNotes: String = ""
)

data class KitchenOrderView(
    val id: String = "",
    val orderNumber: String = "",
    val customerName: String = "",
    val items: List<KitchenOrderItem> = emptyList(),
    val orderFoodNotes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: OrderStatus = OrderStatus.NEW,
    val orderType: OrderType = OrderType.DELIVERY,
    val branchId: String = "branch_damascus_mezzah"
)
