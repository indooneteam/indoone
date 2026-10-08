package com.indoone.notifications

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val title: String,
    val body: String,
    val route: String?,
    val receivedAt: Long,
    val isRead: Boolean = false,
)
