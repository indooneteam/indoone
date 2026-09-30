package com.indoone.settings.dangerzone

import com.google.firebase.auth.FirebaseAuthException

/**
 * Keeps authentication-error formatting compatible with Firebase listener callbacks
 * whose static callback type is exposed as Exception.
 */
val Exception.code: String
    get() = (this as? FirebaseAuthException)?.errorCode.orEmpty()
