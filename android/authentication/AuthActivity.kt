package com.indoone.authentication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indoone.MainActivity
import com.indoone.authentication.login.LoginScreen
import com.indoone.authentication.signup.SignUpScreen
import kotlinx.coroutines.launch

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = AuthSessionStore(applicationContext)
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null && session.isOtpVerified()) {
            openMain()
            return
        }

        setContent {
            MaterialTheme {
                var showingSignup by remember { mutableStateOf(false) }
                var busy by remember { mutableStateOf(false) }
                var otpVisible by remember { mutableStateOf(false) }
                var status by remember { mutableStateOf("") }
                var error by remember { mutableStateOf("") }
                val scope = rememberCoroutineScope()
                val service = remember { IndooneAuthService(auth, FirebaseDatabase.getInstance()) }

                LaunchedEffect(showingSignup) {
                    busy = false
                    otpVisible = false
                    status = ""
                    error = ""
                }

                if (showingSignup) {
                    SignUpScreen(
                        busy = busy,
                        status = status,
                        error = error,
                        otpVisible = otpVisible,
                        onSendOtp = { email, mobile, password ->
                            busy = true
                            error = ""
                            scope.launch {
                                runCatching { service.startSignup(email, mobile, password) }
                                    .onSuccess {
                                        otpVisible = true
                                        status = "OTP sent. Check your email."
                                    }
                                    .onFailure { error = it.message ?: "Could not start signup." }
                                busy = false
                            }
                        },
                        onVerifyOtp = { otp ->
                            busy = true
                            error = ""
                            scope.launch {
                                runCatching { service.verifySignupOtp(otp) }
                                    .onSuccess { uid ->
                                        session.setVerified(uid)
                                        openMain()
                                    }
                                    .onFailure { error = it.message ?: "Could not create account." }
                                busy = false
                            }
                        },
                        onResendOtp = {
                            busy = true
                            error = ""
                            scope.launch {
                                runCatching { service.resendSignupOtp() }
                                    .onSuccess { status = "New OTP sent. Check your email." }
                                    .onFailure { error = it.message ?: "Could not resend OTP." }
                                busy = false
                            }
                        },
                        onLogin = { showingSignup = false },
                    )
                } else {
                    LoginScreen(
                        busy = busy,
                        status = status,
                        error = error,
                        otpVisible = otpVisible,
                        onSendOtp = { identifier, password ->
                            busy = true
                            error = ""
                            scope.launch {
                                runCatching { service.login(identifier, password) }
                                    .onSuccess {
                                        otpVisible = true
                                        status = "OTP sent. Check your email."
                                    }
                                    .onFailure { error = it.message ?: "Login failed." }
                                busy = false
                            }
                        },
                        onVerifyOtp = { otp ->
                            busy = true
                            error = ""
                            scope.launch {
                                runCatching { service.verifyLoginOtp(otp) }
                                    .onSuccess {
                                        auth.currentUser?.uid?.let(session::setVerified)
                                        openMain()
                                    }
                                    .onFailure { error = it.message ?: "Login failed." }
                                busy = false
                            }
                        },
                        onResendOtp = {
                            busy = true
                            error = ""
                            scope.launch {
                                runCatching { service.resendLoginOtp() }
                                    .onSuccess { status = "New OTP sent. Check your email." }
                                    .onFailure { error = it.message ?: "Could not resend OTP." }
                                busy = false
                            }
                        },
                        onCreateAccount = { showingSignup = true },
                    )
                }
            }
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
