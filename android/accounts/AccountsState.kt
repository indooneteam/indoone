package com.indoone.accounts

import androidx.compose.runtime.Immutable
import com.indoone.accounts.sort.AccountSortOrder

@Immutable
data class AccountItem(
    val id: String,
    val name: String,
    val email: String,
    val code: String,
    val secondsRemaining: Int,
    val periodSeconds: Int = 30,
    val favorite: Boolean = false,
    val icon: String = "",
    val serviceClass: String = "google",
)

@Immutable
data class AccountsState(
    val accounts: List<AccountItem> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: AccountSortOrder = AccountSortOrder.DESCENDING,
) {
    val sortAscending: Boolean
        get() = sortOrder == AccountSortOrder.ASCENDING

    val filteredAccounts: List<AccountItem>
        get() {
            val query = searchQuery.trim()
            val filtered = if (query.isEmpty()) {
                accounts
            } else {
                // Main searches by account/service name only, not email.
                accounts.filter { account ->
                    account.name.contains(query, ignoreCase = true)
                }
            }

            return when (sortOrder) {
                AccountSortOrder.ASCENDING -> filtered.sortedBy { it.name.lowercase() }
                AccountSortOrder.DESCENDING -> filtered.sortedByDescending { it.name.lowercase() }
            }
        }
}
