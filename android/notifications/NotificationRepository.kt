package com.indoone.notifications

import android.content.Context
import kotlinx.coroutines.flow.Flow

class NotificationRepository(context: Context) {
    private val dao = NotificationDatabase.getInstance(context).notificationDao()

    fun observeAll(): Flow<List<NotificationEntity>> = dao.observeAll()
    fun observeUnreadCount(): Flow<Int> = dao.observeUnreadCount()

    suspend fun add(
        category: NotificationCategory,
        title: String,
        body: String,
        route: String?,
        receivedAt: Long = System.currentTimeMillis(),
    ): Long {
        val id = dao.insert(
            NotificationEntity(
                category = category.id,
                title = title,
                body = body,
                route = route,
                receivedAt = receivedAt,
                isRead = false,
            ),
        )
        dao.deleteBeyondLatest100()
        return id
    }

    suspend fun markRead(id: Long) = dao.markRead(id)
    suspend fun markAllRead() = dao.markAllRead()
}
