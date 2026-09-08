package com.indoone.accounts.search

import androidx.lifecycle.ViewModel
import com.indoone.accounts.AccountItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Owns search query state and filters by account name or identifier.
 */
class SearchViewModel : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    fun setAccounts(accounts: List<AccountItem>) {
        _state.value = _state.value.copy(accounts = accounts)
    }

    fun updateQuery(value: String) {
        _state.value = _state.value.copy(query = value)
    }

    fun clear() {
        _state.value = _state.value.copy(query = "")
    }
}
