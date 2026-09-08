package com.indoone.authentication.login

data class LoginState(
    val identifier: String = "",
    val password: String = "",
    val otp: String = "",
    val otpSent: Boolean = false,
    val loading: Boolean = false,
    val errorMessage: String? = null,
)
