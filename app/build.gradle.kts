import java.net.URL
import java.security.MessageDigest

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "ir.persiancalendar.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "ir.persiancalendar.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 11
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("ANDROID_KEYSTORE_PATH") ?: "release-key.jks")
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("ANDROID_KEY_ALIAS") ?: ""
            keyPassword = System.getenv("ANDROID_KEY_PASSWORD") ?: ""
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}
dependencies {
    // Compose 1.9.x remains compatible with AGP 8.13 and compileSdk 36.
    val composeBom = platform("androidx.compose:compose-bom:2025.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.2.0")
    implementation("io.github.abdulwahed-s:prayer-time-plus:0.3.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

val downloadCc0Adhan by tasks.registering {
    val audioFile = layout.projectDirectory.file("src/main/res/raw/adhan.ogg")
    outputs.file(audioFile)
    doLast {
        val output = audioFile.asFile
        output.parentFile.mkdirs()
        if (!output.exists()) {
            val connection = URL("https://upload.wikimedia.org/wikipedia/commons/e/e7/Adhan.ogg?download=1").openConnection()
            connection.setRequestProperty("User-Agent", "PersianCalendarAppBuild/1.0 (https://github.com/mohsen64ehsani-beep/PersianCalendarApp)")
            connection.connectTimeout = 20000
            connection.readTimeout = 30000
            connection.getInputStream().use { input ->
                output.outputStream().use { destination -> input.copyTo(destination) }
            }
        }
        val digest = MessageDigest.getInstance("SHA-1").digest(output.readBytes())
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        check(digest == "b86432f1581e11f898de1dc3402af0d541af110e") {
            "CC0 Adhan audio checksum mismatch; refusing to package an unverified file."
        }
    }
}


val downloadVazirmatnFonts by tasks.registering {
    val fontDir = layout.projectDirectory.dir("src/main/res/font")
    val regular = fontDir.file("vazirmatn_regular.ttf")
    val bold = fontDir.file("vazirmatn_bold.ttf")
    outputs.files(regular, bold)
    doLast {
        val files = listOf(
            Triple("Vazirmatn-Regular.ttf", "vazirmatn_regular.ttf", "64e4a8189557a17b63fd7a77794207483e592972"),
            Triple("Vazirmatn-Bold.ttf", "vazirmatn_bold.ttf", "efa9b095da01d299310bdcae293e24e657fe6da9")
        )
        files.forEach { (sourceName, targetName, expectedGitBlobSha) ->
            val output = fontDir.file(targetName).asFile
            output.parentFile.mkdirs()
            if (!output.exists()) {
                val url = "https://raw.githubusercontent.com/rastikerdar/vazirmatn/master/fonts/ttf/$sourceName"
                val connection = URL(url).openConnection()
                connection.connectTimeout = 20000
                connection.readTimeout = 30000
                connection.getInputStream().use { input ->
                    output.outputStream().use { destination -> input.copyTo(destination) }
                }
            }
            val bytes = output.readBytes()
            val digest = MessageDigest.getInstance("SHA-1")
            digest.update("blob ${bytes.size}\u0000".toByteArray(Charsets.UTF_8))
            digest.update(bytes)
            val actualGitBlobSha = digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
            check(actualGitBlobSha == expectedGitBlobSha) {
                "Vazirmatn font checksum mismatch for $targetName; refusing to package an unverified font."
            }
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn(downloadCc0Adhan, downloadVazirmatnFonts)
}
