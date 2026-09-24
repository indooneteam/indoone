plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.indoone"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.indoone.authenticator"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        val channel = if (System.getenv("GITHUB_REF_NAME") == "develop") "develop" else "main"
        val backendUrl = System.getenv("INDOONE_BACKEND_URL") ?: "https://indoone-backend.onrender.com"
        buildConfigField("String", "INDOONE_CHANNEL", "\"$channel\"")
        buildConfigField("String", "INDOONE_BACKEND_URL", "\"$backendUrl\"")
    }

    signingConfigs {
        val developKeystorePath = System.getenv("INDOONE_KEYSTORE_PATH")
        val developKeystorePassword = System.getenv("INDOONE_KEYSTORE_PASSWORD")
        val developKeyAlias = System.getenv("INDOONE_KEY_ALIAS")
        val developKeyPassword = System.getenv("INDOONE_KEY_PASSWORD")

        if (
            System.getenv("GITHUB_REF_NAME") == "develop" &&
            !developKeystorePath.isNullOrBlank() &&
            !developKeystorePassword.isNullOrBlank() &&
            !developKeyAlias.isNullOrBlank() &&
            !developKeyPassword.isNullOrBlank()
        ) {
            create("develop") {
                storeFile = file(developKeystorePath)
                storePassword = developKeystorePassword
                keyAlias = developKeyAlias
                keyPassword = developKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            if (System.getenv("GITHUB_REF_NAME") == "develop" && signingConfigs.findByName("develop") != null) {
                signingConfig = signingConfigs.getByName("develop")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets["main"].kotlin.setSrcDirs(
        listOf(
            "AccountRecordMapper.kt",
            "IndooneApplication.kt",
            "MainActivity.kt",
            "authentication",
            "accounts",
            "menu",
            "home",
            "lobby",
            "connect",
            "settings"
        )
    )
    sourceSets["main"].manifest.srcFile(file("AndroidManifest.xml"))

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "DebugProbesKt.bin"
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.camera:camera-core:1.6.2")
    implementation("androidx.camera:camera-camera2:1.6.2")
    implementation("androidx.camera:camera-lifecycle:1.6.2")
    implementation("androidx.camera:camera-view:1.6.2")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("androidx.biometric:biometric:1.1.0")
}
