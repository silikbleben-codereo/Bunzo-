package com.example.ui.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BunzoApplication
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.remote.FirebasePhoneAuthManager
import com.example.data.remote.PhoneAuthState
import com.example.data.repository.BunzoRepository
import com.example.utils.SyrianPhoneValidator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: BunzoRepository = BunzoApplication.instance.repository
) : ViewModel() {

    private val phoneAuthManager: FirebasePhoneAuthManager = repository.phoneAuthManager

    val currentUser: StateFlow<User?> = repository.currentUserState
    val phoneAuthState: StateFlow<PhoneAuthState> = phoneAuthManager.authState

    private val _countdownSeconds = MutableStateFlow(0)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _canResend = MutableStateFlow(true)
    val canResend: StateFlow<Boolean> = _canResend.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _otpSuccessMessage = MutableStateFlow<String?>(null)
    val otpSuccessMessage: StateFlow<String?> = _otpSuccessMessage.asStateFlow()

    private var countdownJob: Job? = null

    val isCustomer: Boolean
        get() = currentUser.value?.role == UserRole.CUSTOMER || currentUser.value == null

    val isKitchen: Boolean
        get() = currentUser.value?.role == UserRole.KITCHEN

    val isAdmin: Boolean
        get() = currentUser.value?.role == UserRole.ADMIN || currentUser.value?.role == UserRole.OWNER

    private data class PendingRegistration(
        val firstName: String,
        val lastName: String,
        val phone: String,
        val email: String,
        val region: String,
        val address: String,
        val pass: String
    )

    private var pendingRegistration: PendingRegistration? = null
    private var pendingLoginPassword: Pair<String, String>? = null // (phone, pass)

    init {
        // Observe PhoneAuthState for state transitions, auto-verification, and errors
        viewModelScope.launch {
            phoneAuthState.collect { state ->
                when (state) {
                    is PhoneAuthState.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = state.message
                    }
                    is PhoneAuthState.CodeSending -> {
                        _isLoading.value = true
                        _errorMessage.value = null
                    }
                    is PhoneAuthState.CodeSent -> {
                        _isLoading.value = false
                        _otpSuccessMessage.value = "تم إرسال رمز التحقق عبر رسالة نصية قصيرة SMS"
                        startCountdownTimer()
                    }
                    is PhoneAuthState.Verifying -> {
                        _isLoading.value = true
                        _errorMessage.value = null
                    }
                    is PhoneAuthState.AutoVerified -> {
                        _isLoading.value = true
                        _otpSuccessMessage.value = "تم التحقق التلقائي من رمز SMS ✓"
                        completeAuthentication(state.phone, state.firebaseUser.uid) {}
                    }
                    is PhoneAuthState.Verified -> {
                        _isLoading.value = false
                        _otpSuccessMessage.value = "تم تأكيد رقم الهاتف بنجاح ✓"
                    }
                    is PhoneAuthState.Idle -> {
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    private fun startCountdownTimer() {
        countdownJob?.cancel()
        _countdownSeconds.value = 60
        _canResend.value = false
        countdownJob = viewModelScope.launch {
            while (_countdownSeconds.value > 0) {
                delay(1000)
                _countdownSeconds.value -= 1
            }
            _canResend.value = true
        }
    }

    fun prepareRegistrationAndSendOtp(
        activity: Activity?,
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        region: String,
        address: String,
        pass: String,
        confirmPass: String,
        onOtpSent: (String) -> Unit
    ) {
        _errorMessage.value = null
        if (firstName.isBlank() || lastName.isBlank()) {
            _errorMessage.value = "يرجى إدخال الاسم الأول واسم العائلة"
            return
        }
        val phoneErr = SyrianPhoneValidator.getValidationError(phone)
        if (phoneErr != null) {
            _errorMessage.value = phoneErr
            return
        }
        if (pass.length < 6) {
            _errorMessage.value = "كلمة المرور يجب أن تتكون من 6 أحرف أو أرقام على الأقل"
            return
        }
        if (pass != confirmPass) {
            _errorMessage.value = "كلمتا المرور غير متطابقتين"
            return
        }

        val normalizedPhone = SyrianPhoneValidator.normalizeToInternational(phone)

        pendingRegistration = PendingRegistration(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            phone = normalizedPhone,
            email = email.trim(),
            region = region.trim(),
            address = address.trim(),
            pass = pass
        )

        phoneAuthManager.sendVerificationCode(
            activity = activity,
            e164PhoneNumber = normalizedPhone,
            forceResend = false,
            onCodeSent = { onOtpSent(normalizedPhone) },
            onInstantVerified = { user ->
                viewModelScope.launch {
                    completeAuthentication(normalizedPhone, user.uid) {}
                }
            },
            onError = { msg ->
                _isLoading.value = false
                _errorMessage.value = msg
            }
        )
    }

    fun loginAndSendOtp(
        activity: Activity?,
        phone: String,
        pass: String,
        onOtpSent: (String) -> Unit
    ) {
        _errorMessage.value = null
        val phoneErr = SyrianPhoneValidator.getValidationError(phone)
        if (phoneErr != null) {
            _errorMessage.value = phoneErr
            return
        }
        if (pass.isBlank()) {
            _errorMessage.value = "يرجى إدخال كلمة المرور"
            return
        }

        val normalizedPhone = SyrianPhoneValidator.normalizeToInternational(phone)
        pendingLoginPassword = Pair(normalizedPhone, pass)

        phoneAuthManager.sendVerificationCode(
            activity = activity,
            e164PhoneNumber = normalizedPhone,
            forceResend = false,
            onCodeSent = { onOtpSent(normalizedPhone) },
            onInstantVerified = { user ->
                viewModelScope.launch {
                    completeAuthentication(normalizedPhone, user.uid) {}
                }
            },
            onError = { msg ->
                _isLoading.value = false
                _errorMessage.value = msg
            }
        )
    }

    fun requestOtp(activity: Activity?, phone: String, onSuccess: () -> Unit = {}) {
        _errorMessage.value = null
        _otpSuccessMessage.value = null
        val phoneErr = SyrianPhoneValidator.getValidationError(phone)
        if (phoneErr != null) {
            _errorMessage.value = phoneErr
            return
        }

        val normalizedPhone = SyrianPhoneValidator.normalizeToInternational(phone)
        phoneAuthManager.sendVerificationCode(
            activity = activity,
            e164PhoneNumber = normalizedPhone,
            forceResend = false,
            onCodeSent = { onSuccess() },
            onInstantVerified = { user ->
                viewModelScope.launch {
                    completeAuthentication(normalizedPhone, user.uid) {}
                }
            },
            onError = { msg ->
                _isLoading.value = false
                _errorMessage.value = msg
            }
        )
    }

    fun resendOtp(activity: Activity?, phone: String) {
        if (!_canResend.value) return
        _errorMessage.value = null
        val normalizedPhone = SyrianPhoneValidator.normalizeToInternational(phone)
        phoneAuthManager.sendVerificationCode(
            activity = activity,
            e164PhoneNumber = normalizedPhone,
            forceResend = true,
            onCodeSent = {
                startCountdownTimer()
            },
            onInstantVerified = { user ->
                viewModelScope.launch {
                    completeAuthentication(normalizedPhone, user.uid) {}
                }
            },
            onError = { msg ->
                _isLoading.value = false
                _errorMessage.value = msg
            }
        )
    }

    fun verifyOtp(
        smsCode: String,
        phone: String,
        onSuccess: (User) -> Unit
    ) {
        _errorMessage.value = null
        val normalizedPhone = SyrianPhoneValidator.normalizeToInternational(phone)

        _isLoading.value = true
        viewModelScope.launch {
            val verificationId = phoneAuthManager.getActiveVerificationId()
            val result = phoneAuthManager.verifyOtpCode(
                verificationId = verificationId,
                code = smsCode,
                targetPhone = normalizedPhone
            )

            result.onSuccess { firebaseUser ->
                completeAuthentication(normalizedPhone, firebaseUser.uid, onSuccess)
            }.onFailure { err ->
                _isLoading.value = false
                _errorMessage.value = err.message ?: "فشل التحقق من رمز SMS"
            }
        }
    }

    /**
     * Completes user sign-in / registration after verified Phone Authentication
     */
    private suspend fun completeAuthentication(
        phone: String,
        firebaseUid: String,
        onSuccess: (User) -> Unit
    ) {
        val pendingReg = pendingRegistration
        if (pendingReg != null && SyrianPhoneValidator.normalizeToInternational(pendingReg.phone) == phone) {
            val regRes = repository.register(
                firstName = pendingReg.firstName,
                lastName = pendingReg.lastName,
                phone = phone,
                email = pendingReg.email,
                region = pendingReg.region,
                address = pendingReg.address,
                password = pendingReg.pass,
                firebaseUid = firebaseUid
            )
            pendingRegistration = null
            _isLoading.value = false
            regRes.onSuccess { user ->
                _otpSuccessMessage.value = "تم توثيق الحساب ورقم الهاتف بنجاح ✓"
                onSuccess(user)
            }.onFailure {
                _errorMessage.value = it.message ?: "فشل حفظ بيانات المستخدم"
            }
        } else {
            val loginRes = repository.loginWithVerifiedPhone(phone, firebaseUid)
            pendingLoginPassword = null
            _isLoading.value = false
            loginRes.onSuccess { user ->
                _otpSuccessMessage.value = "تم تسجيل الدخول وتأكيد رقم الهاتف بنجاح ✓"
                onSuccess(user)
            }.onFailure {
                val fallbackUser = User(
                    id = firebaseUid,
                    phone = phone,
                    phoneVerified = true,
                    phoneVerifiedAt = System.currentTimeMillis(),
                    firebaseUid = firebaseUid,
                    role = UserRole.CUSTOMER
                )
                onSuccess(fallbackUser)
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, region: String, address: String, email: String) {
        viewModelScope.launch {
            repository.updateProfile(firstName, lastName, region, address, email)
        }
    }

    fun logout() {
        phoneAuthManager.resetState()
        countdownJob?.cancel()
        _countdownSeconds.value = 0
        _canResend.value = true
        repository.logout()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearOtpState() {
        phoneAuthManager.resetState()
        countdownJob?.cancel()
        _countdownSeconds.value = 0
        _canResend.value = true
        _otpSuccessMessage.value = null
        _errorMessage.value = null
    }
}
