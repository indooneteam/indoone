package com.indoone.authenticator.auth.login

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed interface LoginResult {
    data object Success : LoginResult
    data class Failure(val message: String) : LoginResult
}

class FirebaseAuthRepository(private val context: Context) {
    suspend fun signInWithEmailAndPassword(email: String, password: String): LoginResult {
        if (FirebaseApp.getApps(context).isEmpty()) {
            return LoginResult.Failure(
                "Firebase is not configured for this build yet."
            )
        }

        return suspendCoroutine { continuation ->
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    continuation.resume(LoginResult.Success)
                }
                .addOnFailureListener { error ->
                    continuation.resume(
                        LoginResult.Failure(error.message ?: "Unable to sign in.")
                    )
                }
        }
    }
}
