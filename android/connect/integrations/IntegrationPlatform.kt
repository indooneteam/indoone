package com.indoone.connect.integrations

enum class IntegrationPlatform(
    val key: String,
    val title: String,
    val description: String,
    val accountLabel: String,
) {
    INSTAGRAM(
        key = "instagram",
        title = "Instagram",
        description = "Connect a professional Instagram account for comments, DMs, posts and Reels automation.",
        accountLabel = "Instagram professional account",
    ),
    WHATSAPP(
        key = "whatsapp",
        title = "WhatsApp",
        description = "Connect a WhatsApp Business account for customer messages and automated replies.",
        accountLabel = "WhatsApp Business account",
    ),
}
