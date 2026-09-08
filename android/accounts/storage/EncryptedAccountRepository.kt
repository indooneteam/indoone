package com.indoone.accounts.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.indoone.accounts.AccountRecord
import com.indoone.accounts.AccountRepository
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
 *
 * The complete account payload is encrypted before it is written to the app's
 * private files directory, so TOTP secrets are not stored in plaintext.
 */
class EncryptedAccountRepository(
    context: Context,
) : AccountRepository {
    private val file = File(context.filesDir, FILE_NAME)

    @Synchronized
    override suspend fun save(account: AccountRecord) {
        val accounts = getAllInternal().toMutableList()
        val index = accounts.indexOfFirst { it.id == account.id }

        if (index >= 0) {
            accounts[index] = account
        } else {
            accounts += account
        }

        writeEncrypted(encode(accounts))
    }

    @Synchronized
    override suspend fun getAll(): List<AccountRecord> {
        return getAllInternal()
    }

    private fun getAllInternal(): List<AccountRecord> {
        if (!file.exists()) {
            return emptyList()
        }

        return runCatching {
            val plaintext = decrypt(file.readBytes())
            decode(plaintext)
        }.getOrElse {
            emptyList()
        }
    }

    private fun writeEncrypted(plaintext: ByteArray) {
        file.writeBytes(encrypt(plaintext))
    }

    private fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)

        return iv + ciphertext
    }

    private fun decrypt(payload: ByteArray): ByteArray {
        require(payload.size > GCM_IV_LENGTH) {
            "Encrypted account data is invalid."
        }

        val iv = payload.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = payload.copyOfRange(GCM_IV_LENGTH, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(GCM_TAG_LENGTH, iv),
        )

        return cipher.doFinal(ciphertext)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE,
        )

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

        accounts.forEach { account ->
            json.put(
                JSONObject()
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
                    .put("updatedAt", account.updatedAt),
            )
        }

        return json.toString().toByteArray(StandardCharsets.UTF_8)
    }

    private fun decode(plaintext: ByteArray): List<AccountRecord> {
        val json = JSONArray(String(plaintext, StandardCharsets.UTF_8))

        return buildList(json.length()) {
            for (index in 0 until json.length()) {
                val item = json.getJSONObject(index)

                add(
                    AccountRecord(
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
                    ),
                )
            }
        }
    }

    private companion object {
        const val FILE_NAME = "accounts.enc"
        const val KEY_ALIAS = "indoone.accounts.aes"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_LENGTH = 12
        const val GCM_TAG_LENGTH = 128
    }
}
