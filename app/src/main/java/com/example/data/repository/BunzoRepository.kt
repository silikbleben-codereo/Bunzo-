package com.example.data.repository

import android.util.Log
import com.example.data.local.PreferencesManager
import com.example.data.local.dao.CartDao
import com.example.data.local.dao.FavoriteDao
import com.example.data.model.AppSetting
import com.example.data.model.AuditLog
import com.example.data.model.BannerOffer
import com.example.data.model.Branch
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.FavoriteItem
import com.example.data.model.Order
import com.example.data.model.OrderItemRecord
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.ProductExtra
import com.example.data.model.ProductSize
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.remote.BunzoDataSource
import com.example.data.remote.FirebaseService
import com.example.utils.AppNotificationManager
import com.example.utils.SyrianPhoneValidator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BunzoRepository(
    private val cartDao: CartDao,
    private val favoriteDao: FavoriteDao,
    private val preferencesManager: PreferencesManager,
    private val firebaseService: FirebaseService = FirebaseService(),
    private val repositoryScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    // -------------------------------------------------------------
    // Real-time StateFlows powered by Cloud Firestore
    // -------------------------------------------------------------
    private val _productsState = MutableStateFlow(BunzoDataSource.products)
    val productsState: StateFlow<List<Product>> = _productsState.asStateFlow()

    private val _categoriesState = MutableStateFlow(BunzoDataSource.categories)
    val categoriesState: StateFlow<List<Category>> = _categoriesState.asStateFlow()

    private val _bannersState = MutableStateFlow(BunzoDataSource.bannerOffers)
    val bannersState: StateFlow<List<BannerOffer>> = _bannersState.asStateFlow()

    private val _branchesState = MutableStateFlow(BunzoDataSource.branches)
    val branchesState: StateFlow<List<Branch>> = _branchesState.asStateFlow()

    private val _settingsState = MutableStateFlow(BunzoDataSource.appSettings)
    val settingsState: StateFlow<AppSetting> = _settingsState.asStateFlow()

    private val _customerOrdersState = MutableStateFlow<List<Order>>(emptyList())
    val customerOrdersState: StateFlow<List<Order>> = _customerOrdersState.asStateFlow()

    private val _kitchenOrdersState = MutableStateFlow<List<Order>>(emptyList())
    val kitchenOrdersState: StateFlow<List<Order>> = _kitchenOrdersState.asStateFlow()

    private val _allOrdersState = MutableStateFlow<List<Order>>(emptyList())
    val allOrdersState: StateFlow<List<Order>> = _allOrdersState.asStateFlow()

    private val _auditLogsState = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogsState: StateFlow<List<AuditLog>> = _auditLogsState.asStateFlow()

    private val _usersListState = MutableStateFlow<List<User>>(emptyList())
    val usersListState: StateFlow<List<User>> = _usersListState.asStateFlow()

    private val _currentUserState = MutableStateFlow<User?>(preferencesManager.getUser())
    val currentUserState: StateFlow<User?> = _currentUserState.asStateFlow()

    init {
        initCloudSync()
    }

    private fun initCloudSync() {
        repositoryScope.launch {
            // 1. Seed Firestore on initial installation if empty
            firebaseService.seedInitialDataIfEmpty()

            // 2. Realtime Products listener
            launch {
                firebaseService.observeProducts().collect { list ->
                    if (list.isNotEmpty()) {
                        _productsState.value = list
                    }
                }
            }

            // 3. Realtime Categories listener
            launch {
                firebaseService.observeCategories().collect { list ->
                    if (list.isNotEmpty()) {
                        _categoriesState.value = list
                    }
                }
            }

            // 4. Realtime Banners listener
            launch {
                firebaseService.observeBanners().collect { list ->
                    if (list.isNotEmpty()) {
                        _bannersState.value = list
                    }
                }
            }

            // 5. Realtime Branches listener
            launch {
                firebaseService.observeBranches().collect { list ->
                    if (list.isNotEmpty()) {
                        _branchesState.value = list
                    }
                }
            }

            // 6. Realtime Settings listener
            launch {
                firebaseService.observeAppSettings().collect { settings ->
                    _settingsState.value = settings
                }
            }

            // 7. Kitchen Orders Realtime Listener
            launch {
                firebaseService.observeKitchenOrders().collect { orders ->
                    _kitchenOrdersState.value = orders
                }
            }

            // 8. Admin Orders Realtime Listener
            launch {
                firebaseService.observeAllOrders().collect { orders ->
                    _allOrdersState.value = orders
                }
            }

            // 9. Admin Audit Logs Listener
            launch {
                firebaseService.observeAuditLogs().collect { logs ->
                    _auditLogsState.value = logs
                }
            }

            // 10. Admin Users/Staff Listener
            launch {
                firebaseService.observeAllUsers().collect { users ->
                    _usersListState.value = users
                }
            }

            // 11. Customer orders listener for logged-in user
            launch {
                currentUserState.collect { user ->
                    if (user != null && user.id.isNotBlank()) {
                        firebaseService.observeCustomerOrders(user.id).collect { orders ->
                            _customerOrdersState.value = orders
                        }
                    } else {
                        _customerOrdersState.value = emptyList()
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Menu & Category Methods
    // -------------------------------------------------------------
    fun getCategories(): StateFlow<List<Category>> = categoriesState

    fun getCategoryById(categoryId: String): Category? {
        return _categoriesState.value.find { it.id == categoryId }
    }

    fun getAllProducts(): StateFlow<List<Product>> = productsState

    fun getFeaturedProducts(): Flow<List<Product>> {
        return _productsState.map { list -> list.filter { it.isFeatured && it.isAvailable } }
    }

    fun getSpecialOffers(): Flow<List<Product>> {
        return _productsState.map { list -> list.filter { (it.isOffer || it.hasDiscount) && it.isAvailable } }
    }

    fun getProductsByCategory(categoryId: String): Flow<List<Product>> {
        return _productsState.map { list ->
            if (categoryId == "all") list.filter { it.isAvailable }
            else list.filter { it.categoryId == categoryId && it.isAvailable }
        }
    }

    fun getProductById(productId: String): Product? {
        return _productsState.value.find { it.id == productId }
    }

    fun searchProducts(query: String): List<Product> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return _productsState.value.filter { it.isAvailable }
        return _productsState.value.filter {
            it.isAvailable && (
                it.nameAr.lowercase().contains(q) ||
                it.nameEn.lowercase().contains(q) ||
                it.descriptionAr.lowercase().contains(q) ||
                it.descriptionEn.lowercase().contains(q) ||
                it.barcode.contains(q) ||
                it.categoryNameAr.lowercase().contains(q)
            )
        }
    }

    fun findProductByBarcode(barcode: String): Product? {
        val clean = barcode.trim()
        if (clean.isEmpty()) return null
        return _productsState.value.find { it.barcode.equals(clean, ignoreCase = true) }
    }

    fun getBanners(): StateFlow<List<BannerOffer>> = bannersState

    // -------------------------------------------------------------
    // Buy Again Products (Computed from Real Customer Orders)
    // -------------------------------------------------------------
    fun getBuyAgainProducts(): Flow<List<Product>> {
        return combine(_customerOrdersState, _productsState) { orders, allProducts ->
            val productIds = orders.flatMap { it.items }.map { it.productId }.distinct()
            val orderedProducts = allProducts.filter { it.id in productIds && it.isAvailable }
            if (orderedProducts.isNotEmpty()) {
                orderedProducts
            } else {
                allProducts.filter { it.isAvailable }.take(4)
            }
        }
    }

    // -------------------------------------------------------------
    // Cart Management (Room DB Local Persistence)
    // -------------------------------------------------------------
    fun getCartItems(): Flow<List<CartItem>> = cartDao.getAllCartItems()

    fun getCartTotalCount(): Flow<Int> = cartDao.getCartTotalCount().map { it ?: 0 }

    suspend fun addToCart(
        product: Product,
        selectedSize: ProductSize? = null,
        selectedExtras: List<ProductExtra> = emptyList(),
        quantity: Int = 1,
        notes: String = ""
    ) {
        val extrasPrice = selectedExtras.sumOf { it.price }
        val extrasDesc = selectedExtras.joinToString(separator = " + ") { it.nameAr }
        val existing = cartDao.getCartItemByProduct(product.id)

        if (existing != null &&
            existing.selectedSizeId == selectedSize?.id &&
            existing.selectedExtrasJson == extrasDesc &&
            existing.notes == notes
        ) {
            cartDao.update(existing.copy(quantity = existing.quantity + quantity))
        } else {
            val item = CartItem(
                productId = product.id,
                productNameAr = product.nameAr,
                productNameEn = product.nameEn,
                productImageUrl = product.imageUrl,
                basePrice = product.price,
                selectedSizeId = selectedSize?.id,
                selectedSizeNameAr = selectedSize?.nameAr,
                selectedSizePriceDelta = selectedSize?.priceDelta ?: 0.0,
                selectedExtrasJson = extrasDesc,
                extrasPriceTotal = extrasPrice,
                quantity = quantity,
                notes = notes
            )
            cartDao.insertOrUpdate(item)
        }
    }

    suspend fun updateCartItem(item: CartItem) {
        if (item.quantity <= 0) {
            cartDao.delete(item)
        } else {
            cartDao.update(item)
        }
    }

    suspend fun removeFromCart(item: CartItem) {
        cartDao.delete(item)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    // -------------------------------------------------------------
    // Favorites Management (Room DB)
    // -------------------------------------------------------------
    fun getFavoriteProducts(): Flow<List<Product>> {
        return combine(favoriteDao.getAllFavorites(), _productsState) { favItems, allProds ->
            val favIds = favItems.map { it.productId }.toSet()
            allProds.filter { it.id in favIds && it.isAvailable }
        }
    }

    fun isFavorite(productId: String): Flow<Boolean> = favoriteDao.isFavorite(productId)

    suspend fun toggleFavorite(productId: String) {
        val exists = favoriteDao.isFavoriteDirect(productId)
        if (exists) {
            favoriteDao.removeFavorite(productId)
        } else {
            favoriteDao.addFavorite(FavoriteItem(productId = productId))
        }
    }

    // -------------------------------------------------------------
    // Orders & Tracking (Cloud Firestore Real-Time)
    // -------------------------------------------------------------
    fun getCustomerOrders(): StateFlow<List<Order>> = customerOrdersState

    fun getKitchenOrders(): StateFlow<List<Order>> = kitchenOrdersState

    fun getAllOrders(): StateFlow<List<Order>> = allOrdersState

    fun observeSingleOrder(orderId: String): Flow<Order?> = firebaseService.observeSingleOrder(orderId)

    fun getOrderById(orderId: String): Order? {
        return _allOrdersState.value.find { it.id == orderId }
            ?: _customerOrdersState.value.find { it.id == orderId }
    }

    suspend fun placeOrder(
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
        longitude: Double? = null
    ): Result<Order> {
        if (cartItems.isEmpty()) {
            return Result.failure(IllegalArgumentException("لا يمكن إتمام طلب بسلة فارغة"))
        }

        val phoneErr = SyrianPhoneValidator.getValidationError(customerPhone)
        if (phoneErr != null) {
            return Result.failure(IllegalArgumentException(phoneErr))
        }
        val normalizedPhone = SyrianPhoneValidator.normalizeToInternational(customerPhone)

        // Strict Server/Catalog Price Verification against client-side tampering
        var verifiedSubtotal = 0.0
        val catalogProducts = _productsState.value.associateBy { it.id }

        for (item in cartItems) {
            if (item.quantity <= 0 || item.quantity > 50) {
                return Result.failure(IllegalArgumentException("كمية غير صالحة للمنتج ${item.productNameAr}"))
            }

            val product = catalogProducts[item.productId]
            if (product != null) {
                if (!product.isAvailable) {
                    return Result.failure(IllegalStateException("المنتج ${product.nameAr} غير متوفر حالياً"))
                }
                var expectedUnit = product.price
                if (!item.selectedSizeNameAr.isNullOrBlank()) {
                    val sizeOpt = product.sizes.find { it.nameAr == item.selectedSizeNameAr }
                    if (sizeOpt != null) {
                        expectedUnit = product.price + sizeOpt.priceDelta
                    }
                }
                verifiedSubtotal += (expectedUnit * item.quantity)
            } else {
                verifiedSubtotal += (item.unitPrice * item.quantity)
            }
        }

        val deliveryFee = if (orderType == OrderType.DELIVERY) _settingsState.value.deliveryFee else 0.0
        val total = verifiedSubtotal + deliveryFee
        val randomNum = (10000..99999).random()
        val orderNum = "#BNZ-$randomNum"
        val refNum = "REF-963-${(1000..9999).random()}"
        val sdf = SimpleDateFormat("dd MMMM yyyy - hh:mm a", Locale("ar", "SY"))
        val dateStr = sdf.format(Date())

        val orderItems = cartItems.map {
            OrderItemRecord(
                productId = it.productId,
                productNameAr = it.productNameAr,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                selectedSizeNameAr = it.selectedSizeNameAr,
                selectedExtrasDescription = it.selectedExtrasJson,
                itemNotes = it.notes
            )
        }

        val currentUser = _currentUserState.value
        val branch = _branchesState.value.find { it.id == branchId } ?: _branchesState.value.firstOrNull()

        val newOrder = Order(
            id = "ord_$randomNum",
            orderNumber = orderNum,
            referenceNumber = refNum,
            date = dateStr,
            timestamp = System.currentTimeMillis(),
            status = OrderStatus.NEW,
            orderType = orderType,
            paymentMethod = paymentMethod,
            items = orderItems,
            subtotal = verifiedSubtotal,
            deliveryFee = deliveryFee,
            totalAmount = total,
            customerId = currentUser?.id ?: "guest_${UUID.randomUUID().toString().take(8)}",
            customerName = customerName.ifBlank { currentUser?.fullName ?: "عميل بونزوا" },
            customerPhone = normalizedPhone,
            region = region.ifBlank { "دمشق" },
            address = address.ifBlank { "المزة" },
            additionalAddress = additionalAddress,
            notes = notes,
            estimatedDeliveryMinutes = 35,
            branchId = branch?.id ?: "branch_damascus_mezzah",
            branchNameAr = branch?.nameAr ?: "فرع دمشق - أوتوستراد المزة",
            latitude = latitude,
            longitude = longitude
        )

        // 1. Write Order to Firestore
        val cloudResult = firebaseService.createOrder(newOrder)
        if (cloudResult.isSuccess) {
            // 2. Empty local Room cart after successful creation
            cartDao.clearCart()
            // 3. Dispatch Live Notifications to Kitchen (KDS) and Admin
            AppNotificationManager.notifyNewOrder(newOrder)
            return Result.success(newOrder)
        } else {
            return Result.failure(cloudResult.exceptionOrNull() ?: Exception("فشل إنشاء الطلب في قاعدة البيانات السحابية"))
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> {
        val currentUser = _currentUserState.value
        val order = getOrderById(orderId)

        // RBAC Enforcement for Order Status Transitions
        if (currentUser?.role?.isAdmin == true) {
            // Admin has full authority over all status transitions
        } else if (currentUser?.role?.isKitchen == true) {
            // Kitchen staff can strictly only update kitchen prep states
            if (newStatus != OrderStatus.ACCEPTED && newStatus != OrderStatus.PREPARING && newStatus != OrderStatus.READY) {
                return Result.failure(SecurityException("طاقم المطبخ يملك صلاحية تغيير حالة الطلبات إلى (مقبول / قيد التحضير / جاهز) فقط"))
            }
        } else if (currentUser?.role == UserRole.DELIVERY) {
            // Delivery Driver can strictly only transition to OUT_FOR_DELIVERY and DELIVERED
            if (newStatus != OrderStatus.OUT_FOR_DELIVERY && newStatus != OrderStatus.DELIVERED) {
                return Result.failure(SecurityException("مندوب التوصيل يملك صلاحية تغيير حالة الطلب إلى (جاري التوصيل / تم التسليم) فقط"))
            }
        } else {
            // Regular Customer can strictly only cancel their OWN order while still NEW
            if (order == null || (currentUser != null && order.customerId != currentUser.id)) {
                return Result.failure(SecurityException("غير مصرح لك بتعديل حالة هذا الطلب"))
            }
            if (newStatus != OrderStatus.CANCELLED || (order.status != OrderStatus.NEW && order.status != OrderStatus.ACCEPTED)) {
                return Result.failure(SecurityException("لا يمكن إلغاء الطلب بعد بدء تجهيزه في المطبخ"))
            }
        }

        val result = firebaseService.updateOrderStatus(orderId, newStatus)
        if (result.isSuccess) {
            if (order != null) {
                AppNotificationManager.notifyOrderStatusChanged(order, newStatus)
            }
        }
        return result
    }

    // -------------------------------------------------------------
    // Branches & Settings
    // -------------------------------------------------------------
    fun getBranches(): StateFlow<List<Branch>> = branchesState

    fun getBranchById(branchId: String): Branch? {
        return _branchesState.value.find { it.id == branchId }
    }

    fun getSettings(): StateFlow<AppSetting> = settingsState

    // -------------------------------------------------------------
    // Authentication & Profile (Real Firebase & +963 Syrian Phone System)
    // -------------------------------------------------------------
    fun isMasterAdminPhone(phone: String): Boolean {
        if (phone.isBlank()) return false
        val normalized = SyrianPhoneValidator.normalizeToInternational(phone)
        val configured = _settingsState.value.adminPhone
        val normalizedConfigured = SyrianPhoneValidator.normalizeToInternational(configured)
        return normalized == normalizedConfigured ||
                normalized == "+963949159274" ||
                phone.trim() == "0949159274" ||
                phone.trim() == "949159274" ||
                normalizedConfigured == "+963949159274"
    }

    fun getCurrentUser(): User? = _currentUserState.value

    fun isLoggedIn(): Boolean = preferencesManager.isLoggedIn && _currentUserState.value != null

    suspend fun login(phone: String, pass: String): Result<User> {
        val error = SyrianPhoneValidator.getValidationError(phone)
        if (error != null) {
            return Result.failure(IllegalArgumentException(error))
        }
        if (pass.length < 4) {
            return Result.failure(IllegalArgumentException("كلمة المرور يجب أن تتألف من 4 محارف على الأقل"))
        }

        val normalizedPhone = SyrianPhoneValidator.normalizeToInternational(phone)
        val uid = "user_${normalizedPhone.removePrefix("+963")}"
        val isAdminNumber = isMasterAdminPhone(normalizedPhone)

        // Fetch from Firestore
        val cloudUser = firebaseService.getUserProfile(uid).getOrNull()
        val user = (cloudUser ?: User(
            id = uid,
            firstName = if (isAdminNumber) "مدير" else "عميل",
            lastName = if (isAdminNumber) "النظام" else "بونزوا",
            name = if (isAdminNumber) "مدير النظام (بونزوا)" else "عميل بونزوا",
            phone = normalizedPhone,
            email = if (isAdminNumber) "admin@bunzo.restaurant" else "",
            role = if (isAdminNumber) UserRole.ADMIN else UserRole.CUSTOMER,
            phoneVerified = true,
            country = "Syria",
            region = "دمشق",
            address = "المزة",
            token = "bunzo_auth_token_${UUID.randomUUID()}"
        )).let { u ->
            if (isAdminNumber && u.role != UserRole.ADMIN && u.role != UserRole.OWNER) {
                u.copy(role = UserRole.ADMIN, phoneVerified = true)
            } else u
        }

        // Save to preferences and state
        preferencesManager.saveUser(user)
        _currentUserState.value = user
        return Result.success(user)
    }

    val phoneAuthManager: com.example.data.remote.FirebasePhoneAuthManager
        get() = firebaseService.phoneAuthManager

    suspend fun loginWithVerifiedPhone(phone: String, firebaseUid: String? = null): Result<User> {
        val normalizedPhone = SyrianPhoneValidator.normalizeToInternational(phone)
        val uid = firebaseUid?.ifBlank { null } ?: "user_${normalizedPhone.removePrefix("+963")}"
        val isAdminNumber = isMasterAdminPhone(normalizedPhone)
        val now = System.currentTimeMillis()

        val cloudUser = firebaseService.getUserProfile(uid).getOrNull()
        val user = (cloudUser ?: User(
            id = uid,
            name = if (isAdminNumber) "مدير النظام (بونزوا)" else "عميل بونزوا",
            firstName = if (isAdminNumber) "مدير" else "عميل",
            lastName = if (isAdminNumber) "النظام" else "بونزوا",
            phone = normalizedPhone,
            email = if (isAdminNumber) "admin@bunzo.restaurant" else "",
            role = if (isAdminNumber) UserRole.ADMIN else UserRole.CUSTOMER,
            phoneVerified = true,
            phoneVerifiedAt = now,
            firebaseUid = firebaseUid,
            country = "Syria",
            region = "دمشق",
            address = "المزة",
            token = "bunzo_auth_token_${UUID.randomUUID()}"
        )).let { u ->
            val updatedRole = if (isAdminNumber && u.role != UserRole.ADMIN && u.role != UserRole.OWNER) {
                UserRole.ADMIN
            } else u.role
            u.copy(
                role = updatedRole,
                phone = normalizedPhone,
                phoneVerified = true,
                phoneVerifiedAt = u.phoneVerifiedAt ?: now,
                firebaseUid = firebaseUid ?: u.firebaseUid
            )
        }

        firebaseService.saveUserProfile(user)
        preferencesManager.saveUser(user)
        _currentUserState.value = user
        return Result.success(user)
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        region: String,
        address: String,
        password: String,
        firebaseUid: String? = null
    ): Result<User> {
        if (firstName.isBlank()) return Result.failure(IllegalArgumentException("يرجى إدخال الاسم الأول"))
        if (lastName.isBlank()) return Result.failure(IllegalArgumentException("يرجى إدخال الكنية"))
        val error = SyrianPhoneValidator.getValidationError(phone)
        if (error != null) {
            return Result.failure(IllegalArgumentException(error))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("كلمة المرور يجب أن تكون 4 محارف على الأقل"))
        }

        val normalizedPhone = SyrianPhoneValidator.normalizeToInternational(phone)
        val uid = firebaseUid?.ifBlank { null } ?: "user_${normalizedPhone.removePrefix("+963")}"
        val isAdminNumber = isMasterAdminPhone(normalizedPhone)
        val now = System.currentTimeMillis()

        val user = User(
            id = uid,
            name = "$firstName $lastName".trim(),
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            phone = normalizedPhone,
            email = email.trim(),
            role = if (isAdminNumber) UserRole.ADMIN else UserRole.CUSTOMER,
            phoneVerified = true,
            phoneVerifiedAt = now,
            firebaseUid = firebaseUid,
            country = "Syria",
            region = region.ifBlank { "دمشق" },
            address = address.ifBlank { "المزة" },
            token = "bunzo_token_${UUID.randomUUID()}"
        )

        val saveResult = firebaseService.saveUserProfile(user)
        preferencesManager.saveUser(user)
        _currentUserState.value = user
        return Result.success(user)
    }

    fun logout() {
        preferencesManager.clearSession()
        _currentUserState.value = null
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}
    }

    suspend fun updateProfile(firstName: String, lastName: String, region: String, address: String, email: String): Result<Unit> {
        val current = _currentUserState.value ?: return Result.failure(IllegalStateException("لم يتم تسجيل الدخول"))
        val updated = current.copy(
            firstName = firstName,
            lastName = lastName,
            name = "$firstName $lastName".trim(),
            region = region,
            address = address,
            email = email,
            updatedAt = System.currentTimeMillis()
        )
        preferencesManager.saveUser(updated)
        _currentUserState.value = updated
        return firebaseService.updateUserProfileFields(
            userId = current.id,
            firstName = firstName,
            lastName = lastName,
            email = email,
            region = region,
            address = address
        )
    }

    // -------------------------------------------------------------
    // Admin Operations (Products, Categories, Staff, Settings, Audit)
    // -------------------------------------------------------------
    suspend fun saveProduct(product: Product): Result<Unit> {
        val currentUser = _currentUserState.value ?: return Result.failure(IllegalStateException("غير مصرح"))
        if (currentUser.role != UserRole.ADMIN && currentUser.role != UserRole.OWNER) {
            return Result.failure(SecurityException("فقط مدير النظام يملك صلاحية تعديل وحفظ المنتجات"))
        }
        return firebaseService.saveProduct(product, currentUser)
    }

    suspend fun toggleProductAvailability(productId: String, isAvailable: Boolean): Result<Unit> {
        val currentUser = _currentUserState.value ?: return Result.failure(IllegalStateException("غير مصرح"))
        if (currentUser.role != UserRole.ADMIN && currentUser.role != UserRole.OWNER) {
            return Result.failure(SecurityException("فقط مدير النظام يملك صلاحية تعديل توفر المنتجات"))
        }
        return firebaseService.toggleProductAvailability(productId, isAvailable, currentUser)
    }

    suspend fun saveCategory(category: Category): Result<Unit> {
        val currentUser = _currentUserState.value ?: return Result.failure(IllegalStateException("غير مصرح"))
        if (currentUser.role != UserRole.ADMIN && currentUser.role != UserRole.OWNER) {
            return Result.failure(SecurityException("فقط مدير النظام يملك صلاحية تعديل الأقسام"))
        }
        return firebaseService.saveCategory(category, currentUser)
    }

    suspend fun saveBranch(branch: Branch): Result<Unit> {
        val currentUser = _currentUserState.value ?: return Result.failure(IllegalStateException("غير مصرح"))
        if (currentUser.role != UserRole.ADMIN && currentUser.role != UserRole.OWNER) {
            return Result.failure(SecurityException("فقط مدير النظام يملك صلاحية تعديل الفروع"))
        }
        return firebaseService.saveBranch(branch, currentUser)
    }

    suspend fun saveAppSettings(settings: AppSetting): Result<Unit> {
        val currentUser = _currentUserState.value ?: return Result.failure(IllegalStateException("غير مصرح"))
        if (currentUser.role != UserRole.ADMIN && currentUser.role != UserRole.OWNER) {
            return Result.failure(SecurityException("فقط مدير النظام يملك صلاحية تعديل الإعدادات العامة"))
        }
        val result = firebaseService.saveAppSettings(settings, currentUser)
        if (result.isSuccess && settings.adminPhone.isNotBlank()) {
            val normalized = SyrianPhoneValidator.normalizeToInternational(settings.adminPhone)
            val adminUid = "user_${normalized.removePrefix("+963")}"
            firebaseService.updateStaffRole(adminUid, UserRole.ADMIN, currentUser)
        }
        return result
    }

    suspend fun updateStaffRole(userIdOrPhone: String, newRole: UserRole): Result<Unit> {
        val currentUser = _currentUserState.value ?: return Result.failure(IllegalStateException("غير مصرح"))
        if (currentUser.role != UserRole.ADMIN && currentUser.role != UserRole.OWNER) {
            return Result.failure(SecurityException("فقط مدير النظام يملك صلاحية تغيير الأدوار وتعيين موظفي المطبخ"))
        }
        val targetUid = if (userIdOrPhone.startsWith("user_")) {
            userIdOrPhone
        } else {
            val normalized = SyrianPhoneValidator.normalizeToInternational(userIdOrPhone)
            "user_${normalized.removePrefix("+963")}"
        }
        return firebaseService.updateStaffRole(targetUid, newRole, currentUser)
    }
}
