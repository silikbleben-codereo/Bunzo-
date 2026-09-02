package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoPrimary
import com.example.utils.SoundHelper

@Composable
fun WelcomeDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToBranches: () -> Unit
) {
    if (!isOpen) return

    LaunchedEffect(Unit) {
        SoundHelper.playWelcomeChime()
    }

    Dialog(
        onDismissRequest = {
            SoundHelper.playClickSound()
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(300)) + scaleIn(tween(350))
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 16.dp)
                    .testTag("welcome_dialog_card")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                SoundHelper.playClickSound()
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F7))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Authentic Bunzo Logo Badge
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(2.dp, BunzoAccent, RoundedCornerShape(20.dp))
                            .background(Color(0xFFF7B731)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bunzo_logo),
                            contentDescription = "شعار مطعم بونزوا",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "أهلاً بك في مطعم بونزوا! 🍔",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = BunzoPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtitle
                    Text(
                        text = "وجهتك الأولى لأشهى وجبات البرغر والشاورما والبروستد الفاخرة بأعلى معايير الجودة في سوريا 🇸🇾",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Feature highlights
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFDF7E7), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        WelcomeFeatureRow(
                            icon = Icons.Default.DeliveryDining,
                            title = "توصيل سريع وساخن",
                            description = "تغطية كاملة لدمشق، حلب، واللاذقية"
                        )
                        WelcomeFeatureRow(
                            icon = Icons.Default.LocalOffer,
                            title = "عروض توفير يومية",
                            description = "خصومات حصرية على الوجبات العائلية والفردية"
                        )
                        WelcomeFeatureRow(
                            icon = Icons.Default.Payment,
                            title = "طرق دفع سهلة وآمنة",
                            description = "سيريتل كاش، MTN كاش، شام بنك، والدفع عند الاستلام"
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Main Action Button
                    Button(
                        onClick = {
                            SoundHelper.playClickSound()
                            onNavigateToMenu()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BunzoPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("welcome_explore_menu_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = BunzoAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تصفح المنيو وابدأ الطلب",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Branches Button
                    OutlinedButton(
                        onClick = {
                            SoundHelper.playClickSound()
                            onNavigateToBranches()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("welcome_branches_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = BunzoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "فروعنا ومواقع المطاعم",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = BunzoPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeFeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BunzoAccent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BunzoPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = BunzoPrimary
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
