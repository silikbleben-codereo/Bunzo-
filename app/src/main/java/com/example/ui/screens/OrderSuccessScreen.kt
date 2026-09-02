package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoDark
import com.example.ui.theme.BunzoPrimary
import com.example.ui.theme.BunzoSuccess
import com.example.ui.viewmodel.OrderViewModel
import com.example.utils.PriceFormatter

@Composable
fun OrderSuccessScreen(
    orderId: String,
    orderViewModel: OrderViewModel,
    onTrackOrder: (String) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val order = orderViewModel.getOrderById(orderId)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("order_success_screen")
    ) {
        // Big Success Badge
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(BunzoSuccess),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "تم تأكيد طلبك بنجاح! 🎉",
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "تم إرسال الطلب مباشرة لنظام مطبخ بونزوا وبدأ تحضيره",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Order Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("رقم الطلب:", fontSize = 13.sp, color = Color.Gray)
                    Text(
                        text = order?.orderNumber ?: orderId,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BunzoPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الرقم المرجعي:", fontSize = 13.sp, color = Color.Gray)
                    Text(
                        text = order?.referenceNumber ?: "REF-963-0000",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الفرع المسؤول:", fontSize = 13.sp, color = Color.Gray)
                    Text(
                        text = order?.branchNameAr ?: "فرع دمشق - المزة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (order != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الإجمالي الكلي:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = PriceFormatter.formatPrice(order.totalAmount),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = BunzoPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Action Buttons
        Button(
            onClick = { onTrackOrder(orderId) },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BunzoPrimary,
                contentColor = BunzoDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("track_order_button")
        ) {
            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = BunzoDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text("متابعة حالة الطلب مباشرة 🛵", fontWeight = FontWeight.Bold, color = BunzoDark)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackToHome,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("back_to_home_button")
        ) {
            Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = BunzoDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text("العودة للرئيسية", color = BunzoDark, fontWeight = FontWeight.Bold)
        }
    }
}
