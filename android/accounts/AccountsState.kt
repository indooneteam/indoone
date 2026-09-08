package com.indoone.accounts

import androidx.compose.runtime.Immutable

@Immutable
data class AccountItem(
    val id: String,
    val name: String,
    val email: String,
    val code: String,
    val secondsRemaining: Int,
    val periodSeconds: Int = 30,
    val favorite: Boolean = false,
)

@Immutable
data class AccountsState(
    val accounts: List<AccountItem> = emptyList(),
    val searchQuery: String = "",
    val sortAscending: Boolean = true,
) {
    val filteredAccounts: List<AccountItem>
        get() {
            val query = searchQuery.trim()
            val filtered = if (query.isEmpty()) {
                accounts
            } else {
                accounts.filter { account ->
                    account.name.contains(query, ignoreCase = true) ||
                        account.email.contains(query, ignoreCase = true)
                }
            }

            return if (sortAscending) {
                filtered.sortedBy { it.name.lowercase() }
            } else {
                filtered.sortedByDescending { it.name.lowercase() }
            }
        }
}
