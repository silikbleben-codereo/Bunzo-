package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.remote.PhoneAuthState
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoDark
import com.example.ui.theme.BunzoError
import com.example.ui.theme.BunzoPrimary
import com.example.ui.theme.BunzoSuccess
import com.example.ui.viewmodel.AuthViewModel
import com.example.utils.SyrianPhoneValidator

@Composable
fun OtpVerificationScreen(
    phone: String,
    authViewModel: AuthViewModel,
    onVerificationSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var otpCode by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val phoneAuthState by authViewModel.phoneAuthState.collectAsState()
    val countdownSeconds by authViewModel.countdownSeconds.collectAsState()
    val canResend by authViewModel.canResend.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val successMessage by authViewModel.otpSuccessMessage.collectAsState()

    val formattedPhone = remember(phone) {
        SyrianPhoneValidator.normalizeToInternational(phone)
    }

    // Auto-focus on input field when entering screen
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    // Auto submit code when 6 digits are typed
    LaunchedEffect(otpCode) {
        if (otpCode.length == 6 && otpCode.all { it.isDigit() }) {
            authViewModel.verifyOtp(
                smsCode = otpCode,
                phone = formattedPhone,
                onSuccess = { user ->
                    onVerificationSuccess(user)
                }
            )
        }
    }

    // Listen for automatic verification from Firebase (Play services SMS retriever)
    LaunchedEffect(phoneAuthState) {
        if (phoneAuthState is PhoneAuthState.Verified) {
            val verifiedState = phoneAuthState as PhoneAuthState.Verified
            val currentLoggedIn = authViewModel.currentUser.value
            if (currentLoggedIn != null) {
                onVerificationSuccess(currentLoggedIn)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("otp_verification_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Hero Icon Box
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(BunzoPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(BunzoPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sms,
                    contentDescription = null,
                    tint = BunzoAccent,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "تأكيد رقم الهاتف عبر SMS 📲",
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "أدخل رمز التحقق (OTP) المكون من 6 أرقام المرسل عبر رسالة نصية إلى رقمك السوري:",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Syrian Phone Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(BunzoPrimary.copy(alpha = 0.08f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🇸🇾", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formattedPhone,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BunzoPrimary
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 6-Digit Segmented Visual OTP Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { focusRequester.requestFocus() },
            contentAlignment = Alignment.Center
        ) {
            // Hidden BasicTextField capturing keyboard inputs
            BasicTextField(
                value = otpCode,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    if (filtered.length <= 6) {
                        otpCode = filtered
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .size(1.dp)
                    .testTag("otp_code_input")
            )

            // 6 Visual Digit Boxes
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 6) {
                    val digit = otpCode.getOrNull(i)?.toString() ?: ""
                    val isFocused = otpCode.length == i || (otpCode.length == 6 && i == 5)
                    val borderColor by animateColorAsState(
                        targetValue = when {
                            errorMessage != null -> BunzoError
                            isFocused -> BunzoPrimary
                            digit.isNotEmpty() -> BunzoPrimary.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        },
                        animationSpec = tween(200),
                        label = "digit_border"
                    )

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (digit.isNotEmpty()) {
                                BunzoPrimary.copy(alpha = 0.04f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        modifier = Modifier
                            .size(46.dp, 56.dp)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("otp_digit_box_$i"),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 2.dp else 0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = digit,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BunzoPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Success / Status Message
        if (successMessage != null && errorMessage == null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(BunzoSuccess.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = BunzoSuccess,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = successMessage ?: "",
                    color = BunzoSuccess,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Error Message
        if (errorMessage != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(BunzoError.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = BunzoError,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = errorMessage ?: "",
                    color = BunzoError,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = {
                if (otpCode.length == 6) {
                    authViewModel.verifyOtp(
                        smsCode = otpCode,
                        phone = formattedPhone,
                        onSuccess = { user ->
                            onVerificationSuccess(user)
                        }
                    )
                }
            },
            enabled = otpCode.length == 6 && !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BunzoPrimary,
                contentColor = BunzoDark,
                disabledContainerColor = BunzoPrimary.copy(alpha = 0.4f),
                disabledContentColor = BunzoDark.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("verify_otp_button")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = BunzoDark,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("جاري التحقق من Firebase...", fontWeight = FontWeight.Bold, color = BunzoDark)
            } else {
                Text(
                    text = "تحقق وتأكيد الرقم ✅",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BunzoDark
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Resend Section
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (countdownSeconds > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { countdownSeconds / 60f },
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = BunzoPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "يمكنك طلب رمز جديد بعد: $countdownSeconds ثانية",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "لم يصلك الرمز؟",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(
                            onClick = {
                                otpCode = ""
                                authViewModel.resendOtp(activity, formattedPhone)
                            },
                            enabled = canResend && !isLoading,
                            modifier = Modifier.testTag("resend_otp_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = BunzoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "إعادة إرسال رمز التحقق 🔁",
                                color = BunzoPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
