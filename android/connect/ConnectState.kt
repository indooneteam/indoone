package com.indoone.connect

data class ConnectState(
    val eyebrow: String = "BUSINESS INTEGRATIONS",
    val title: String = "Connect",
    val description: String = "Connect your business apps and let Indoone AI handle customer requests across your services.",
    val actionsComingSoon: Boolean = false,
    val balance: String = "₹0.00",
    val usage: String = "0",
    val usageLimit: String = "10,000",
)
