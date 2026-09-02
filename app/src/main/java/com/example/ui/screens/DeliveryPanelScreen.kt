package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.UserRole
import com.example.ui.components.AccessDeniedView
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoError
import com.example.ui.theme.BunzoPrimary
import com.example.ui.theme.BunzoSuccess
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.utils.PriceFormatter
import com.example.utils.SoundHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryPanelScreen(
    adminViewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by authViewModel.currentUser.collectAsState()

    // RBAC Guard: DELIVERY, ADMIN, OWNER, MANAGER
    val isAuthorized = currentUser != null && (
        currentUser!!.role == UserRole.DELIVERY ||
        currentUser!!.role.isAdmin ||
        currentUser!!.role == UserRole.MANAGER
    )

    if (!isAuthorized) {
        AccessDeniedView(
            title = "بوابة مندوبي التوصيل (Driver Portal)",
            message = "عذراً، هذه الشاشة مخصصة لكادر مناديب التوصيل المعتمدين. يرجى تسجيل الدخول بحساب مندوب توصيل.",
            onBackClick = onNavigateBack,
            modifier = modifier
        )
        return
    }

    val allOrders by adminViewModel.allOrders.collectAsState()
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("قيد التوصيل 🛵", "جاهزة للاستلام 📦", "تم التسليم ✅")

    val filteredOrders = remember(allOrders, selectedFilterIndex) {
        when (selectedFilterIndex) {
            0 -> allOrders.filter { it.status == OrderStatus.OUT_FOR_DELIVERY }
            1 -> allOrders.filter { it.status == OrderStatus.READY }
            else -> allOrders.filter { it.status == OrderStatus.DELIVERED }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "بوابة مناديب التوصيل 🛵",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BunzoAccent)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "سائق معتمد",
                                    color = BunzoPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Text(
                            text = "السائق: ${currentUser?.fullName ?: "كابتن بونزوا"}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "تم تحديث قائمة طلبات التوصيل", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BunzoPrimary)
            )
        },
        modifier = modifier.fillMaxSize().testTag("delivery_panel_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Tabs
            TabRow(
                selectedTabIndex = selectedFilterIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BunzoPrimary
            ) {
                filterTabs.forEachIndexed { index, title ->
                    val count = when (index) {
                        0 -> allOrders.count { it.status == OrderStatus.OUT_FOR_DELIVERY }
                        1 -> allOrders.count { it.status == OrderStatus.READY }
                        else -> allOrders.count { it.status == OrderStatus.DELIVERED }
                    }
                    Tab(
                        selected = selectedFilterIndex == index,
                        onClick = { selectedFilterIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (selectedFilterIndex == index) BunzoPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        color = if (selectedFilterIndex == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد طلبات في هذا القسم حالياً",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ستظهر الطلبات الجديدة المجهزة فور انتهاء المطبخ من تحضيرها",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        DriverOrderCard(
                            order = order,
                            context = context,
                            onStatusChange = { newStatus ->
                                adminViewModel.updateOrderStatus(order.id, newStatus)
                                SoundHelper.playStatusUpdateSound()
                                Toast.makeText(context, "تم تحديث حالة الطلب إلى: ${newStatus.labelAr}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverOrderCard(
    order: Order,
    context: Context,
    onStatusChange: (OrderStatus) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order Number & Payment Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.orderNumber,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = BunzoPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.date,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (order.status) {
                                OrderStatus.OUT_FOR_DELIVERY -> BunzoAccent.copy(alpha = 0.2f)
                                OrderStatus.READY -> BunzoPrimary.copy(alpha = 0.15f)
                                OrderStatus.DELIVERED -> BunzoSuccess.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status.labelAr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            OrderStatus.OUT_FOR_DELIVERY -> BunzoAccent
                            OrderStatus.READY -> BunzoPrimary
                            OrderStatus.DELIVERED -> BunzoSuccess
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Customer Info Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = BunzoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = order.customerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = BunzoSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = order.customerPhone,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Call Customer Action Button
                FilledTonalIconButton(
                    onClick = {
                        try {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                            context.startActivity(dialIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "تعذر فتح لوحة الاتصال", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = BunzoSuccess.copy(alpha = 0.15f),
                        contentColor = BunzoSuccess
                    )
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "اتصال بالعميل")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Address & GPS Navigation Box
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = BunzoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${order.region} - ${order.address}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (order.additionalAddress.isNotBlank()) {
                                Text(
                                    text = "علامة مميزة: ${order.additionalAddress}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (order.latitude != null && order.longitude != null) {
                                Text(
                                    text = "📍 إحداثيات GPS: (${order.latitude}, ${order.longitude})",
                                    fontSize = 10.sp,
                                    color = BunzoPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Open in Google Maps Button
                    OutlinedButton(
                        onClick = {
                            try {
                                val uri = if (order.latitude != null && order.longitude != null) {
                                    Uri.parse("geo:${order.latitude},${order.longitude}?q=${order.latitude},${order.longitude}(موقع+تسليم+بونزوا)")
                                } else {
                                    Uri.parse("geo:0,0?q=${Uri.encode("${order.region} ${order.address} دمشق سوريا")}")
                                }
                                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذر فتح تطبيق الخرائط", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = BunzoPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "فتح موقع العميل في خرائط Google",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BunzoPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items Summary
            Text(
                text = "محتويات الطلب (${order.items.sumOf { it.quantity }} وجبات):",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            order.items.forEach { item ->
                Text(
                    text = "• ${item.quantity}x ${item.productNameAr}${if (!item.selectedSizeNameAr.isNullOrBlank()) " (${item.selectedSizeNameAr})" else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Amount & Method Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BunzoAccent.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "المبلغ المطلوب تحصيله:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = PriceFormatter.formatPrice(order.totalAmount),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = BunzoPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BunzoPrimary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.paymentMethod.labelAr,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons based on order status
            when (order.status) {
                OrderStatus.READY -> {
                    Button(
                        onClick = { onStatusChange(OrderStatus.OUT_FOR_DELIVERY) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BunzoAccent),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TwoWheeler,
                                contentDescription = null,
                                tint = BunzoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "استلام الوجبة والبدء بالتوصيل 🛵",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = BunzoPrimary
                            )
                        }
                    }
                }
                OrderStatus.OUT_FOR_DELIVERY -> {
                    Button(
                        onClick = { onStatusChange(OrderStatus.DELIVERED) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BunzoSuccess),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تم التسليم للعميل واستلام المبلغ 💰✅",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                OrderStatus.DELIVERED -> {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(
                            text = "تم التوصيل والتسليم بنجاح",
                            fontWeight = FontWeight.Bold,
                            color = BunzoSuccess
                        )
                    }
                }
                else -> {}
            }
        }
    }
}
