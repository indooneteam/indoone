package com.indoone.notifications

data class NotificationEntity(
    val id: Long = 0,
    val category: String,
    val title: String,
    val body: String,
    val route: String?,
    val receivedAt: Long,
    val isRead: Boolean = false,
)
