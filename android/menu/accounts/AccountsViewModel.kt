package com.indoone.accounts

import androidx.lifecycle.ViewModel
import com.indoone.accounts.sort.AccountSortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountsViewModel : ViewModel() {
    private val _state = MutableStateFlow(AccountsState())
    val state: StateFlow<AccountsState> = _state.asStateFlow()

    fun setAccounts(accounts: List<AccountItem>) {
        _state.value = _state.value.copy(accounts = accounts)
    }

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun clearSearch() {
        _state.value = _state.value.copy(searchQuery = "")
    }

    fun toggleSort() {
        _state.value = _state.value.copy(
            sortOrder = _state.value.sortOrder.toggled(),
        )
    }

    fun toggleFavorite(accountId: String) {
        _state.value = _state.value.copy(
            accounts = _state.value.accounts.map { account ->
                if (account.id == accountId) {
                    account.copy(favorite = !account.favorite)
                } else {
                    account
                }
            },
        )
    }
}
