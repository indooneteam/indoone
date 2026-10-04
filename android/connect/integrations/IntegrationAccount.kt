package com.indoone.connect.integrations

data class IntegrationAccount(
    val id: String,
    val platform: IntegrationPlatform,
    val externalAccountId: String,
    val displayName: String,
    val username: String = "",
    val status: IntegrationAccountStatus = IntegrationAccountStatus.CONNECTED,
)

enum class IntegrationAccountStatus {
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    ERROR,
}
