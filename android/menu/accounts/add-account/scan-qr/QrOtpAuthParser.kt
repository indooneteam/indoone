package com.indoone.accounts.addaccount.scanqr

import android.net.Uri
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Converts a scanned otpauth:// URI into a UI-independent account result.
 */
object QrOtpAuthParser {
    fun parse(rawValue: String): Result<QrAccountResult> {
        val value = rawValue.trim()
        val uri = Uri.parse(value)

        if (!uri.scheme.equals("otpauth", ignoreCase = true)) {
            return Result.failure(
                IllegalArgumentException("QR code is not an OTPAuth URI."),
            )
        }

        if (!uri.host.equals("totp", ignoreCase = true)) {
            return Result.failure(
                IllegalArgumentException("Only TOTP QR codes are supported."),
            )
        }

        val secret = uri.getQueryParameter("secret")
            ?.replace(" ", "")
            ?.replace("-", "")
            ?.uppercase()
            .orEmpty()

        if (secret.isBlank()) {
            return Result.failure(
                IllegalArgumentException("TOTP secret is missing."),
            )
        }

        val label = decode(uri.path?.removePrefix("/").orEmpty())
        val issuer = uri.getQueryParameter("issuer")?.trim().orEmpty()
        val parts = label.split(":", limit = 2)
        val labelIssuer = parts.firstOrNull()?.trim().orEmpty()
        val account = if (parts.size == 2) parts[1].trim() else label
        val name = issuer.ifBlank {
            labelIssuer.ifBlank { "Account" }
        }

        val algorithm = uri.getQueryParameter("algorithm")
            ?.trim()
            ?.uppercase()
            ?.ifBlank { "SHA1" }
            ?: "SHA1"

        val digits = uri.getQueryParameter("digits")
            ?.toIntOrNull()
            ?.takeIf { it == 6 || it == 8 }
            ?: 6

        val period = uri.getQueryParameter("period")
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
            ?: 30

        return Result.success(
            QrAccountResult(
                name = name,
                email = account,
                secret = secret,
                issuer = issuer,
                algorithm = algorithm,
                digits = digits,
                period = period,
            ),
        )
    }

    private fun decode(value: String): String {
        return try {
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        } catch (_: IllegalArgumentException) {
            value
        }
    }
}
