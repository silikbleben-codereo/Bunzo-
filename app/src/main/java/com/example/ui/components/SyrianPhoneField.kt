package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Country
import com.example.ui.theme.BunzoError
import com.example.ui.theme.BunzoPrimary
import com.example.ui.theme.BunzoSuccess
import com.example.utils.SyrianPhoneValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyrianPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "رقم الهاتف",
    selectedCountry: Country = Country.SYRIA,
    onCountrySelect: (Country) -> Unit = {},
    isError: Boolean = false,
    errorMessage: String? = null,
    testTag: String = "phone_input_field"
) {
    var showCountryPicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isSyria = selectedCountry.dialCode == "+963"
    val validationErr = if (isSyria) SyrianPhoneValidator.getValidationError(value) else null
    val isValid = value.isNotBlank() && (if (isSyria) validationErr == null else value.length >= 7)

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() || it == '+' || it == ' ' }
                if (filtered.length <= 16) {
                    onValueChange(filtered)
                }
            },
            label = { Text(label) },
            placeholder = { Text(if (isSyria) "0933112233 أو 933112233" else selectedCountry.exampleNumber) },
            leadingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showCountryPicker = true }
                        .padding(start = 12.dp, end = 6.dp)
                        .testTag("country_code_selector")
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(0.5.dp, Color.LightGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedCountry.flagEmoji, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedCountry.dialCode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BunzoPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "اختيار الدولة",
                        tint = BunzoPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(Color.LightGray)
                    )
                }
            },
            trailingIcon = {
                if (isValid) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "رقم صحيح",
                        tint = BunzoSuccess
                    )
                } else if (value.isNotBlank() && (isError || validationErr != null)) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "رقم غير صحيح",
                        tint = BunzoError
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            },
            isError = isError || (value.isNotBlank() && validationErr != null),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BunzoPrimary,
                focusedLabelColor = BunzoPrimary,
                cursorColor = BunzoPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )

        val displayError = errorMessage ?: if (value.isNotBlank() && isSyria) validationErr else null
        if (displayError != null) {
            Text(
                text = displayError,
                color = BunzoError,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }

    if (showCountryPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCountryPicker = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اختر الدولة ورمز الاتصال 🌍",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = { showCountryPicker = false }) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث عن دولة...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                val filteredCountries = Country.SUPPORTED_COUNTRIES.filter {
                    it.nameAr.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.dialCode.contains(searchQuery.trim()) ||
                    it.code.contains(searchQuery.trim(), ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(filteredCountries) { country ->
                        val isSelected = country.dialCode == selectedCountry.dialCode
                        Card(
                            onClick = {
                                onCountrySelect(country)
                                showCountryPicker = false
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BunzoPrimary.copy(alpha = 0.1f) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(country.flagEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = country.nameAr,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = country.dialCode,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = BunzoPrimary
                                )
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
