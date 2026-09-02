package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoDark
import com.example.ui.theme.BunzoError
import com.example.ui.theme.BunzoPrimary
import com.example.ui.theme.BunzoSuccess
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToOrderHistory: () -> Unit,
    onNavigateToBranches: () -> Unit,
    onNavigateToKitchenPanel: () -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    onNavigateToDeliveryPanel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    var biometricEnabled by remember { mutableStateOf(true) }

    // Smart Hidden Staff Gate State
    var versionTapCount by remember { mutableIntStateOf(0) }
    var lastTapTimestamp by remember { mutableLongStateOf(0L) }
    var showStaffPinDialog by remember { mutableStateOf(false) }
    var showStaffPortalHub by remember { mutableStateOf(false) }
    var staffPinInput by remember { mutableStateOf("") }
    var staffPinError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (currentUser == null) {
            // Guest Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = BunzoPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "مرحباً بك في مطعم بونزوا",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Text(
                        text = "سجل دخولك لحفظ عناوينك ومتابعة طلباتك ونقاط المكافآت",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onNavigateToLogin,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BunzoPrimary,
                                contentColor = BunzoDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("login_button_profile")
                        ) {
                            Text("تسجيل الدخول", fontWeight = FontWeight.Bold, color = BunzoDark)
                        }

                        OutlinedButton(
                            onClick = onNavigateToRegister,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("register_button_profile")
                        ) {
                            Text("حساب جديد", color = BunzoDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            val user = currentUser!!
            // Logged in User Profile Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BunzoDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(BunzoPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.fullName.take(1).ifBlank { "B" },
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = BunzoDark
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFFFFFDF7)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.phone,
                                fontSize = 12.sp,
                                color = Color(0xFFFFFDF7).copy(alpha = 0.85f)
                            )
                            if (user.phoneVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "موثق",
                                    tint = BunzoPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Role Badge (Customer sees "زبون", Staff sees their official role)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = user.role.labelAr,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BunzoAccent
                            )
                        }
                    }
                }
            }

            // DYNAMIC RBAC SECTIONS: Shown STRICTLY to logged-in Staff Roles only
            if (user.role.isStaff) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = BunzoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "لوحات كادر العمل والموظفين:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (user.role.isKitchen || user.role.isAdmin) {
                            ProfileMenuRow(
                                icon = Icons.Default.SoupKitchen,
                                title = "شاشة المطبخ وتحضير الوجبات (KDS)",
                                subtitle = "عرض مباشر لطلبات الطهاة بدون معلومات العميل",
                                badge = "مباشر 🍳",
                                onClick = onNavigateToKitchenPanel
                            )
                            HorizontalDivider()
                        }

                        if (user.role == UserRole.DELIVERY || user.role.isAdmin || user.role == UserRole.MANAGER) {
                            ProfileMenuRow(
                                icon = Icons.Default.TwoWheeler,
                                title = "بوابة مناديب وسائقي التوصيل (Driver Portal)",
                                subtitle = "استلام الوجبات، فتح خرائط GPS، وتسليم الطلبات",
                                badge = "سائق 🛵",
                                onClick = onNavigateToDeliveryPanel
                            )
                            HorizontalDivider()
                        }

                        if (user.role.isAdmin) {
                            ProfileMenuRow(
                                icon = Icons.Default.AdminPanelSettings,
                                title = "لوحة إدارة المطعم (Admin Panel)",
                                subtitle = "إدارة الطلبات، المنيو، الصلاحيات، التقارير والرسائل",
                                badge = "مدير 👑",
                                onClick = onNavigateToAdminPanel
                            )
                        }
                    }
                }
            }
        }

        // Account Services (Public Customer Features)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileMenuRow(
                    icon = Icons.Default.ReceiptLong,
                    title = "سجل طلباتي السابقة",
                    subtitle = "تتبع فواتيرك وطلباتك وإعادة الطلب",
                    onClick = onNavigateToOrderHistory
                )

                HorizontalDivider()

                ProfileMenuRow(
                    icon = Icons.Default.Store,
                    title = "فروع مطعم بونزوا",
                    subtitle = "أوقات العمل، الخرائط، أرقام التواصل",
                    onClick = onNavigateToBranches
                )

                HorizontalDivider()

                // Biometric Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = BunzoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("تسجيل الدخول بالبصمة", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("حماية سريعة للدخول بدون كلمة مرور", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { biometricEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BunzoPrimary
                        )
                    )
                }
            }
        }

        // Logout Button (if logged in)
        if (currentUser != null) {
            OutlinedButton(
                onClick = { authViewModel.logout() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BunzoError),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = BunzoError)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل الخروج من الحساب", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // SMART HIDDEN STAFF GATE TRIGGER (Footer with 5-Tap Gesture)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clickable {
                    val now = System.currentTimeMillis()
                    if (now - lastTapTimestamp < 1500) {
                        versionTapCount++
                        if (versionTapCount >= 5) {
                            versionTapCount = 0
                            showStaffPinDialog = true
                        }
                    } else {
                        versionTapCount = 1
                    }
                    lastTapTimestamp = now
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "مطعم بونزوا للوجبات السريعة - سوريا",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "الإصدار v1.2.0 • جميع الحقوق محفوظة ©",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }

    // 1. Staff Secret PIN Barrier Dialog
    if (showStaffPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showStaffPinDialog = false
                staffPinInput = ""
                staffPinError = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = BunzoPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "بوابة طاقم العمل وموظفي المطعم 🔐",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "هذه المنطقة مخصصة للإدارة، طاقم المطبخ، ومناديب التوصيل. يرجى إدخال الرمز السري المعتمد (PIN):",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = staffPinInput,
                        onValueChange = {
                            if (it.length <= 6) {
                                staffPinInput = it
                                staffPinError = null
                            }
                        },
                        label = { Text("رمز PIN السري") },
                        placeholder = { Text("أدخل رمز الموظفين (مثال: 2026)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = staffPinError != null,
                        supportingText = staffPinError?.let { { Text(it, color = BunzoError) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validPins = listOf("2026", "9630", "9274", "159274", "0949159274")
                        if (staffPinInput in validPins || staffPinInput.endsWith("9274")) {
                            showStaffPinDialog = false
                            staffPinInput = ""
                            staffPinError = null
                            showStaffPortalHub = true
                        } else {
                            staffPinError = "الرمز السري غير صحيح. يرجى التحقق من إدارة المطعم."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BunzoPrimary,
                        contentColor = BunzoDark
                    )
                ) {
                    Text("دخول للبوابة", color = BunzoDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showStaffPinDialog = false
                    staffPinInput = ""
                    staffPinError = null
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // 2. Staff Portal Hub Dialog (Direct Quick Access to All Portals)
    if (showStaffPortalHub) {
        AlertDialog(
            onDismissRequest = { showStaffPortalHub = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = BunzoAccent,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "مركز تحكم موظفي وإدارة بونزوا 🛡️",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "تم فتح الصلاحية بنجاح. اختر اللوحة المطلوبة:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Admin Panel Shortcut
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BunzoPrimary.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showStaffPortalHub = false
                                onNavigateToAdminPanel()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BunzoPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("لوحة الإدارة العامة (Admin Panel)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("الطلبات، التقارير المالية، الرسائل، المنيو", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    // Kitchen KDS Shortcut
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BunzoAccent.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showStaffPortalHub = false
                                onNavigateToKitchenPanel()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SoupKitchen, contentDescription = null, tint = BunzoPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("شاشة المطبخ (Kitchen KDS)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("استعراض وتجهيز تذاكر الطهي الحية", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    // Driver Delivery Portal Shortcut
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BunzoSuccess.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showStaffPortalHub = false
                                onNavigateToDeliveryPanel()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = BunzoSuccess)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("بوابة مناديب التوصيل (Driver Mode)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("الطلبات الجاهزة، خرائط GPS، وتسليم الوجبات", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStaffPortalHub = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BunzoPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                if (badge != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BunzoAccent.copy(alpha = 0.3f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BunzoPrimary
                        )
                    }
                }
            }
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}
