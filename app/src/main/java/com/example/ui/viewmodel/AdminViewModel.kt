package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BunzoApplication
import com.example.data.model.AppSetting
import com.example.data.model.AuditLog
import com.example.data.model.Branch
import com.example.data.model.Category
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.BunzoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminDashboardStats(
    val totalRevenue: Double = 0.0,
    val todayRevenue: Double = 0.0,
    val totalOrdersCount: Int = 0,
    val newOrdersCount: Int = 0,
    val preparingOrdersCount: Int = 0,
    val readyOrdersCount: Int = 0,
    val deliveredOrdersCount: Int = 0,
    val cancelledOrdersCount: Int = 0,
    val topSellingProducts: List<Pair<String, Int>> = emptyList()
)

class AdminViewModel(
    private val repository: BunzoRepository = BunzoApplication.instance.repository
) : ViewModel() {

    val allOrders: StateFlow<List<Order>> = repository.getAllOrders()
    val allProducts: StateFlow<List<Product>> = repository.getAllProducts()
    val categories: StateFlow<List<Category>> = repository.getCategories()
    val branches: StateFlow<List<Branch>> = repository.getBranches()
    val settings: StateFlow<AppSetting> = repository.getSettings()
    val auditLogs: StateFlow<List<AuditLog>> = repository.auditLogsState
    val usersList: StateFlow<List<User>> = repository.usersListState

    private val _orderStatusFilter = MutableStateFlow<OrderStatus?>(null)
    val orderStatusFilter: StateFlow<OrderStatus?> = _orderStatusFilter.asStateFlow()

    private val _branchFilter = MutableStateFlow<String?>(null)
    val branchFilter: StateFlow<String?> = _branchFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _adminMessage = MutableStateFlow<String?>(null)
    val adminMessage: StateFlow<String?> = _adminMessage.asStateFlow()

    val filteredOrders: StateFlow<List<Order>> = combine(
        allOrders,
        _orderStatusFilter,
        _branchFilter,
        _searchQuery
    ) { orders, statusFilter, branchFilter, query ->
        orders.filter { order ->
            val matchesStatus = statusFilter == null || order.status == statusFilter
            val matchesBranch = branchFilter == null || order.branchId == branchFilter
            val q = query.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                order.orderNumber.lowercase().contains(q) ||
                order.customerName.lowercase().contains(q) ||
                order.customerPhone.contains(q)
            matchesStatus && matchesBranch && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<AdminDashboardStats> = allOrders.map { orders: List<Order> ->
        val validOrders = orders.filter { it.status != OrderStatus.CANCELLED }
        val totalRev = validOrders.sumOf { it.totalAmount }

        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        val todayRev = validOrders.filter { it.timestamp >= oneDayAgo }.sumOf { it.totalAmount }

        val itemCounts = mutableMapOf<String, Int>()
        validOrders.flatMap { it.items }.forEach { item ->
            itemCounts[item.productNameAr] = (itemCounts[item.productNameAr] ?: 0) + item.quantity
        }
        val topProducts = itemCounts.toList().sortedByDescending { it.second }.take(5)

        AdminDashboardStats(
            totalRevenue = totalRev,
            todayRevenue = todayRev,
            totalOrdersCount = orders.size,
            newOrdersCount = orders.count { it.status == OrderStatus.NEW || it.status == OrderStatus.ACCEPTED },
            preparingOrdersCount = orders.count { it.status == OrderStatus.PREPARING },
            readyOrdersCount = orders.count { it.status == OrderStatus.READY || it.status == OrderStatus.OUT_FOR_DELIVERY },
            deliveredOrdersCount = orders.count { it.status == OrderStatus.DELIVERED },
            cancelledOrdersCount = orders.count { it.status == OrderStatus.CANCELLED },
            topSellingProducts = topProducts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminDashboardStats())

    fun setOrderStatusFilter(status: OrderStatus?) {
        _orderStatusFilter.value = status
    }

    fun setBranchFilter(branchId: String?) {
        _branchFilter.value = branchId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            _adminMessage.value = "تم تحديث حالة الطلب إلى: ${newStatus.labelAr}"
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, OrderStatus.CANCELLED)
            _adminMessage.value = "تم إلغاء الطلب"
        }
    }

    fun toggleProductAvailability(productId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            val res = repository.toggleProductAvailability(productId, isAvailable)
            res.onSuccess {
                _adminMessage.value = if (isAvailable) "تم تفعيل توفر الوجبة" else "تم إيقاف توفر الوجبة"
            }.onFailure {
                _adminMessage.value = it.message
            }
        }
    }

    fun saveProduct(product: Product, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.saveProduct(product)
            res.onSuccess {
                _adminMessage.value = "تم حفظ بيانات الوجبة بنجاح ✅"
                onComplete()
            }.onFailure {
                _adminMessage.value = it.message ?: "فشل حفظ الوجبة"
            }
        }
    }

    fun saveCategory(category: Category, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.saveCategory(category)
            res.onSuccess {
                _adminMessage.value = "تم حفظ القسم بنجاح ✅"
                onComplete()
            }.onFailure {
                _adminMessage.value = it.message ?: "فشل حفظ القسم"
            }
        }
    }

    fun saveAppSettings(settings: AppSetting) {
        viewModelScope.launch {
            val res = repository.saveAppSettings(settings)
            res.onSuccess {
                _adminMessage.value = "تم تحديث إعدادات المطعم بنجاح ✅"
            }.onFailure {
                _adminMessage.value = it.message
            }
        }
    }

    fun assignUserRole(phone: String, role: UserRole) {
        viewModelScope.launch {
            val res = repository.updateStaffRole(phone, role)
            res.onSuccess {
                _adminMessage.value = "تم تحديث رتبة المستخدم ($phone) إلى ${role.labelAr} بنجاح"
            }.onFailure {
                _adminMessage.value = it.message
            }
        }
    }

    fun updateStaffRole(userId: String, role: UserRole) {
        viewModelScope.launch {
            val res = repository.updateStaffRole(userId, role)
            res.onSuccess {
                _adminMessage.value = "تم تحديث رتبة المستخدم إلى ${role.labelAr} بنجاح"
            }.onFailure {
                _adminMessage.value = it.message
            }
        }
    }

    fun clearAdminMessage() {
        _adminMessage.value = null
    }
}
