package com.indoone.authentication.forgotpassword

data class ForgotPasswordState(
    val identifier: String = "",
    val otp: String = "",
    val otpSent: Boolean = false,
    val loading: Boolean = false,
    val errorMessage: String? = null,
)
