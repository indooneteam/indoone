package com.indoone.accounts.accounts.list

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Generates RFC 6238 compatible TOTP codes for stored account records.
 */
object AccountTotpGenerator {
    fun generate(
        secret: String,
        timeMillis: Long,
        periodSeconds: Int,
        digits: Int,
        algorithm: String,
    ): String {
        require(periodSeconds > 0) { "TOTP period must be positive." }
        require(digits == 6 || digits == 8) { "TOTP digits must be 6 or 8." }

        val normalizedSecret = secret
            .replace(" ", "")
            .replace("-", "")
            .uppercase()

        val key = Base32.decode(normalizedSecret)
        require(key.isNotEmpty()) { "TOTP secret is empty." }

        val counter = timeMillis / 1000L / periodSeconds
        val message = ByteArray(8)
        for (index in 7 downTo 0) {
            message[index] = (counter ushr ((7 - index) * 8)).toByte()
        }

        val mac = Mac.getInstance(toJcaAlgorithm(algorithm))
        mac.init(SecretKeySpec(key, mac.algorithm))
        val digest = mac.doFinal(message)

        val offset = digest[digest.lastIndex].toInt() and 0x0F
        val binary = ((digest[offset].toInt() and 0x7F) shl 24) or
            ((digest[offset + 1].toInt() and 0xFF) shl 16) or
            ((digest[offset + 2].toInt() and 0xFF) shl 8) or
            (digest[offset + 3].toInt() and 0xFF)

        val modulus = if (digits == 8) 100_000_000 else 1_000_000
        return (binary % modulus).toString().padStart(digits, '0')
    }

    private fun toJcaAlgorithm(algorithm: String): String {
        return when (algorithm.uppercase()) {
            "SHA256", "SHA-256" -> "HmacSHA256"
            "SHA512", "SHA-512" -> "HmacSHA512"
            else -> "HmacSHA1"
        }
    }
}

private object Base32 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun decode(input: String): ByteArray {
        val clean = input.trim().replace("=", "").uppercase()
        require(clean.isNotEmpty()) { "Invalid Base32 secret." }

        var buffer = 0
        var bitsLeft = 0
        val output = ArrayList<Byte>((clean.length * 5) / 8)

        for (character in clean) {
            val value = ALPHABET.indexOf(character)
            require(value >= 0) { "Invalid Base32 secret." }

            buffer = (buffer shl 5) or value
            bitsLeft += 5

            while (bitsLeft >= 8) {
                bitsLeft -= 8
                output += ((buffer ushr bitsLeft) and 0xFF).toByte()
            }
        }

        return output.toByteArray()
    }
}
