package com.example.utils

import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class AppNotification(
    val id: String = "notif_${System.currentTimeMillis()}",
    val titleAr: String,
    val messageAr: String,
    val targetRole: UserRole? = null, // null means broadcast / user-specific
    val targetUserId: String? = null,
    val targetPhone: String? = null,
    val orderId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Enterprise In-App Notification Dispatcher for Bunzo Restaurant System.
 * Broadcasts real-time events between Customer, Kitchen, and Admin.
 */
object AppNotificationManager {
    private val _notifications = MutableSharedFlow<AppNotification>(extraBufferCapacity = 50)
    val notifications: SharedFlow<AppNotification> = _notifications.asSharedFlow()

    fun notifyNewOrder(order: Order) {
        SoundHelper.playKitchenNewOrderAlert()
        // 1. Notify Kitchen Staff (KDS)
        _notifications.tryEmit(
            AppNotification(
                titleAr = "طلب مطبخ جديد 👨‍🍳",
                messageAr = "وصل طلب جديد رقم ${order.orderNumber} يحتوي على ${order.items.size} وجبات للتحضير.",
                targetRole = UserRole.KITCHEN,
                orderId = order.id
            )
        )
        // 2. Notify Admin Dashboard
        _notifications.tryEmit(
            AppNotification(
                titleAr = "طلب جديد وارد 👑",
                messageAr = "تم استلام طلب جديد ${order.orderNumber} بإجمالي ${order.totalAmount} ل.س من العميل ${order.customerName}.",
                targetRole = UserRole.ADMIN,
                orderId = order.id
            )
        )
    }

    fun notifyOrderStatusChanged(order: Order, newStatus: OrderStatus) {
        SoundHelper.playStatusUpdateSound()
        val (title, msg) = when (newStatus) {
            OrderStatus.NEW -> "تم استلام طلبك" to "طلبك رقم ${order.orderNumber} قيد المراجعة."
            OrderStatus.ACCEPTED -> "تم قبول طلبك ✅" to "تم قبول طلبك رقم ${order.orderNumber} وسيتم البدء بتحضيره فوراً."
            OrderStatus.PREPARING -> "طلبك قيد التحضير 🍳" to "يقوم كادر المطبخ بتحضير وجباتك الساخنة الآن."
            OrderStatus.READY -> "طلبك جاهز للتسليم 📦" to "وجبتك أصبحت جاهزة للتسليم أو التوصيل."
            OrderStatus.OUT_FOR_DELIVERY -> "خرج للتوصيل 🛵" to "سائق بونزوا في طريقه إليك لتسليم طلبك ${order.orderNumber}."
            OrderStatus.DELIVERED -> "تم تسليم الطلب بنجاح 🎉" to "نتمنى لك وجبة شهية! شكراً لطلبك من مطعم بونزوا."
            OrderStatus.CANCELLED -> "تم إلغاء الطلب ⚠️" to "نعتذر، تم إلغاء الطلب رقم ${order.orderNumber}."
        }

        _notifications.tryEmit(
            AppNotification(
                titleAr = title,
                messageAr = msg,
                targetPhone = order.customerPhone,
                targetUserId = order.customerId,
                orderId = order.id
            )
        )
    }
}
