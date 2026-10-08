package com.indoone.notifications

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NotificationRepository private constructor(context: Context) {
    private val helper = NotificationDatabaseHelper(context.applicationContext)
    private val mutationMutex = Mutex()
    private val _notifications = MutableStateFlow(loadNotifications())

    fun observeAll(): StateFlow<List<NotificationEntity>> = _notifications

    fun observeUnreadCount(): Flow<Int> = _notifications.map { list ->
        list.count { notification -> !notification.isRead }
    }

    suspend fun add(
        category: NotificationCategory,
        title: String,
        body: String,
        route: String?,
        receivedAt: Long = System.currentTimeMillis(),
    ): Long = mutationMutex.withLock {
        withContext(Dispatchers.IO) {
            val db = helper.writableDatabase
            val values = android.content.ContentValues().apply {
                put(COL_CATEGORY, category.id)
                put(COL_TITLE, title)
                put(COL_BODY, body)
                put(COL_ROUTE, route)
                put(COL_RECEIVED_AT, receivedAt)
                put(COL_IS_READ, 0)
            }
            val id = db.insertOrThrow(TABLE_NAME, null, values)
            db.delete(
                TABLE_NAME,
                "$COL_ID NOT IN (SELECT $COL_ID FROM $TABLE_NAME ORDER BY $COL_RECEIVED_AT DESC LIMIT 100)",
                null,
            )
            _notifications.value = loadNotifications(db)
            id
        }
    }

    suspend fun markRead(id: Long) = mutationMutex.withLock {
        withContext(Dispatchers.IO) {
            helper.writableDatabase.update(
                TABLE_NAME,
                android.content.ContentValues().apply { put(COL_IS_READ, 1) },
                "$COL_ID = ?",
                arrayOf(id.toString()),
            )
            _notifications.value = loadNotifications()
        }
    }

    suspend fun markAllRead() = mutationMutex.withLock {
        withContext(Dispatchers.IO) {
            helper.writableDatabase.update(
                TABLE_NAME,
                android.content.ContentValues().apply { put(COL_IS_READ, 1) },
                "$COL_IS_READ = 0",
                null,
            )
            _notifications.value = loadNotifications()
        }
    }

    private fun loadNotifications(db: SQLiteDatabase = helper.readableDatabase): List<NotificationEntity> {
        val notifications = ArrayList<NotificationEntity>()
        db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "$COL_RECEIVED_AT DESC, $COL_ID DESC",
        ).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COL_ID)
            val categoryIndex = cursor.getColumnIndexOrThrow(COL_CATEGORY)
            val titleIndex = cursor.getColumnIndexOrThrow(COL_TITLE)
            val bodyIndex = cursor.getColumnIndexOrThrow(COL_BODY)
            val routeIndex = cursor.getColumnIndexOrThrow(COL_ROUTE)
            val receivedAtIndex = cursor.getColumnIndexOrThrow(COL_RECEIVED_AT)
            val isReadIndex = cursor.getColumnIndexOrThrow(COL_IS_READ)
            while (cursor.moveToNext()) {
                notifications += NotificationEntity(
                    id = cursor.getLong(idIndex),
                    category = cursor.getString(categoryIndex),
                    title = cursor.getString(titleIndex),
                    body = cursor.getString(bodyIndex),
                    route = cursor.getString(routeIndex),
                    receivedAt = cursor.getLong(receivedAtIndex),
                    isRead = cursor.getInt(isReadIndex) != 0,
                )
            }
        }
        return notifications
    }

    companion object {
        private const val DATABASE_NAME = "indoone_notification_history.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "notifications"
        private const val COL_ID = "id"
        private const val COL_CATEGORY = "category"
        private const val COL_TITLE = "title"
        private const val COL_BODY = "body"
        private const val COL_ROUTE = "route"
        private const val COL_RECEIVED_AT = "received_at"
        private const val COL_IS_READ = "is_read"

        @Volatile
        private var instance: NotificationRepository? = null

        fun getInstance(context: Context): NotificationRepository =
            instance ?: synchronized(this) {
                instance ?: NotificationRepository(context).also { instance = it }
            }
    }
}

private class NotificationDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "indoone_notification_history.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "category TEXT NOT NULL," +
                "title TEXT NOT NULL," +
                "body TEXT NOT NULL," +
                "route TEXT," +
                "received_at INTEGER NOT NULL," +
                "is_read INTEGER NOT NULL DEFAULT 0" +
                ")"
        )
        db.execSQL("CREATE INDEX idx_notifications_received_at ON notifications(received_at DESC)")
        db.execSQL("CREATE INDEX idx_notifications_is_read ON notifications(is_read)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}
