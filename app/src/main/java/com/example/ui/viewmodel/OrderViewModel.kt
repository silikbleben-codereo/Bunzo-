package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BunzoApplication
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.PaymentMethod
import com.example.data.repository.BunzoRepository
import com.example.utils.SyrianPhoneValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: BunzoRepository = BunzoApplication.instance.repository
) : ViewModel() {

    val orders: StateFlow<List<Order>> = repository.getCustomerOrders()
    val allOrders: StateFlow<List<Order>> = repository.getAllOrders()

    private val _currentActiveOrder = MutableStateFlow<Order?>(null)
    val currentActiveOrder: StateFlow<Order?> = _currentActiveOrder.asStateFlow()

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    private val _orderError = MutableStateFlow<String?>(null)
    val orderError: StateFlow<String?> = _orderError.asStateFlow()

    fun getOrderById(orderId: String): Order? {
        return repository.getOrderById(orderId)
    }

    fun observeSingleOrder(orderId: String): Flow<Order?> {
        return repository.observeSingleOrder(orderId)
    }

    fun placeOrder(
        cartItems: List<CartItem>,
        orderType: OrderType,
        paymentMethod: PaymentMethod,
        customerName: String,
        customerPhone: String,
        region: String,
        address: String,
        additionalAddress: String,
        notes: String,
        branchId: String = "branch_damascus_mezzah",
        latitude: Double? = null,
        longitude: Double? = null,
        onSuccess: (Order) -> Unit
    ) {
        if (cartItems.isEmpty()) {
            _orderError.value = "السلة فارغة، يرجى إضافة وجبات أولاً"
            return
        }
        val phoneErr = SyrianPhoneValidator.getValidationError(customerPhone)
        if (phoneErr != null) {
            _orderError.value = phoneErr
            return
        }
        if (orderType == OrderType.DELIVERY && address.isBlank()) {
            _orderError.value = "يرجى تحديد عنوان التوصيل بالتفصيل"
            return
        }

        _isPlacingOrder.value = true
        _orderError.value = null

        viewModelScope.launch {
            val result = repository.placeOrder(
                cartItems = cartItems,
                orderType = orderType,
                paymentMethod = paymentMethod,
                customerName = customerName,
                customerPhone = customerPhone,
                region = region,
                address = address,
                additionalAddress = additionalAddress,
                notes = notes,
                branchId = branchId,
                latitude = latitude,
                longitude = longitude
            )
            _isPlacingOrder.value = false
            result.onSuccess { newOrder ->
                _currentActiveOrder.value = newOrder
                onSuccess(newOrder)
            }.onFailure {
                _orderError.value = it.message ?: "فشل إرسال الطلب إلى الخادم السحابي، يرجى التحقق من اتصالك"
            }
        }
    }

    fun advanceOrderStep(orderId: String) {
        val order = getOrderById(orderId) ?: return
        val nextStatus = when (order.status) {
            OrderStatus.NEW -> OrderStatus.ACCEPTED
            OrderStatus.ACCEPTED -> OrderStatus.PREPARING
            OrderStatus.PREPARING -> OrderStatus.READY
            OrderStatus.READY -> OrderStatus.OUT_FOR_DELIVERY
            OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.DELIVERED
            OrderStatus.DELIVERED -> OrderStatus.DELIVERED
            OrderStatus.CANCELLED -> OrderStatus.CANCELLED
        }

        viewModelScope.launch {
            repository.updateOrderStatus(orderId, nextStatus)
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, OrderStatus.CANCELLED)
        }
    }

    fun clearError() {
        _orderError.value = null
    }
}
