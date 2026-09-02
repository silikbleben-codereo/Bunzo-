package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BunzoApplication
import com.example.data.model.KitchenOrderView
import com.example.data.model.OrderStatus
import com.example.data.repository.BunzoRepository
import com.example.utils.SoundHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class KitchenFilter(val labelAr: String) {
    ALL_ACTIVE("جميع طلبات المطبخ"),
    NEW_ONLY("جديد ⏳"),
    PREPARING_ONLY("قيد الطبخ 🍳"),
    READY_ONLY("جاهز للتسليم 📦")
}

class KitchenViewModel(
    private val repository: BunzoRepository = BunzoApplication.instance.repository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(KitchenFilter.ALL_ACTIVE)
    val selectedFilter: StateFlow<KitchenFilter> = _selectedFilter.asStateFlow()

    private val _selectedBranchFilter = MutableStateFlow<String?>(null)
    val selectedBranchFilter: StateFlow<String?> = _selectedBranchFilter.asStateFlow()

    /**
     * Sanitized Least-Privilege kitchen view stream.
     * Contains only meal preparation tickets with NO customer contact info or financial totals.
     */
    val kitchenTickets: StateFlow<List<KitchenOrderView>> = repository.getKitchenOrders().map { orders ->
        orders.map { it.toKitchenView() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: KitchenFilter) {
        _selectedFilter.value = filter
    }

    fun setBranchFilter(branchId: String?) {
        _selectedBranchFilter.value = branchId
    }

    fun markAccepted(orderId: String) {
        SoundHelper.playStatusUpdateSound()
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, OrderStatus.ACCEPTED)
        }
    }

    fun markPreparing(orderId: String) {
        SoundHelper.playStatusUpdateSound()
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, OrderStatus.PREPARING)
        }
    }

    fun markReady(orderId: String) {
        SoundHelper.playStatusUpdateSound()
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, OrderStatus.READY)
        }
    }

    fun getElapsedTimeText(timestamp: Long): String {
        val diffMinutes = ((System.currentTimeMillis() - timestamp) / (1000 * 60)).coerceAtLeast(0)
        return if (diffMinutes == 0L) "الآن" else "منذ $diffMinutes دقيقة"
    }
}
