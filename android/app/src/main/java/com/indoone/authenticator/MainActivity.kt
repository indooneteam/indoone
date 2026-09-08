package com.indoone.authenticator

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.setContent
import com.indoone.authenticator.auth.login.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setContent {
            LoginScreen(
                onCreateAccount = { /* Signup screen will be wired next. */ },
                onForgotPassword = { /* Forgot password screen will be wired next. */ },
            )
        }
    }
}
