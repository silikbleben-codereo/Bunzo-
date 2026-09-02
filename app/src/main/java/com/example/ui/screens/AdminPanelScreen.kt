package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLog
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.components.AccessDeniedView
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoError
import com.example.ui.theme.BunzoPrimary
import com.example.ui.theme.BunzoSuccess
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.utils.PriceFormatter
import com.example.utils.SyrianPhoneValidator

@Composable
fun AdminPanelScreen(
    adminViewModel: AdminViewModel,
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    // Strict RBAC Guard: Block access if user is not ADMIN or OWNER
    if (currentUser == null || !currentUser!!.role.isAdmin) {
        AccessDeniedView(
            title = "وصول مقيّد: منطقة الإدارة العامة",
            message = "عذراً، هذه اللوحة مخصصة حصرياً لمدير النظام المصرح له. تم حظر محاولة الوصول المباشر.",
            onBackClick = onNavigateBack,
            modifier = modifier
        )
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("نظرة عامة 📊", "الطلبات الحية 📋", "قائمة المنيو 🍔", "الموظفين والصلاحيات 👥", "سجل التغييرات 📜")

    val orders by adminViewModel.allOrders.collectAsState()
    val products by mainViewModel.allProducts.collectAsState()
    val auditLogs by adminViewModel.auditLogs.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("admin_panel_screen")
    ) {
        // Admin Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BunzoPrimary)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BunzoAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = BunzoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "مركز إدارة مطعم بونزوا",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "لوحة التحكم السحابية والعمليات المباشرة",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Tabs Row
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BunzoPrimary,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> AdminOverviewTab(orders, products)
            1 -> AdminOrdersTab(orders, adminViewModel)
            2 -> AdminProductsTab(products, adminViewModel)
            3 -> AdminStaffTab(adminViewModel)
            4 -> AdminAuditLogsTab(auditLogs)
        }
    }
}

@Composable
private fun AdminOverviewTab(
    orders: List<Order>,
    products: List<Product>
) {
    val totalRevenue = orders.filter { it.status == OrderStatus.DELIVERED }.sumOf { it.totalAmount }
    val activeOrdersCount = orders.count { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }
    val completedCount = orders.count { it.status == OrderStatus.DELIVERED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "مؤشرات الأداء والإيرادات 📈",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "إجمالي المبيعات",
                value = PriceFormatter.formatPrice(totalRevenue),
                icon = Icons.Default.AttachMoney,
                tint = BunzoSuccess,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "الطلبات النشطة",
                value = "$activeOrdersCount",
                icon = Icons.Default.Receipt,
                tint = BunzoAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "الطلبات المكتملة",
                value = "$completedCount طلب",
                icon = Icons.Default.CheckCircle,
                tint = BunzoPrimary,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "إجمالي الوجبات",
                value = "${products.size} وجبة",
                icon = Icons.Default.RestaurantMenu,
                tint = Color(0xFF673AB7),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "توزيع الطلبات حسب الحالة الحالية:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        OrderStatus.values().forEach { status ->
            val count = orders.count { it.status == status }
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(status.labelAr, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BunzoPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("$count طلب", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BunzoPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontWeight = FontWeight.Black, fontSize = 15.sp)
        }
    }
}

@Composable
private fun AdminOrdersTab(
    orders: List<Order>,
    adminViewModel: AdminViewModel
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val displayedOrders = when (selectedFilter) {
        "ACTIVE" -> orders.filter { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }
        "DELIVERED" -> orders.filter { it.status == OrderStatus.DELIVERED }
        "CANCELLED" -> orders.filter { it.status == OrderStatus.CANCELLED }
        else -> orders
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filterOptions = listOf("ALL" to "الكل", "ACTIVE" to "النشطة", "DELIVERED" to "المكتملة", "CANCELLED" to "الملغاة")
            items(filterOptions) { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BunzoPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayedOrders, key = { it.id }) { order ->
                AdminOrderCard(
                    order = order,
                    onUpdateStatus = { nextStatus -> adminViewModel.updateOrderStatus(order.id, nextStatus) },
                    onCancelOrder = { adminViewModel.cancelOrder(order.id) }
                )
            }
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: Order,
    onUpdateStatus: (OrderStatus) -> Unit,
    onCancelOrder: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BunzoPrimary)
                Text(order.status.labelAr, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BunzoAccent)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text("العميل: ${order.customerName} (${order.customerPhone})", fontSize = 12.sp)
            Text("العنوان: ${order.region} - ${order.address}", fontSize = 11.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach { item ->
                Text("• ${item.quantity}x ${item.productNameAr} (${PriceFormatter.formatPrice(item.totalPrice)})", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("المجموع: ${PriceFormatter.formatPrice(order.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
                        val nextStatus = when (order.status) {
                            OrderStatus.NEW -> OrderStatus.ACCEPTED
                            OrderStatus.ACCEPTED -> OrderStatus.PREPARING
                            OrderStatus.PREPARING -> OrderStatus.READY
                            OrderStatus.READY -> OrderStatus.OUT_FOR_DELIVERY
                            OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.DELIVERED
                            else -> null
                        }
                        if (nextStatus != null) {
                            Button(
                                onClick = { onUpdateStatus(nextStatus) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BunzoPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("نقل إلى: ${nextStatus.labelAr}", fontSize = 11.sp)
                            }
                        }

                        IconButton(
                            onClick = onCancelOrder,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = "إلغاء", tint = BunzoError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminProductsTab(
    products: List<Product>,
    adminViewModel: AdminViewModel
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("التحكم بتوفر الوجبات في المنيو 🍔", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(products, key = { it.id }) { product ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.nameAr, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${product.categoryNameAr} - ${PriceFormatter.formatPrice(product.price)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (product.isAvailable) "متوفر" else "نفذت الكمية",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (product.isAvailable) BunzoSuccess else BunzoError
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = product.isAvailable,
                            onCheckedChange = { available ->
                                adminViewModel.toggleProductAvailability(product.id, available)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BunzoSuccess
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStaffTab(
    adminViewModel: AdminViewModel
) {
    val settings by adminViewModel.settings.collectAsState()
    val usersList by adminViewModel.usersList.collectAsState()

    var masterAdminPhoneInput by remember(settings.adminPhone) {
        val displayPhone = if (settings.adminPhone.isNotBlank()) {
            SyrianPhoneValidator.toLocalDisplayFormat(settings.adminPhone)
        } else "0949159274"
        mutableStateOf(displayPhone)
    }
    var masterAdminStatusMessage by remember { mutableStateOf<String?>(null) }

    var phoneInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.KITCHEN) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Master Admin Phone Setting (with ability to change anytime)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BunzoPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = BunzoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("رقم هاتف الإدارة الرئيسي (Master Admin) 👑", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "حساب الإدارة الأساسي ذو الصلاحيات الكاملة على النظام",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider()

                    Text(
                        text = "الرقم المسجل حالياً كإدارة عامة: ${settings.adminPhone.ifBlank { "0949159274 (+963949159274)" }}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = BunzoPrimary
                    )

                    OutlinedTextField(
                        value = masterAdminPhoneInput,
                        onValueChange = { masterAdminPhoneInput = it },
                        label = { Text("تعديل رقم الإدارة الجديد") },
                        placeholder = { Text("0949159274") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val error = SyrianPhoneValidator.getValidationError(masterAdminPhoneInput)
                            if (error != null) {
                                masterAdminStatusMessage = "⚠️ $error"
                            } else {
                                val normalized = SyrianPhoneValidator.normalizeToInternational(masterAdminPhoneInput)
                                adminViewModel.saveAppSettings(
                                    settings.copy(
                                        adminPhone = normalized,
                                        defaultPhone = normalized,
                                        whatsappNumber = normalized
                                    )
                                )
                                masterAdminStatusMessage = "✅ تم تحديث رقم الإدارة الرئيسي إلى $normalized وتعيين كامل الصلاحيات له بنجاح!"
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BunzoPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ وتحديث رقم الإدارة الرئيسي", fontWeight = FontWeight.Bold)
                    }

                    if (masterAdminStatusMessage != null) {
                        Text(
                            text = masterAdminStatusMessage ?: "",
                            color = if (masterAdminStatusMessage?.startsWith("⚠️") == true) BunzoError else BunzoSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Section 2: Promote Staff and Assign Roles
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("ترقية وتعيين صلاحيات الموظفين 👥", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = "أدخل رقم هاتف الموظف (+963) لتعيين دوره كشيف في المطبخ، مدير نظام، أو مندوب توصيل",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("رقم موبايل الموظف") },
                        placeholder = { Text("09xxxxxxxx") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = selectedRole == UserRole.KITCHEN,
                            onClick = { selectedRole = UserRole.KITCHEN },
                            label = { Text("طاقم المطبخ (KDS)") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = selectedRole == UserRole.DELIVERY,
                            onClick = { selectedRole = UserRole.DELIVERY },
                            label = { Text("مندوب توصيل") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = selectedRole == UserRole.ADMIN,
                            onClick = { selectedRole = UserRole.ADMIN },
                            label = { Text("مدير نظام (Admin)") },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (phoneInput.isNotBlank()) {
                                adminViewModel.assignUserRole(phoneInput, selectedRole)
                                statusMessage = "تم تحديث صلاحية الحساب ($phoneInput) إلى ${selectedRole.labelAr} بنجاح!"
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BunzoAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تطبيق الصلاحية للموظف", fontWeight = FontWeight.Bold, color = BunzoPrimary)
                    }

                    if (statusMessage != null) {
                        Text(
                            text = statusMessage ?: "",
                            color = BunzoSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Section 3: Registered Users & Staff Members List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المستخدمين والكادر المسجل (${usersList.size}) 👥",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${usersList.count { it.phoneVerified }} موثق بالـ SMS ✅",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BunzoSuccess
                )
            }
        }

        items(usersList) { userItem ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (userItem.role) {
                                            UserRole.ADMIN, UserRole.OWNER -> BunzoPrimary.copy(alpha = 0.15f)
                                            UserRole.KITCHEN -> BunzoAccent.copy(alpha = 0.25f)
                                            UserRole.DELIVERY -> BunzoSuccess.copy(alpha = 0.15f)
                                            else -> Color.Gray.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (userItem.role) {
                                        UserRole.ADMIN, UserRole.OWNER -> Icons.Default.AdminPanelSettings
                                        UserRole.KITCHEN -> Icons.Default.SoupKitchen
                                        UserRole.DELIVERY -> Icons.Default.TwoWheeler
                                        else -> Icons.Default.Person
                                    },
                                    contentDescription = null,
                                    tint = when (userItem.role) {
                                        UserRole.ADMIN, UserRole.OWNER -> BunzoPrimary
                                        UserRole.KITCHEN -> BunzoPrimary
                                        UserRole.DELIVERY -> BunzoSuccess
                                        else -> Color.Gray
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(userItem.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = userItem.phone.ifBlank { "بدون رقم هاتف" },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (userItem.role) {
                                        UserRole.ADMIN, UserRole.OWNER -> BunzoPrimary
                                        UserRole.KITCHEN -> BunzoAccent
                                        UserRole.DELIVERY -> BunzoSuccess
                                        else -> Color.Gray.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = userItem.role.labelAr,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userItem.role == UserRole.KITCHEN) BunzoPrimary else if (userItem.role == UserRole.CUSTOMER) Color.DarkGray else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))

                    // Phone Verification Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (userItem.phoneVerified) {
                                Text(
                                    text = "✅ رقم الهاتف موثق عبر SMS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BunzoSuccess
                                )
                            } else {
                                Text(
                                    text = "❌ رقم الهاتف غير موثق",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BunzoError
                                )
                            }
                        }

                        if (userItem.phoneVerified && userItem.phoneVerifiedAt != null) {
                            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                            val dateFormatted = sdf.format(java.util.Date(userItem.phoneVerifiedAt))
                            Text(
                                text = "تاريخ التوثيق: $dateFormatted",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminAuditLogsTab(
    logs: List<AuditLog>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("سجل الرقابة والتعديلات المباشر (Audit Trail) 📜", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(logs) { log ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(log.action, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BunzoPrimary)
                        Text("${log.timestamp}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(log.description, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("بواسطة: ${log.actorName} (${log.actorRole.labelAr})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
