package com.example.data.remote

import android.util.Log
import com.example.data.model.AppSetting
import com.example.data.model.AuditLog
import com.example.data.model.BannerOffer
import com.example.data.model.Branch
import com.example.data.model.Category
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.User
import com.example.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Enterprise Firebase Service for Bunzo Restaurant.
 * Manages Cloud Firestore, Firebase Authentication, and Realtime Listeners safely.
 */
class FirebaseService {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("FirebaseService", "FirebaseAuth unavailable: ${e.message}")
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
            db
        } catch (e: Exception) {
            Log.w("FirebaseService", "Firestore unavailable: ${e.message}")
            null
        }
    }

    companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_PRODUCTS = "products"
        const val COLLECTION_CATEGORIES = "categories"
        const val COLLECTION_BRANCHES = "branches"
        const val COLLECTION_ORDERS = "orders"
        const val COLLECTION_SETTINGS = "settings"
        const val COLLECTION_AUDIT_LOGS = "auditLogs"
        const val COLLECTION_BANNERS = "banners"
        const val DOC_APP_SETTINGS = "app_settings"
    }

    val currentAuthUid: String?
        get() = try {
            auth?.currentUser?.uid
        } catch (_: Exception) {
            null
        }

    // -------------------------------------------------------------
    // Authentication & User Profile Management
    // -------------------------------------------------------------

    suspend fun saveUserProfile(user: User): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COLLECTION_USERS)
                .document(user.id)
                .set(user.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): Result<User?> {
        val db = firestore ?: return Result.success(null)
        return try {
            val snapshot = db.collection(COLLECTION_USERS)
                .document(uid)
                .get()
                .await()
            if (snapshot.exists()) {
                Result.success(User.fromFirestore(snapshot.id, snapshot.data))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUserProfile(uid: String): Flow<User?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_USERS)
                .document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        trySend(User.fromFirestore(snapshot.id, snapshot.data))
                    } else {
                        trySend(null)
                    }
                }
        } catch (_: Exception) {
            trySend(null)
        }
        awaitClose { listener?.remove() }
    }

    suspend fun updateUserProfileFields(
        userId: String,
        firstName: String,
        lastName: String,
        email: String,
        region: String,
        address: String
    ): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val name = "$firstName $lastName".trim()
            db.collection(COLLECTION_USERS)
                .document(userId)
                .update(
                    mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "name" to name,
                        "email" to email,
                        "region" to region,
                        "address" to address,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    val phoneAuthManager: FirebasePhoneAuthManager by lazy {
        FirebasePhoneAuthManager(auth ?: FirebaseAuth.getInstance())
    }

    suspend fun updateUserPhoneVerification(
        userId: String,
        phoneNumber: String,
        firebaseUid: String
    ): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val now = System.currentTimeMillis()
            db.collection(COLLECTION_USERS)
                .document(userId)
                .update(
                    mapOf(
                        "phone" to phoneNumber,
                        "phoneVerified" to true,
                        "phoneVerifiedAt" to now,
                        "firebaseUid" to firebaseUid,
                        "updatedAt" to now
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStaffRole(userId: String, newRole: UserRole, adminUser: User): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COLLECTION_USERS)
                .document(userId)
                .update(
                    mapOf(
                        "role" to newRole.roleKey,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()

            // Record Audit Log
            recordAuditLog(
                AuditLog(
                    id = "audit_${System.currentTimeMillis()}",
                    actorId = adminUser.id,
                    actorName = adminUser.fullName,
                    actorRole = adminUser.role,
                    action = "UPDATE_ROLE",
                    targetType = "USER",
                    targetId = userId,
                    description = "تم تغيير صلاحية المستخدم إلى ${newRole.labelAr}"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // Realtime Products & Menu Flows
    // -------------------------------------------------------------

    fun observeProducts(): Flow<List<Product>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(BunzoDataSource.products)
            awaitClose { }
            return@callbackFlow
        }
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_PRODUCTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        return@addSnapshotListener
                    }
                    val products = snapshot.documents.map { Product.fromFirestore(it.id, it.data) }
                    trySend(products)
                }
        } catch (_: Exception) {}
        awaitClose { listener?.remove() }
    }

    fun observeCategories(): Flow<List<Category>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(BunzoDataSource.categories)
            awaitClose { }
            return@callbackFlow
        }
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_CATEGORIES)
                .orderBy("sortOrder", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        return@addSnapshotListener
                    }
                    val categories = snapshot.documents.map { Category.fromFirestore(it.id, it.data) }
                    trySend(categories)
                }
        } catch (_: Exception) {}
        awaitClose { listener?.remove() }
    }

    fun observeBanners(): Flow<List<BannerOffer>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(BunzoDataSource.bannerOffers)
            awaitClose { }
            return@callbackFlow
        }
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_BANNERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        return@addSnapshotListener
                    }
                    val banners = snapshot.documents.map { BannerOffer.fromFirestore(it.id, it.data) }
                    trySend(banners)
                }
        } catch (_: Exception) {}
        awaitClose { listener?.remove() }
    }

    fun observeBranches(): Flow<List<Branch>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(BunzoDataSource.branches)
            awaitClose { }
            return@callbackFlow
        }
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_BRANCHES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        return@addSnapshotListener
                    }
                    val branches = snapshot.documents.map { Branch.fromFirestore(it.id, it.data) }
                    trySend(branches)
                }
        } catch (_: Exception) {}
        awaitClose { listener?.remove() }
    }

    fun observeAppSettings(): Flow<AppSetting> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(BunzoDataSource.appSettings)
            awaitClose { }
            return@callbackFlow
        }
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_SETTINGS)
                .document(DOC_APP_SETTINGS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        return@addSnapshotListener
                    }
                    if (snapshot.exists()) {
                        trySend(AppSetting.fromFirestore(snapshot.data))
                    }
                }
        } catch (_: Exception) {}
        awaitClose { listener?.remove() }
    }

    // -------------------------------------------------------------
    // Realtime Orders (Customer, Kitchen, and Admin scopes)
    // -------------------------------------------------------------

    fun observeCustomerOrders(customerId: String): Flow<List<Order>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_ORDERS)
                .whereEqualTo("customerId", customerId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        return@addSnapshotListener
                    }
                    val orders = snapshot.documents.map { Order.fromFirestore(it.id, it.data) }
                    trySend(orders)
                }
        } catch (_: Exception) {}
        awaitClose { listener?.remove() }
    }

    fun observeKitchenOrders(): Flow<List<Order>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_ORDERS)
                .whereIn("status", listOf("NEW", "ACCEPTED", "PREPARING", "READY"))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        return@addSnapshotListener
                    }
                    val orders = snapshot.documents.map { Order.fromFirestore(it.id, it.data) }
                    trySend(orders)
                }
        } catch (_: Exception) {}
        awaitClose { listener?.remove() }
    }

    fun observeAllOrders(): Flow<List<Order>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_ORDERS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        return@addSnapshotListener
                    }
                    val orders = snapshot.documents.map { Order.fromFirestore(it.id, it.data) }
                    trySend(orders)
                }
        } catch (_: Exception) {}
        awaitClose { listener?.remove() }
    }

    fun observeSingleOrder(orderId: String): Flow<Order?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    trySend(Order.fromFirestore(snapshot.id, snapshot.data))
                }
        } catch (_: Exception) {
            trySend(null)
        }
        awaitClose { listener?.remove() }
    }

    suspend fun createOrder(order: Order): Result<Order> {
        val db = firestore ?: return Result.success(order)
        return try {
            db.collection(COLLECTION_ORDERS)
                .document(order.id)
                .set(order.toFirestoreMap())
                .await()
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus, actorId: String? = null): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .update(
                    mapOf(
                        "status" to newStatus.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // Product & Menu CRUD (Admin Operations)
    // -------------------------------------------------------------

    suspend fun saveProduct(product: Product, adminUser: User): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COLLECTION_PRODUCTS)
                .document(product.id)
                .set(product.toFirestoreMap())
                .await()

            recordAuditLog(
                AuditLog(
                    id = "audit_${System.currentTimeMillis()}",
                    actorId = adminUser.id,
                    actorName = adminUser.fullName,
                    actorRole = adminUser.role,
                    action = "SAVE_PRODUCT",
                    targetType = "PRODUCT",
                    targetId = product.id,
                    description = "تم حفظ/تعديل الوجبة: ${product.nameAr} بسعر ${product.price} ل.س"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleProductAvailability(productId: String, isAvailable: Boolean, adminUser: User): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .update("isAvailable", isAvailable)
                .await()

            recordAuditLog(
                AuditLog(
                    id = "audit_${System.currentTimeMillis()}",
                    actorId = adminUser.id,
                    actorName = adminUser.fullName,
                    actorRole = adminUser.role,
                    action = "TOGGLE_AVAILABILITY",
                    targetType = "PRODUCT",
                    targetId = productId,
                    description = "تم تغيير حالة توفر الوجبة إلى ${if (isAvailable) "متوفر" else "غير متوفر"}"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveCategory(category: Category, adminUser: User): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COLLECTION_CATEGORIES)
                .document(category.id)
                .set(category.toFirestoreMap())
                .await()

            recordAuditLog(
                AuditLog(
                    id = "audit_${System.currentTimeMillis()}",
                    actorId = adminUser.id,
                    actorName = adminUser.fullName,
                    actorRole = adminUser.role,
                    action = "SAVE_CATEGORY",
                    targetType = "CATEGORY",
                    targetId = category.id,
                    description = "تم حفظ القسم: ${category.nameAr}"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveBranch(branch: Branch, adminUser: User): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COLLECTION_BRANCHES)
                .document(branch.id)
                .set(branch.toFirestoreMap())
                .await()

            recordAuditLog(
                AuditLog(
                    id = "audit_${System.currentTimeMillis()}",
                    actorId = adminUser.id,
                    actorName = adminUser.fullName,
                    actorRole = adminUser.role,
                    action = "SAVE_BRANCH",
                    targetType = "BRANCH",
                    targetId = branch.id,
                    description = "تم تحديث بيانات الفرع: ${branch.nameAr}"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveAppSettings(settings: AppSetting, adminUser: User): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COLLECTION_SETTINGS)
                .document(DOC_APP_SETTINGS)
                .set(settings.toFirestoreMap())
                .await()

            recordAuditLog(
                AuditLog(
                    id = "audit_${System.currentTimeMillis()}",
                    actorId = adminUser.id,
                    actorName = adminUser.fullName,
                    actorRole = adminUser.role,
                    action = "UPDATE_SETTINGS",
                    targetType = "SETTINGS",
                    targetId = DOC_APP_SETTINGS,
                    description = "تم تعديل إعدادات المطعم ورسوم التوصيل (${settings.deliveryFee} ل.س)"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // Audit Logs
    // -------------------------------------------------------------

    suspend fun recordAuditLog(log: AuditLog): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COLLECTION_AUDIT_LOGS)
                .document(log.id)
                .set(log.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAuditLogs(): Flow<List<AuditLog>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_AUDIT_LOGS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        return@addSnapshotListener
                    }
                    val logs = snapshot.documents.map { AuditLog.fromFirestore(it.id, it.data) }
                    trySend(logs)
                }
        } catch (_: Exception) {}
        awaitClose { listener?.remove() }
    }

    fun observeAllUsers(): Flow<List<User>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_USERS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        return@addSnapshotListener
                    }
                    val users = snapshot.documents.map { User.fromFirestore(it.id, it.data) }
                    trySend(users)
                }
        } catch (_: Exception) {}
        awaitClose { listener?.remove() }
    }

    // -------------------------------------------------------------
    // Seed Firestore on First Cloud Initialization
    // -------------------------------------------------------------
    suspend fun seedInitialDataIfEmpty() {
        val db = firestore ?: return
        try {
            val prodsSnapshot = db.collection(COLLECTION_PRODUCTS).limit(1).get().await()
            if (prodsSnapshot.isEmpty) {
                for (cat in BunzoDataSource.categories) {
                    db.collection(COLLECTION_CATEGORIES).document(cat.id).set(cat.toFirestoreMap()).await()
                }
                for (prod in BunzoDataSource.products) {
                    db.collection(COLLECTION_PRODUCTS).document(prod.id).set(prod.toFirestoreMap()).await()
                }
                for (br in BunzoDataSource.branches) {
                    db.collection(COLLECTION_BRANCHES).document(br.id).set(br.toFirestoreMap()).await()
                }
                for (ban in BunzoDataSource.bannerOffers) {
                    db.collection(COLLECTION_BANNERS).document(ban.id).set(ban.toFirestoreMap()).await()
                }
                db.collection(COLLECTION_SETTINGS).document(DOC_APP_SETTINGS).set(BunzoDataSource.appSettings.toFirestoreMap()).await()
                Log.d("FirebaseService", "Initial Firestore dataset seeded successfully.")
            }
        } catch (e: Exception) {
            Log.w("FirebaseService", "Seed check skipped or offline: ${e.message}")
        }
    }
}
