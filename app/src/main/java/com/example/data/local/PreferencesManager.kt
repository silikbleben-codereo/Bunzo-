package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.User
import com.example.data.model.UserRole

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bunzo_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_AUTH_TOKEN = "key_auth_token"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_FIRST_NAME = "key_first_name"
        private const val KEY_LAST_NAME = "key_last_name"
        private const val KEY_PHONE = "key_phone"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_ROLE = "key_role"
        private const val KEY_PHONE_VERIFIED = "key_phone_verified"
        private const val KEY_REGION = "key_region"
        private const val KEY_ADDRESS = "key_address"
        private const val KEY_DARK_MODE = "key_dark_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "key_notif_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_AUTO_REFRESH = "key_auto_refresh"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
    }

    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()

    val isLoggedIn: Boolean
        get() = !authToken.isNullOrBlank() || !prefs.getString(KEY_USER_ID, null).isNullOrBlank()

    fun saveUser(user: User) {
        prefs.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_FIRST_NAME, user.firstName)
            putString(KEY_LAST_NAME, user.lastName)
            putString(KEY_PHONE, user.phone)
            putString(KEY_EMAIL, user.email)
            putString(KEY_ROLE, user.role.roleKey)
            putBoolean(KEY_PHONE_VERIFIED, user.phoneVerified)
            putString(KEY_REGION, user.region)
            putString(KEY_ADDRESS, user.address)
            if (user.token != null) {
                putString(KEY_AUTH_TOKEN, user.token)
            }
        }.apply()
    }

    fun getUser(): User? {
        val uid = prefs.getString(KEY_USER_ID, null) ?: return null
        val phone = prefs.getString(KEY_PHONE, "") ?: ""
        return User(
            id = uid,
            firstName = prefs.getString(KEY_FIRST_NAME, "") ?: "",
            lastName = prefs.getString(KEY_LAST_NAME, "") ?: "",
            phone = phone,
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            role = UserRole.fromKey(prefs.getString(KEY_ROLE, "customer")),
            phoneVerified = prefs.getBoolean(KEY_PHONE_VERIFIED, false),
            country = "Syria",
            region = prefs.getString(KEY_REGION, "دمشق") ?: "دمشق",
            address = prefs.getString(KEY_ADDRESS, "") ?: "",
            token = authToken
        )
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_ROLE)
            .remove(KEY_PHONE_VERIFIED)
            .apply()
    }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "ar") ?: "ar"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var isAutoRefreshEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_REFRESH, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_REFRESH, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()
}
