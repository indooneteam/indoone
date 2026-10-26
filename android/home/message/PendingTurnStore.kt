package com.indoone.home.message

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.UUID

/**
 * The turn that has been sent but not answered yet.
 *
 * `/api/chat` answers in the same response, so a request that is cancelled when the app
 * goes to the background is a question with no answer and nothing saying why. The turn is
 * written here the moment the user sends it, with status `sending`, and the background
 * worker is what moves it on.
 *
 * Deliberately plain SQLite rather than Room. Room needs the KSP Gradle plugin, and a
 * KSP version that does not match this project's Kotlin plugin fails the build outright -
 * the same build this change has to prove itself in. There are three columns of state
 * here; a code generator is not what makes it correct. Room can replace this later
 * without changing a single caller, because everything goes through this class.
 */
class PendingTurnStore private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    data class Turn(
        val id: String,
        val conversationId: String,
        val text: String,
        val status: String,
        val reply: String?,
        val error: String?,
        val createdAt: Long,
    )

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE (
                id TEXT PRIMARY KEY,
                conversation_id TEXT NOT NULL,
                text TEXT NOT NULL,
                status TEXT NOT NULL,
                reply TEXT,
                error TEXT,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_pending_status ON $TABLE(status)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Nothing to migrate yet: version 1 is the first schema. A change here is a
        // version bump with a real migration, the same rule the backend follows.
    }

    /**
     * Record a turn as sending. The row is the source of truth from this moment, so a
     * process death leaves something behind to reconcile against.
     */
    fun begin(conversationId: String, text: String): Turn {
        val turn = Turn(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            text = text,
            status = STATUS_SENDING,
            reply = null,
            error = null,
            createdAt = System.currentTimeMillis(),
        )
        val values = ContentValues().apply {
            put("id", turn.id)
            put("conversation_id", turn.conversationId)
            put("text", turn.text)
            put("status", turn.status)
            put("created_at", turn.createdAt)
        }
        writableDatabase.insertOrThrow(TABLE, null, values)
        return turn
    }

    fun markDelivered(id: String, reply: String) {
        val values = ContentValues().apply {
            put("status", STATUS_DELIVERED)
            put("reply", reply)
            putNull("error")
        }
        writableDatabase.update(TABLE, values, "id = ?", arrayOf(id))
    }

    /**
     * Mark a turn failed. It stays in the table until the user acts on it: the failure
     * is shown in the thread with a retry, and nothing re-sends on its own.
     */
    fun markFailed(id: String, error: String) {
        val values = ContentValues().apply {
            put("status", STATUS_FAILED)
            put("error", error)
        }
        writableDatabase.update(TABLE, values, "id = ?", arrayOf(id))
    }

    fun find(id: String): Turn? = read("id = ?", arrayOf(id)).firstOrNull()

    /** A turn that was still sending when the process died, if there is one. */
    fun unfinished(): Turn? = read("status = ?", arrayOf(STATUS_SENDING)).firstOrNull()

    fun delete(id: String) {
        writableDatabase.delete(TABLE, "id = ?", arrayOf(id))
    }

    private fun read(selection: String, args: Array<String>): List<Turn> {
        val turns = mutableListOf<Turn>()
        readableDatabase.query(
            TABLE,
            arrayOf("id", "conversation_id", "text", "status", "reply", "error", "created_at"),
            selection,
            args,
            null,
            null,
            "created_at ASC",
            "20",
        ).use { cursor ->
            while (cursor.moveToNext()) {
                turns += Turn(
                    id = cursor.getString(0),
                    conversationId = cursor.getString(1),
                    text = cursor.getString(2),
                    status = cursor.getString(3),
                    reply = if (cursor.isNull(4)) null else cursor.getString(4),
                    error = if (cursor.isNull(5)) null else cursor.getString(5),
                    createdAt = cursor.getLong(6),
                )
            }
        }
        return turns
    }

    companion object {
        const val STATUS_SENDING = "sending"
        const val STATUS_FAILED = "failed"
        const val STATUS_DELIVERED = "delivered"

        private const val DB_NAME = "indoone_pending_turns.db"
        private const val DB_VERSION = 1
        private const val TABLE = "pending_turns"

        @Volatile
        private var instance: PendingTurnStore? = null

        fun get(context: Context): PendingTurnStore =
            instance ?: synchronized(this) {
                instance ?: PendingTurnStore(context).also { instance = it }
            }
    }
}
