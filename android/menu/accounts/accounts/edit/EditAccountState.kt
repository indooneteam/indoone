package com.indoone.accounts.accounts.edit

data class EditAccountState(
    val id: String,
    val name: String,
    val email: String,
    val secret: String,
    val digits: Int,
    val period: Int,
    val algorithm: String,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && secret.isNotBlank() && !isSaving
}
