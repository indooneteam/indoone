package com.indoone.accounts.addaccount.scanqr.accountdetails

import com.indoone.accounts.AccountRecord
import java.util.UUID

/**
 * Converts validated account details into the storage model.
 */
object AccountRecordMapper {
    fun from(request: AccountSaveRequest, now: Long = System.currentTimeMillis()): AccountRecord {
        return AccountRecord(
            id = UUID.randomUUID().toString(),
            name = request.name,
            email = request.email,
            secret = request.secret,
            digits = request.digits,
            period = request.period,
            algorithm = request.algorithm,
            createdAt = now,
            updatedAt = now,
        )
    }
}
