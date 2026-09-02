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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoError
import com.example.ui.theme.BunzoPrimary
import com.example.ui.theme.BunzoSuccess
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.OrderViewModel
import com.example.utils.PriceFormatter

@Composable
fun OrderHistoryScreen(
    orderViewModel: OrderViewModel,
    mainViewModel: MainViewModel,
    onNavigateToTracking: (String) -> Unit,
    onNavigateToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orders by orderViewModel.orders.collectAsState()

    if (orders.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.ReceiptLong,
            title = "لا توجد طلبات سابقة بعد",
            subtitle = "اطلب الآن وجبتك المفضلة من بونزوا لتظهر سجلات طلباتك هنا",
            buttonText = "تصفح القائمة والطلب 🍔",
            onButtonClick = onNavigateToMenu,
            modifier = modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("order_history_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders, key = { it.id }) { order ->
                OrderHistoryCard(
                    order = order,
                    onTrackClick = { onNavigateToTracking(order.id) }
                )
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(
    order: Order,
    onTrackClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTrackClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Number + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = BunzoPrimary
                )

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (order.status) {
                                OrderStatus.DELIVERED -> BunzoSuccess.copy(alpha = 0.15f)
                                OrderStatus.CANCELLED -> BunzoError.copy(alpha = 0.15f)
                                else -> BunzoAccent.copy(alpha = 0.25f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status.labelAr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            OrderStatus.DELIVERED -> BunzoSuccess
                            OrderStatus.CANCELLED -> BunzoError
                            else -> BunzoPrimary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Date
            Text(
                text = order.date,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Items Summary
            val itemsSummary = order.items.joinToString(", ") { "${it.quantity}x ${it.productNameAr}" }
            Text(
                text = itemsSummary,
                fontSize = 12.sp,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Total + Track Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("الإجمالي:", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = PriceFormatter.formatPrice(order.totalAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BunzoPrimary
                    )
                }

                Button(
                    onClick = onTrackClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BunzoPrimary)
                ) {
                    Text("تتبع الطلب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
