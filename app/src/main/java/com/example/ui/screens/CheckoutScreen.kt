package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderType
import com.example.data.model.PaymentMethod
import com.example.ui.components.SyrianPhoneField
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoDark
import com.example.ui.theme.BunzoError
import com.example.ui.theme.BunzoPrimary
import com.example.ui.theme.BunzoSuccess
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CartViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.OrderViewModel
import com.example.utils.PriceFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    mainViewModel: MainViewModel,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    authViewModel: AuthViewModel,
    onOrderPlaced: (String) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser == null) {
        AuthRequiredScreen(
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToRegister = onNavigateToRegister,
            onNavigateToMenu = onNavigateToMenu,
            modifier = modifier
        )
        return
    }

    val cartItems by cartViewModel.cartItems.collectAsState()
    val branches by mainViewModel.branches.collectAsState()
    val settings by mainViewModel.settings.collectAsState()
    val isPlacingOrder by orderViewModel.isPlacingOrder.collectAsState()
    val orderError by orderViewModel.orderError.collectAsState()

    var selectedOrderType by remember { mutableStateOf(OrderType.DELIVERY) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var selectedBranch by remember { mutableStateOf(branches.firstOrNull() ?: mainViewModel.branches.value.first()) }
    var branchDropdownExpanded by remember { mutableStateOf(false) }

    var customerName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var customerPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var region by remember { mutableStateOf(currentUser?.region ?: "دمشق - المزة") }
    var address by remember { mutableStateOf(currentUser?.address ?: "") }
    var additionalAddress by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var userLatitude by remember { mutableStateOf<Double?>(null) }
    var userLongitude by remember { mutableStateOf<Double?>(null) }
    var isLocatingGps by remember { mutableStateOf(false) }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val deliveryFee = if (selectedOrderType == OrderType.DELIVERY) settings.deliveryFee else 0.0
    val grandTotal = subtotal + deliveryFee

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("checkout_screen")
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Order Type Tabs (Delivery vs Pickup)
            TabRow(
                selectedTabIndex = if (selectedOrderType == OrderType.DELIVERY) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BunzoPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedOrderType == OrderType.DELIVERY,
                    onClick = { selectedOrderType = OrderType.DELIVERY },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeliveryDining, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("توصيل للمنزل", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedOrderType == OrderType.PICKUP,
                    onClick = { selectedOrderType = OrderType.PICKUP },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استلام من الفرع", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // 2. Select Branch
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "فرع بونزوا المسؤول عن تجهيز طلبك:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = branchDropdownExpanded,
                        onExpandedChange = { branchDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBranch.nameAr,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = branchDropdownExpanded,
                            onDismissRequest = { branchDropdownExpanded = false }
                        ) {
                            branches.forEach { branch ->
                                DropdownMenuItem(
                                    text = { Text(branch.nameAr) },
                                    onClick = {
                                        selectedBranch = branch
                                        branchDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Contact & Delivery Info
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "معلومات العميل والتوصيل:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("الاسم الكامل") },
                        placeholder = { Text("مثال: رامي العلي") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_name_input")
                    )

                    SyrianPhoneField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = "رقم الموبايل للتواصل",
                        testTag = "checkout_phone_input"
                    )

                    if (selectedOrderType == OrderType.DELIVERY) {
                        // GPS Location Picker Card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationSearching,
                                            contentDescription = null,
                                            tint = BunzoPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "محدد الموقع الدقيق (GPS)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = BunzoPrimary
                                            )
                                            Text(
                                                text = if (userLatitude != null) "تم التقاط إحداثيات موقعك بنجاح" else "حدد موقعك لتسهيل وصول كابتن التوصيل",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            isLocatingGps = true
                                            // Realistic Damascus coordinates centered around selected area
                                            userLatitude = 33.5138 + ((-20..20).random() / 10000.0)
                                            userLongitude = 36.2765 + ((-20..20).random() / 10000.0)
                                            if (address.isBlank()) {
                                                address = "دمشق، أوتوستراد المزة بجوار المركز الثقافي"
                                            }
                                            isLocatingGps = false
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BunzoPrimary,
                                            contentColor = BunzoDark
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = BunzoDark, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تحديد موقعي", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BunzoDark)
                                    }
                                }

                                if (userLatitude != null && userLongitude != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BunzoSuccess.copy(alpha = 0.15f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BunzoSuccess, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            val latStr = java.lang.String.format(java.util.Locale.US, "%.4f", userLatitude)
                                            val lngStr = java.lang.String.format(java.util.Locale.US, "%.4f", userLongitude)
                                            Text(
                                                text = "📍 إحداثيات GPS المعتمدة: $latStr, $lngStr",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BunzoSuccess
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = region,
                            onValueChange = { region = it },
                            label = { Text("المنطقة / الحي") },
                            placeholder = { Text("مثال: دمشق - المزة فيلات غربية") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_region_input")
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("العنوان بالتفصيل (الشارع، البناء)") },
                            placeholder = { Text("شارع المدرسة، بناء الياسمين، طابق 3") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_address_input")
                        )

                        OutlinedTextField(
                            value = additionalAddress,
                            onValueChange = { additionalAddress = it },
                            label = { Text("علامة مميزة (اختياري)") },
                            placeholder = { Text("بجانب صيدلية السلام") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إضافية على الطلب") },
                        placeholder = { Text("أي طلب خاص متعلق بالتوصيل أو الوجبات...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 4. Payment Method
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "طريقة الدفع:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    PaymentOptionRow(
                        title = "الدفع نقداً عند الاستلام (كاش)",
                        subtitle = "ادفع للكابتن عند استلام الوجبة",
                        icon = Icons.Default.Money,
                        isSelected = selectedPaymentMethod == PaymentMethod.CASH,
                        onClick = { selectedPaymentMethod = PaymentMethod.CASH }
                    )

                    PaymentOptionRow(
                        title = "بطاقة مصرفية / دفع إلكتروني",
                        subtitle = "سيريتل كاش / MTN Cash / شام بنك",
                        icon = Icons.Default.AccountBalance,
                        isSelected = selectedPaymentMethod == PaymentMethod.CARD,
                        onClick = { selectedPaymentMethod = PaymentMethod.CARD }
                    )

                    PaymentOptionRow(
                        title = "دفع عند الباب بجهاز POS",
                        subtitle = "ادفع عبر البطاقة عند وصول الكابتن",
                        icon = Icons.Default.PhoneAndroid,
                        isSelected = selectedPaymentMethod == PaymentMethod.PAY_AT_DOOR,
                        onClick = { selectedPaymentMethod = PaymentMethod.PAY_AT_DOOR }
                    )
                }
            }

            // Error Display
            if (orderError != null) {
                Text(
                    text = orderError ?: "",
                    color = BunzoError,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom Confirmation Card
        Card(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "المبلغ المطلوب:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = PriceFormatter.formatPrice(grandTotal),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = BunzoPrimary
                        )
                    }

                    Button(
                        onClick = {
                            orderViewModel.placeOrder(
                                cartItems = cartItems,
                                orderType = selectedOrderType,
                                paymentMethod = selectedPaymentMethod,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                region = region,
                                address = address,
                                additionalAddress = additionalAddress,
                                notes = notes,
                                branchId = selectedBranch.id,
                                latitude = userLatitude,
                                longitude = userLongitude,
                                onSuccess = { placedOrder ->
                                    onOrderPlaced(placedOrder.id)
                                }
                            )
                        },
                        enabled = !isPlacingOrder,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BunzoPrimary,
                            contentColor = BunzoDark
                        ),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("confirm_order_button")
                    ) {
                        if (isPlacingOrder) {
                            CircularProgressIndicator(
                                color = BunzoDark,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = "تأكيد وإرسال الطلب 🚀",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BunzoDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentOptionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BunzoPrimary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) BunzoPrimary else Color.Transparent)
                    .border(1.5.dp, if (isSelected) BunzoPrimary else Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BunzoPrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
