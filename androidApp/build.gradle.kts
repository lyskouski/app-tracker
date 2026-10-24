import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Release signing: key.properties (storePassword, keyPassword, keyAlias) and key.jks next to this file.
val keyProperties = file("key.properties").takeIf { it.exists() }?.let { keyFile ->
    Properties().apply { keyFile.inputStream().use { load(it) } }
}

android {
    namespace = "com.tercad.zwyka"
    compileSdk = 37

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "com.tercad.zwyka"
        minSdk = 24
        targetSdk = 36
        // Set by the release workflow: -PversionCode=<commits count> -PversionName=<tag>.
        versionCode = (findProperty("versionCode") as String?)?.toInt() ?: 1
        versionName = findProperty("versionName") as String? ?: "1.0"
    }

    signingConfigs {
        if (keyProperties != null) {
            create("release") {
                storeFile = file("key.jks")
                storePassword = keyProperties.getProperty("storePassword")
                keyAlias = keyProperties.getProperty("keyAlias")
                keyPassword = keyProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.findByName("release")
        }
    }

    sourceSets.getByName("main") {
        manifest.srcFile("src/AndroidManifest.xml")
        java.srcDir("src")
        res.srcDir("res")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.sqldelight.android.driver)
}
