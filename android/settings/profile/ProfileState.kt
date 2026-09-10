package com.indoone.settings.profile

data class ProfileState(
    val email: String = "Email not available",
    val mobile: String = "Mobile number not set",
    val loading: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)
