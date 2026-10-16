package com.indoone.lobby.accounts.addaccount.scanqr.accountdetails

import com.indoone.lobby.accounts.AccountRecord
import com.indoone.lobby.accounts.AccountRepository

/**
 * Validates account details, maps them to the storage model, and persists them.
 */
class AccountSaveCoordinator(
    private val repository: AccountRepository,
) {
    suspend fun save(state: AccountDetailsState): Result<AccountRecord> {
        val request = AccountDetailsValidator.validate(state).getOrElse { error ->
            return Result.failure(error)
        }

        val record = AccountRecordMapper.from(request)

        return try {
            repository.save(record)
            Result.success(record)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
