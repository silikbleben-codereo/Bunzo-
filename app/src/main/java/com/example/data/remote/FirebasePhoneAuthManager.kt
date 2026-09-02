package com.example.data.remote

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed interface PhoneAuthState {
    object Idle : PhoneAuthState
    data class CodeSending(val phone: String) : PhoneAuthState
    data class CodeSent(
        val verificationId: String,
        val phone: String,
        val token: PhoneAuthProvider.ForceResendingToken?
    ) : PhoneAuthState
    data class Verifying(val code: String) : PhoneAuthState
    data class Verified(val firebaseUser: FirebaseUser, val phone: String) : PhoneAuthState
    data class AutoVerified(val firebaseUser: FirebaseUser, val phone: String) : PhoneAuthState
    data class Error(val message: String, val errorType: PhoneAuthErrorType = PhoneAuthErrorType.GENERIC) : PhoneAuthState
}

enum class PhoneAuthErrorType {
    INVALID_PHONE_NUMBER,
    INVALID_SMS_CODE,
    CODE_EXPIRED,
    QUOTA_EXCEEDED,
    NETWORK_ERROR,
    ALREADY_LINKED,
    GENERIC
}

class FirebasePhoneAuthManager(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val _authState = MutableStateFlow<PhoneAuthState>(PhoneAuthState.Idle)
    val authState: StateFlow<PhoneAuthState> = _authState.asStateFlow()

    private var activeVerificationId: String? = null
    private var activeResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var lastSentPhone: String? = null
    private var lastSentTimestamp: Long = 0L

    companion object {
        private const val TAG = "FirebasePhoneAuth"
        private const val SMS_TIMEOUT_SECONDS = 60L
        private const val MIN_RESEND_COOLDOWN_MS = 60_000L
    }

    /**
     * Sends a real SMS OTP to the given phone number using Firebase Phone Authentication.
     */
    fun sendVerificationCode(
        activity: Activity?,
        e164PhoneNumber: String,
        forceResend: Boolean = false,
        onCodeSent: ((String) -> Unit)? = null,
        onInstantVerified: ((FirebaseUser) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        val now = System.currentTimeMillis()
        if (lastSentPhone == e164PhoneNumber && (now - lastSentTimestamp) < MIN_RESEND_COOLDOWN_MS && !forceResend) {
            val remainingSec = ((MIN_RESEND_COOLDOWN_MS - (now - lastSentTimestamp)) / 1000).coerceAtLeast(1)
            val msg = "يرجى الانتظار $remainingSec ثانية قبل إعادة طلب رمز التحقق"
            _authState.value = PhoneAuthState.Error(msg, PhoneAuthErrorType.QUOTA_EXCEEDED)
            onError?.invoke(msg)
            return
        }

        _authState.value = PhoneAuthState.CodeSending(e164PhoneNumber)
        lastSentPhone = e164PhoneNumber
        lastSentTimestamp = now

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "Instant verification completed: ${credential.smsCode}")
                // Automatic Instant Verification or SMS auto-retrieval
                signInWithCredentialInternal(credential, e164PhoneNumber) { userResult ->
                    userResult.onSuccess { user ->
                        _authState.value = PhoneAuthState.AutoVerified(user, e164PhoneNumber)
                        onInstantVerified?.invoke(user)
                    }.onFailure { err ->
                        val translated = mapExceptionToArabic(err)
                        _authState.value = PhoneAuthState.Error(translated.first, translated.second)
                        onError?.invoke(translated.first)
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "Firebase phone verification failed", e)
                val (msg, type) = mapExceptionToArabic(e)
                _authState.value = PhoneAuthState.Error(msg, type)
                onError?.invoke(msg)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "SMS OTP sent successfully. VerificationId: $verificationId")
                activeVerificationId = verificationId
                activeResendToken = token
                _authState.value = PhoneAuthState.CodeSent(verificationId, e164PhoneNumber, token)
                onCodeSent?.invoke(verificationId)
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                Log.d(TAG, "Auto-retrieval timeout. User must input manually: $verificationId")
                activeVerificationId = verificationId
            }
        }

        try {
            val optionsBuilder = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(e164PhoneNumber)
                .setTimeout(SMS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .setCallbacks(callbacks)

            if (activity != null) {
                optionsBuilder.setActivity(activity)
            }

            if (forceResend && activeResendToken != null) {
                optionsBuilder.setForceResendingToken(activeResendToken!!)
            }

            PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Exception initiating phone verification", e)
            val (msg, type) = mapExceptionToArabic(e)
            _authState.value = PhoneAuthState.Error(msg, type)
            onError?.invoke(msg)
        }
    }

    /**
     * Verifies the 6-digit SMS OTP against Firebase Authentication.
     */
    suspend fun verifyOtpCode(
        verificationId: String?,
        code: String,
        targetPhone: String
    ): Result<FirebaseUser> {
        val targetVerificationId = verificationId ?: activeVerificationId
        if (targetVerificationId.isNullOrBlank()) {
            val err = "لم يتم العثور على جلسة تحقق صالحة. يرجى طلب رمز جديد."
            _authState.value = PhoneAuthState.Error(err, PhoneAuthErrorType.CODE_EXPIRED)
            return Result.failure(IllegalStateException(err))
        }

        val cleanCode = code.trim().replace(" ", "")
        if (cleanCode.length != 6) {
            val err = "يرجى إدخال رمز التحقق المكون من 6 أرقام."
            _authState.value = PhoneAuthState.Error(err, PhoneAuthErrorType.INVALID_SMS_CODE)
            return Result.failure(IllegalArgumentException(err))
        }

        _authState.value = PhoneAuthState.Verifying(cleanCode)

        return try {
            val credential = PhoneAuthProvider.getCredential(targetVerificationId, cleanCode)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                _authState.value = PhoneAuthState.Verified(user, targetPhone)
                Result.success(user)
            } else {
                val err = "تعذر تسجيل الدخول بالرقم. حاول مجدداً."
                _authState.value = PhoneAuthState.Error(err, PhoneAuthErrorType.GENERIC)
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify OTP code", e)
            val (msg, type) = mapExceptionToArabic(e)
            _authState.value = PhoneAuthState.Error(msg, type)
            Result.failure(Exception(msg, e))
        }
    }

    private fun signInWithCredentialInternal(
        credential: PhoneAuthCredential,
        phone: String,
        callback: (Result<FirebaseUser>) -> Unit
    ) {
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    callback(Result.success(user))
                } else {
                    callback(Result.failure(Exception("لم يتم العثور على حساب مستخدم")))
                }
            }
            .addOnFailureListener { err ->
                callback(Result.failure(err))
            }
    }

    fun resetState() {
        _authState.value = PhoneAuthState.Idle
    }

    fun getActiveVerificationId(): String? = activeVerificationId

    fun getActiveResendToken(): PhoneAuthProvider.ForceResendingToken? = activeResendToken

    private fun mapExceptionToArabic(e: Throwable): Pair<String, PhoneAuthErrorType> {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> {
                val msg = e.message?.lowercase() ?: ""
                if (msg.contains("sms") || msg.contains("code") || msg.contains("verification")) {
                    Pair("رمز التحقق غير صحيح. يرجى التحقق من الرسالة النصية وإعادة المحاولة.", PhoneAuthErrorType.INVALID_SMS_CODE)
                } else {
                    Pair("رقم الهاتف غير صحيح أو غير متوافق مع صيغة E.164 الدولية (+9639XXXXXXXX).", PhoneAuthErrorType.INVALID_PHONE_NUMBER)
                }
            }
            is FirebaseTooManyRequestsException -> {
                Pair("تم تجاوز عدد المحاولات المسموح بها. لحمايتك تم حظر الطلبات مؤقتاً، يرجى المحاولة بعد قليل.", PhoneAuthErrorType.QUOTA_EXCEEDED)
            }
            is FirebaseNetworkException -> {
                Pair("تعذر الاتصال بالشبكة. يرجى التحقق من اتصالك بالإنترنت والمحاولة مجدداً.", PhoneAuthErrorType.NETWORK_ERROR)
            }
            is FirebaseAuthUserCollisionException -> {
                Pair("رقم الهاتف مرتبط بحساب آخر بالفعل.", PhoneAuthErrorType.ALREADY_LINKED)
            }
            else -> {
                val rawMsg = e.localizedMessage ?: ""
                if (rawMsg.contains("quota", ignoreCase = true) || rawMsg.contains("blocked", ignoreCase = true)) {
                    Pair("تم تجاوز حصة الرسائل القصيرة SMS لهذا الرقم حالياً. حاول لاحقاً.", PhoneAuthErrorType.QUOTA_EXCEEDED)
                } else if (rawMsg.contains("format", ignoreCase = true) || rawMsg.contains("invalid", ignoreCase = true)) {
                    Pair("يرجى إدخال رقم هاتف صحيح بصيغة +9639XXXXXXXX", PhoneAuthErrorType.INVALID_PHONE_NUMBER)
                } else {
                    Pair("تعذر إرسال أو تأكيد رمز التحقق عبر SMS. يرجى المحاولة مرة أخرى.", PhoneAuthErrorType.GENERIC)
                }
            }
        }
    }
}
