package com.indoone.lobby.accounts

/**
 * An authenticator account kept in Trash for up to 30 days.
 */
data class TrashRecord(
    val account: AccountRecord,
    val deletedAt: Long,
    val purgeAt: Long,
)
