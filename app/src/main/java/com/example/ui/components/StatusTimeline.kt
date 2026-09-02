package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoPrimary
import com.example.ui.theme.BunzoSuccess

data class TimelineStep(
    val status: OrderStatus,
    val titleAr: String,
    val subtitleAr: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean
)

@Composable
fun OrderStatusTimeline(
    currentStatus: OrderStatus,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        TimelineStep(OrderStatus.NEW, "تم استلام الطلب", "طلبك وصل لنظام المطعم بنجاح", currentStatus.ordinal >= OrderStatus.NEW.ordinal, currentStatus == OrderStatus.NEW),
        TimelineStep(OrderStatus.ACCEPTED, "تم قبول وتأكيد الطلب", "تمت مراجعة الطلب والموافقة عليه", currentStatus.ordinal >= OrderStatus.ACCEPTED.ordinal, currentStatus == OrderStatus.ACCEPTED),
        TimelineStep(OrderStatus.PREPARING, "قيد التحضير والطهي", "الشيف يحضر وجبتك الآن بعناية", currentStatus.ordinal >= OrderStatus.PREPARING.ordinal, currentStatus == OrderStatus.PREPARING),
        TimelineStep(OrderStatus.READY, "جاهز في المطعم", "تم تجهيز الوجبة وتغليفها", currentStatus.ordinal >= OrderStatus.READY.ordinal, currentStatus == OrderStatus.READY),
        TimelineStep(OrderStatus.OUT_FOR_DELIVERY, "في الطريق إليك", "الكابتن استلم الطلب ومتجه لعنوانك", currentStatus.ordinal >= OrderStatus.OUT_FOR_DELIVERY.ordinal, currentStatus == OrderStatus.OUT_FOR_DELIVERY),
        TimelineStep(OrderStatus.DELIVERED, "تم التوصيل بنجاح", "بالهناء والشفاء، نتمنى لك وجبة شهية", currentStatus.ordinal >= OrderStatus.DELIVERED.ordinal, currentStatus == OrderStatus.DELIVERED)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Indicator Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    step.isCompleted && !step.isCurrent -> BunzoSuccess
                                    step.isCurrent -> BunzoAccent
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (step.isCompleted && !step.isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else if (step.isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(BunzoPrimary)
                            )
                        }
                    }

                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(38.dp)
                                .background(
                                    if (step.isCompleted && steps[index + 1].isCompleted) BunzoSuccess
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Text
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = step.titleAr,
                        fontWeight = if (step.isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (step.isCurrent) BunzoPrimary else if (step.isCompleted) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                    Text(
                        text = step.subtitleAr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
