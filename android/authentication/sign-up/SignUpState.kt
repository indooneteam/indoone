package com.indoone.authentication.signup

data class SignUpState(
    val name: String = "",
    val identifier: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val errorMessage: String? = null,
)
