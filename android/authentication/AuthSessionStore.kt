package com.indoone.authentication

import android.content.Context

class AuthSessionStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun isOtpVerified(): Boolean = preferences.getBoolean(KEY_OTP_VERIFIED, false)

    fun setVerified(uid: String) {
        preferences.edit()
            .putBoolean(KEY_OTP_VERIFIED, true)
            .putString(KEY_UID, uid)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val PREFERENCES = "indoone_auth_session"
        private const val KEY_OTP_VERIFIED = "otp_verified"
        private const val KEY_UID = "uid"
    }
}
