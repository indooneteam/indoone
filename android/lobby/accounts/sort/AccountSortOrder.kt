package com.indoone.lobby.accounts.sort

enum class AccountSortOrder {
    ASCENDING,
    DESCENDING;

    fun toggled(): AccountSortOrder = when (this) {
        ASCENDING -> DESCENDING
        DESCENDING -> ASCENDING
    }
}
