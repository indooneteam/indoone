package com.indoone

import com.indoone.accounts.AccountRecord
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountSaveRequest

object AccountRecordMapper {
    fun from(request: AccountSaveRequest, now: Long = System.currentTimeMillis()): AccountRecord {
        return com.indoone.accounts.addaccount.scanqr.accountdetails.AccountRecordMapper.from(request, now)
    }
}
