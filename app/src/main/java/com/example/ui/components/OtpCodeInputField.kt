package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoError
import com.example.ui.theme.BunzoPrimary

@Composable
fun OtpCodeInputField(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    isError: Boolean = false,
    onComplete: ((String) -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Hidden transparent input capturing keyboard events and paste
        BasicTextField(
            value = TextFieldValue(otpValue, selection = TextRange(otpValue.length)),
            onValueChange = { newValue ->
                val digitsOnly = newValue.text.filter { it.isDigit() }.take(length)
                onOtpChange(digitsOnly)
                if (digitsOnly.length == length) {
                    onComplete?.invoke(digitsOnly)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = if (otpValue.length == length) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (otpValue.length == length) {
                        onComplete?.invoke(otpValue)
                    }
                }
            ),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester)
                .testTag("hidden_otp_input")
        )

        // Visual 6-Box Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusRequester.requestFocus()
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (index in 0 until length) {
                val char = otpValue.getOrNull(index)?.toString() ?: ""
                val isCurrent = index == otpValue.length
                val isFilled = index < otpValue.length

                val borderColor = when {
                    isError -> BunzoError
                    isCurrent -> BunzoPrimary
                    isFilled -> BunzoAccent
                    else -> MaterialTheme.colorScheme.outlineVariant
                }

                val backgroundColor = when {
                    isError -> BunzoError.copy(alpha = 0.05f)
                    isCurrent -> BunzoPrimary.copy(alpha = 0.06f)
                    isFilled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.surface
                }

                Box(
                    modifier = Modifier
                        .size(width = 46.dp, height = 54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .border(
                            width = if (isCurrent) 2.dp else 1.5.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .testTag("otp_digit_box_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isError) BunzoError else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
