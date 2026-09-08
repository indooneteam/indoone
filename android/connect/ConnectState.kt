package com.indoone.connect

data class ConnectState(
    val eyebrow: String = "NEARBY DEVICE SHARING",
    val title: String = "Connect",
    val description: String = "Pair nearby devices, share files and control exactly what each device can access.",
    val actionsComingSoon: Boolean = true,
)
