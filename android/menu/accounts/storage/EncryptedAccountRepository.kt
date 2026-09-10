package com.indoone.accounts.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.indoone.accounts.AccountRecord
import com.indoone.accounts.AccountRepository
import com.indoone.accounts.TrashRecord
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Durable local account storage with Android Keystore-backed AES/GCM encryption.
 */
class EncryptedAccountRepository(
    context: Context,
) : AccountRepository {
    private val file = File(context.filesDir, FILE_NAME)
    private val trashFile = File(context.filesDir, TRASH_FILE_NAME)

    override suspend fun save(account: AccountRecord) {
        val accounts = getAllInternal().toMutableList()
        val index = accounts.indexOfFirst { it.id == account.id }

        if (index >= 0) accounts[index] = account else accounts += account
        writeEncrypted(file, encode(accounts))
    }

    override suspend fun getAll(): List<AccountRecord> = getAllInternal()

    override suspend fun remove(id: String) {
        val accounts = getAllInternal()
        if (accounts.none { it.id == id }) return
        writeEncrypted(file, encode(accounts.filterNot { it.id == id }))
    }

    override suspend fun moveToTrash(id: String) {
        val accounts = getAllInternal()
        val account = accounts.firstOrNull { it.id == id } ?: return
        val now = System.currentTimeMillis()
        val trash = loadTrashInternal(now).filterNot { it.account.id == id }.toMutableList()
        trash += TrashRecord(account, now, now + TRASH_DURATION_MS)
        writeEncrypted(trashFile, encodeTrash(trash))
        writeEncrypted(file, encode(accounts.filterNot { it.id == id }))
    }

    override suspend fun listTrash(): List<TrashRecord> = loadTrashInternal(System.currentTimeMillis())

    override suspend fun restoreFromTrash(id: String): AccountRecord {
        val trash = loadTrashInternal(System.currentTimeMillis()).toMutableList()
        val item = trash.firstOrNull { it.account.id == id }
            ?: throw IllegalStateException("Trash account not found.")
        val accounts = getAllInternal().toMutableList()
        accounts.removeAll { it.id == id }
        accounts += item.account
        writeEncrypted(file, encode(accounts))
        writeEncrypted(trashFile, encodeTrash(trash.filterNot { it.account.id == id }))
        return item.account
    }

    override suspend fun permanentlyDeleteFromTrash(id: String) {
        val trash = loadTrashInternal(System.currentTimeMillis())
        writeEncrypted(trashFile, encodeTrash(trash.filterNot { it.account.id == id }))
    }

    private fun getAllInternal(): List<AccountRecord> {
        if (!file.exists()) return emptyList()
        return runCatching { decode(decrypt(file.readBytes())) }.getOrElse { emptyList() }
    }

    private fun loadTrashInternal(now: Long): List<TrashRecord> {
        if (!trashFile.exists()) return emptyList()
        val parsed = runCatching { decodeTrash(decrypt(trashFile.readBytes())) }.getOrElse { emptyList() }
        val active = parsed.filter { it.purgeAt > now }
        if (active.size != parsed.size) writeEncrypted(trashFile, encodeTrash(active))
        return active.sortedByDescending { it.deletedAt }
    }

    private fun writeEncrypted(target: File, plaintext: ByteArray) {
        target.writeBytes(encrypt(plaintext))
    }

    private fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return cipher.iv + cipher.doFinal(plaintext)
    }

    private fun decrypt(payload: ByteArray): ByteArray {
        require(payload.size > GCM_IV_LENGTH) { "Encrypted account data is invalid." }
        val iv = payload.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = payload.copyOfRange(GCM_IV_LENGTH, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return keyGenerator.generateKey()
    }

    private fun encode(accounts: List<AccountRecord>): ByteArray {
        val json = JSONArray()
        accounts.forEach { account -> json.put(accountJson(account)) }
        return json.toString().toByteArray(StandardCharsets.UTF_8)
    }

    private fun encodeTrash(trash: List<TrashRecord>): ByteArray {
        val json = JSONArray()
        trash.forEach { item ->
            json.put(
                JSONObject()
                    .put("account", accountJson(item.account))
                    .put("deletedAt", item.deletedAt)
                    .put("purgeAt", item.purgeAt),
            )
        }
        return json.toString().toByteArray(StandardCharsets.UTF_8)
    }

    private fun accountJson(account: AccountRecord): JSONObject = JSONObject()
        .put("id", account.id)
        .put("name", account.name)
        .put("email", account.email)
        .put("secret", account.secret)
        .put("digits", account.digits)
        .put("period", account.period)
        .put("algorithm", account.algorithm)
        .put("provider", account.provider)
        .put("service", account.service)
        .put("favorite", account.favorite)
        .put("createdAt", account.createdAt)
        .put("updatedAt", account.updatedAt)

    private fun decode(plaintext: ByteArray): List<AccountRecord> {
        val json = JSONArray(String(plaintext, StandardCharsets.UTF_8))
        return buildList(json.length()) {
            for (index in 0 until json.length()) add(decodeAccount(json.getJSONObject(index)))
        }
    }

    private fun decodeTrash(plaintext: ByteArray): List<TrashRecord> {
        val json = JSONArray(String(plaintext, StandardCharsets.UTF_8))
        return buildList(json.length()) {
            for (index in 0 until json.length()) {
                val item = json.getJSONObject(index)
                add(
                    TrashRecord(
                        account = decodeAccount(item.getJSONObject("account")),
                        deletedAt = item.optLong("deletedAt", 0L),
                        purgeAt = item.optLong("purgeAt", 0L),
                    ),
                )
            }
        }
    }

    private fun decodeAccount(item: JSONObject): AccountRecord = AccountRecord(
        id = item.getString("id"),
        name = item.getString("name"),
        email = item.optString("email"),
        secret = item.getString("secret"),
        digits = item.optInt("digits", 6),
        period = item.optInt("period", 30),
        algorithm = item.optString("algorithm", "SHA1"),
        provider = item.optString("provider"),
        service = item.optString("service"),
        favorite = item.optBoolean("favorite", false),
        createdAt = item.optLong("createdAt", 0L),
        updatedAt = item.optLong("updatedAt", 0L),
    )

    private companion object {
        const val FILE_NAME = "accounts.enc"
        const val TRASH_FILE_NAME = "trash.enc"
        const val KEY_ALIAS = "indoone.accounts.aes"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_LENGTH = 12
        const val GCM_TAG_LENGTH = 128
        const val TRASH_DURATION_MS = 30L * 24L * 60L * 60L * 1000L
    }
}
