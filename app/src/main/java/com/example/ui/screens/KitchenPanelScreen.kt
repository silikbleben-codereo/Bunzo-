package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SoupKitchen
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KitchenOrderView
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.ui.components.AccessDeniedView
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoError
import com.example.ui.theme.BunzoPrimary
import com.example.ui.theme.BunzoSuccess
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.KitchenFilter
import com.example.ui.viewmodel.KitchenViewModel

@Composable
fun KitchenPanelScreen(
    kitchenViewModel: KitchenViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    // Strict RBAC Guard: Block access if user is not KITCHEN, MANAGER, ADMIN, or OWNER
    if (currentUser == null || !currentUser!!.role.isKitchen) {
        AccessDeniedView(
            title = "وصول مقيّد: شاشة طاقم المطبخ",
            message = "عذراً، هذه الشاشة مخصصة لطاقم المطبخ وإعداد الطلبات (KDS) فقط. يرجى تسجيل الدخول بحساب مصرح له.",
            onBackClick = onNavigateBack,
            modifier = modifier
        )
        return
    }

    val filter by kitchenViewModel.selectedFilter.collectAsState()
    val allTickets by kitchenViewModel.kitchenTickets.collectAsState()

    val filteredTickets = when (filter) {
        KitchenFilter.ALL_ACTIVE -> allTickets.filter { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }
        KitchenFilter.NEW_ONLY -> allTickets.filter { it.status == OrderStatus.NEW || it.status == OrderStatus.ACCEPTED }
        KitchenFilter.PREPARING_ONLY -> allTickets.filter { it.status == OrderStatus.PREPARING }
        KitchenFilter.READY_ONLY -> allTickets.filter { it.status == OrderStatus.READY }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E141B))
            .testTag("kitchen_panel_screen")
    ) {
        // Kitchen Header Bar
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
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BunzoAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SoupKitchen,
                        contentDescription = null,
                        tint = BunzoPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "نظام شاشة المطبخ (KDS)",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = "تجهيز الوجبات المباشر (أمان عالي - بدون بيانات العميل)",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BunzoAccent)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${filteredTickets.size} تذاكر نشطة",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = Color(0xFF3B0528)
                )
            }
        }

        // Filter Tabs
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(KitchenFilter.values()) { f ->
                val count = when (f) {
                    KitchenFilter.ALL_ACTIVE -> allTickets.count { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }
                    KitchenFilter.NEW_ONLY -> allTickets.count { it.status == OrderStatus.NEW || it.status == OrderStatus.ACCEPTED }
                    KitchenFilter.PREPARING_ONLY -> allTickets.count { it.status == OrderStatus.PREPARING }
                    KitchenFilter.READY_ONLY -> allTickets.count { it.status == OrderStatus.READY }
                }
                FilterChip(
                    selected = filter == f,
                    onClick = { kitchenViewModel.setFilter(f) },
                    label = { Text("${f.labelAr} ($count)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BunzoAccent,
                        selectedLabelColor = Color(0xFF3B0528),
                        containerColor = Color(0xFF281C25),
                        labelColor = Color.White
                    ),
                    modifier = Modifier.testTag("kitchen_filter_${f.name}")
                )
            }
        }

        if (filteredTickets.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.OutdoorGrill,
                title = "لا توجد طلبات قيد الانتظار حالياً",
                subtitle = "جميع الوجبات تم تجهيزها وتسليمها بنجاح!",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTickets, key = { it.id }) { ticket ->
                    KitchenTicketCard(
                        ticket = ticket,
                        elapsedTime = kitchenViewModel.getElapsedTimeText(ticket.timestamp),
                        onAccept = { kitchenViewModel.markAccepted(ticket.id) },
                        onStartPrep = { kitchenViewModel.markPreparing(ticket.id) },
                        onMarkReady = { kitchenViewModel.markReady(ticket.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KitchenTicketCard(
    ticket: KitchenOrderView,
    elapsedTime: String,
    onAccept: () -> Unit,
    onStartPrep: () -> Unit,
    onMarkReady: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1C26)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kitchen_ticket_${ticket.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Order Number + Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ticket.orderNumber,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = BunzoAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ticket.orderType.labelAr,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Timer badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (ticket.status == OrderStatus.NEW) BunzoError.copy(alpha = 0.25f)
                            else BunzoAccent.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = if (ticket.status == OrderStatus.NEW) BunzoError else BunzoAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = elapsedTime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ticket.status == OrderStatus.NEW) BunzoError else BunzoAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            // Meal Items with exact sizes and toppings instructions
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ticket.items.forEach { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.quantity}x ${item.productNameAr}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )

                            if (!item.selectedSizeNameAr.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BunzoAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.selectedSizeNameAr,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BunzoAccent
                                    )
                                }
                            }
                        }

                        if (!item.selectedExtrasDescription.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+ إضافات: ${item.selectedExtrasDescription}",
                                fontSize = 12.sp,
                                color = BunzoAccent.copy(alpha = 0.9f)
                            )
                        }

                        if (!item.itemNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "⚠️ ملاحظة الشيف: ${item.itemNotes}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Yellow
                            )
                        }
                    }
                }
            }

            if (ticket.orderFoodNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📝 ملاحظات الطلب العامة: ${ticket.orderFoodNotes}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (ticket.status) {
                    OrderStatus.NEW, OrderStatus.ACCEPTED -> {
                        Button(
                            onClick = onStartPrep,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BunzoAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_prep_${ticket.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OutdoorGrill,
                                contentDescription = null,
                                tint = Color(0xFF3B0528),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "بدء الطهي والتحضير الآن 🍳",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFF3B0528)
                            )
                        }
                    }

                    OrderStatus.PREPARING -> {
                        Button(
                            onClick = onMarkReady,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BunzoSuccess),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("mark_ready_${ticket.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تم تجهيز الوجبة بالكامل (جاهز للتسليم) 📦",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }

                    OrderStatus.READY -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BunzoSuccess.copy(alpha = 0.2f))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "الوجبة جاهزة وبانتظار استلام الكابتن أو العميل ✅",
                                color = BunzoSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}
