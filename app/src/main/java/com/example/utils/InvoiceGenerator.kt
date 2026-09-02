package com.example.utils

import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FinancialSummary(
    val totalRevenue: Double,
    val totalOrdersCount: Int,
    val deliveredOrdersCount: Int,
    val cancelledOrdersCount: Int,
    val cashRevenue: Double,
    val cardRevenue: Double,
    val averageOrderValue: Double,
    val totalDeliveryFeesCollected: Double,
    val totalDiscountsGiven: Double
)

object InvoiceGenerator {

    fun calculateFinancialSummary(orders: List<Order>): FinancialSummary {
        val nonCancelled = orders.filter { it.status != OrderStatus.CANCELLED }
        val delivered = orders.filter { it.status == OrderStatus.DELIVERED }
        val cancelled = orders.filter { it.status == OrderStatus.CANCELLED }

        val totalRev = nonCancelled.sumOf { it.totalAmount }
        val cashRev = nonCancelled.filter { it.paymentMethod == PaymentMethod.CASH || it.paymentMethod == PaymentMethod.PAY_AT_DOOR }.sumOf { it.totalAmount }
        val cardRev = nonCancelled.filter { it.paymentMethod == PaymentMethod.CARD }.sumOf { it.totalAmount }
        val deliveryFees = nonCancelled.sumOf { it.deliveryFee }
        val discounts = nonCancelled.sumOf { it.discountAmount }
        val avgValue = if (nonCancelled.isNotEmpty()) totalRev / nonCancelled.size else 0.0

        return FinancialSummary(
            totalRevenue = totalRev,
            totalOrdersCount = orders.size,
            deliveredOrdersCount = delivered.size,
            cancelledOrdersCount = cancelled.size,
            cashRevenue = cashRev,
            cardRevenue = cardRev,
            averageOrderValue = avgValue,
            totalDeliveryFeesCollected = deliveryFees,
            totalDiscountsGiven = discounts
        )
    }

    fun generateReceiptText(order: Order): String {
        val sb = StringBuilder()
        sb.appendLine("========================================")
        sb.appendLine("        مطعم بونزوا للوجبات السريعة        ")
        sb.appendLine("          BUNZO FAST FOOD SYRIA         ")
        sb.appendLine("========================================")
        sb.appendLine("رقم الفاتورة: ${order.orderNumber}")
        sb.appendLine("الرقم المرجعي: ${order.referenceNumber}")
        sb.appendLine("التاريخ: ${order.date}")
        sb.appendLine("الفرع: ${order.branchNameAr}")
        sb.appendLine("نوع الطلب: ${order.orderType.labelAr}")
        sb.appendLine("طريقة الدفع: ${order.paymentMethod.labelAr}")
        sb.appendLine("حالة الدفع: ${if (order.paymentStatus == "PAID" || order.status == OrderStatus.DELIVERED) "مدفوع بالكامل" else "بانتظار التحصيل"}")
        sb.appendLine("----------------------------------------")
        sb.appendLine("بيانات العميل:")
        sb.appendLine("الاسم: ${order.customerName}")
        sb.appendLine("الهاتف: ${order.customerPhone}")
        sb.appendLine("العنوان: ${order.region} - ${order.address}")
        if (order.latitude != null && order.longitude != null) {
            sb.appendLine("إحداثيات GPS: ${order.latitude}, ${order.longitude}")
        }
        sb.appendLine("----------------------------------------")
        sb.appendLine("الأصناف المطلوبة:")
        order.items.forEachIndexed { idx, item ->
            val sizeStr = if (!item.selectedSizeNameAr.isNullOrBlank()) " (${item.selectedSizeNameAr})" else ""
            sb.appendLine("${idx + 1}. ${item.productNameAr}$sizeStr")
            sb.appendLine("   الكمية: ${item.quantity} x ${PriceFormatter.formatPrice(item.unitPrice)} = ${PriceFormatter.formatPrice(item.totalPrice)}")
            if (item.selectedExtrasDescription.isNotBlank()) {
                sb.appendLine("   + إضافات: ${item.selectedExtrasDescription}")
            }
            if (item.itemNotes.isNotBlank()) {
                sb.appendLine("   * ملاحظة: ${item.itemNotes}")
            }
        }
        sb.appendLine("----------------------------------------")
        sb.appendLine("المجموع الفرعي: ${PriceFormatter.formatPrice(order.subtotal)}")
        if (order.deliveryFee > 0) {
            sb.appendLine("رسوم التوصيل: ${PriceFormatter.formatPrice(order.deliveryFee)}")
        }
        if (order.discountAmount > 0) {
            sb.appendLine("قيمة الخصم: -${PriceFormatter.formatPrice(order.discountAmount)}")
        }
        sb.appendLine("----------------------------------------")
        sb.appendLine("الإجمالي المستحق: ${PriceFormatter.formatPrice(order.totalAmount)}")
        sb.appendLine("========================================")
        sb.appendLine("شكراً لاختياركم مطعم بونزوا! نتمنى لكم وجبة شهية 🍔")
        sb.appendLine("الرقم الضريبي الموحد: SYR-TAX-984210")
        sb.appendLine("========================================")
        return sb.toString()
    }

    fun generateCsvReport(orders: List<Order>): String {
        val sb = StringBuilder()
        sb.appendLine("Order Number,Reference,Date,Customer Name,Phone,Region,Address,Order Type,Status,Payment Method,Subtotal (SYP),Delivery Fee (SYP),Discount (SYP),Total Amount (SYP),GPS Coordinates")
        orders.forEach { o ->
            val gps = if (o.latitude != null && o.longitude != null) "\"${o.latitude},${o.longitude}\"" else "\"\""
            sb.appendLine("\"${o.orderNumber}\",\"${o.referenceNumber}\",\"${o.date}\",\"${o.customerName}\",\"${o.customerPhone}\",\"${o.region}\",\"${o.address}\",\"${o.orderType.labelAr}\",\"${o.status.labelAr}\",\"${o.paymentMethod.labelAr}\",${o.subtotal},${o.deliveryFee},${o.discountAmount},${o.totalAmount},$gps")
        }
        return sb.toString()
    }
}
