
import java.net.URI
import java.util.zip.ZipFile

val prepareVoskModel = tasks.register("prepareVoskModel") {
    doLast {
        val assetRoot = file("src/main/assets")
        val modelRoot = file("src/main/assets/model-en-us")
        val uuidFile = file("src/main/assets/model-en-us/uuid")

        val requiredFiles = listOf(
            File(modelRoot, "am/final.mdl"),
            File(modelRoot, "conf/model.conf"),
            File(modelRoot, "graph/Gr.fst"),
            File(modelRoot, "graph/HCLr.fst"),
        )

        if (uuidFile.exists() && requiredFiles.all(File::exists)) {
            println("Vosk wake model already prepared.")
            return@doLast
        }

        val archive = File(temporaryDir, "vosk-model-small-en-us-0.15.zip")
        val url = URI(
            "https://alphacephei.com/vosk/models/" +
                "vosk-model-small-en-us-0.15.zip"
        ).toURL()

        val connection = url.openConnection().apply {
            connectTimeout = 30_000
            readTimeout = 120_000
        }

        connection.getInputStream().use { input ->
            archive.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        if (modelRoot.exists()) {
            modelRoot.deleteRecursively()
        }
        modelRoot.mkdirs()

        ZipFile(archive).use { zip ->
            val prefix = "vosk-model-small-en-us-0.15/"
            zip.entries().asSequence()
                .filter { !it.isDirectory && it.name.startsWith(prefix) }
                .forEach { entry ->
                    val relativePath = entry.name.removePrefix(prefix)
                    if (relativePath.isBlank()) return@forEach

                    val target = File(modelRoot, relativePath)
                    target.parentFile.mkdirs()

                    zip.getInputStream(entry).use { input ->
                        target.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
        }

        if (!uuidFile.exists()) {
            uuidFile.writeText("indoone-vosk-small-en-us-0.15")
        }

        val missing = requiredFiles.filterNot(File::exists)
        check(missing.isEmpty()) {
            "Vosk wake model download is incomplete; missing: " +
                missing.joinToString { it.relativeTo(modelRoot).path }
        }

        println("Vosk wake model prepared successfully at " + modelRoot.absolutePath)
    }
}

tasks.named("preBuild").configure {
    dependsOn(prepareVoskModel)
}


plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.kapt")
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
        versionCode = 4
        versionName = "0.1.0"

        val channel = when (System.getenv("GITHUB_REF_NAME")) {
            "develop" -> "develop"
            "terminal" -> "terminal"
            else -> "main"
        }
        val defaultBackendUrl = if (channel == "terminal") "http://127.0.0.1:8000" else "https://indoone-backend.onrender.com"
        val backendUrl = System.getenv("INDOONE_BACKEND_URL") ?: defaultBackendUrl
        buildConfigField("String", "INDOONE_CHANNEL", "\"$channel\"")
        buildConfigField("String", "INDOONE_BACKEND_URL", "\"$backendUrl\"")
    }

    signingConfigs {
        val developKeystorePath = System.getenv("INDOONE_KEYSTORE_PATH")
        val developKeystorePassword = System.getenv("INDOONE_KEYSTORE_PASSWORD")
        val developKeyAlias = System.getenv("INDOONE_KEY_ALIAS")
        val developKeyPassword = System.getenv("INDOONE_KEY_PASSWORD")

        if (
            (System.getenv("GITHUB_REF_NAME") == "develop" || System.getenv("GITHUB_REF_NAME") == "terminal") &&
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
            if ((System.getenv("GITHUB_REF_NAME") == "develop" || System.getenv("GITHUB_REF_NAME") == "terminal") && signingConfigs.findByName("develop") != null) {
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
            "assistant",
            "menu",
            "home",
            "lobby",
            "connect",
            "settings",
            "notifications"
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
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
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
    implementation("com.google.android.gms:play-services-auth:21.5.0")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("net.java.dev.jna:jna:5.18.1@aar")
    implementation("com.alphacephei:vosk-android:0.3.75@aar")
        implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    kapt("androidx.room:room-compiler:2.8.5")
}
