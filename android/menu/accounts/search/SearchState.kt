package com.indoone.accounts.search

import com.indoone.accounts.AccountItem

/**
 * State for the dedicated account search surface.
 */
data class SearchState(
    val query: String = "",
    val accounts: List<AccountItem> = emptyList(),
) {
    val filteredAccounts: List<AccountItem>
        get() {
            val normalized = query.trim()
            if (normalized.isEmpty()) return accounts

            return accounts.filter { account ->
                account.name.contains(normalized, ignoreCase = true) ||
                    account.email.contains(normalized, ignoreCase = true)
            }
        }
}
