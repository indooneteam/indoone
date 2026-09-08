package com.indoone.menu.dangerzone

import com.google.firebase.auth.FirebaseAuthException

val Exception.code: String
    get() = (this as? FirebaseAuthException)?.errorCode.orEmpty()
