package com.indoone.connect.integrations

interface IntegrationApi {
    suspend fun listAccounts(platform: IntegrationPlatform): List<IntegrationAccount>

    suspend fun startConnection(
        platform: IntegrationPlatform,
        redirectUri: String,
    ): IntegrationConnectionIntent

    suspend fun disconnect(accountId: String)
}

sealed interface IntegrationConnectionIntent {
    data class BrowserAuth(val authorizationUrl: String) : IntegrationConnectionIntent

    data class Completed(val account: IntegrationAccount) : IntegrationConnectionIntent
}
