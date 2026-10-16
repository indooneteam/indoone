package com.indoone

import com.indoone.lobby.accounts.AccountRecord
import com.indoone.lobby.accounts.addaccount.scanqr.accountdetails.AccountSaveRequest

object AccountRecordMapper {
    fun from(request: AccountSaveRequest, now: Long = System.currentTimeMillis()): AccountRecord {
        return com.indoone.lobby.accounts.addaccount.scanqr.accountdetails.AccountRecordMapper.from(request, now)
    }
}
