import java.security.KeyStore
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

/*
 * Release signing: key.properties and key.jks next to this file.
 *
 * Same format as the other terCAD apps: key.password (store and key),
 * key.alias (default: the only key of the store, or "key");
 * storePassword / keyPassword / keyAlias are accepted as well. key.file is ignored: key.jks is used.
 */
val keyProperties = file("key.properties").takeIf { it.exists() }?.let { keyFile ->
    Properties().apply { keyFile.inputStream().use { load(it) } }
}

fun keyProperty(vararg names: String): String? =
    names.firstNotNullOfOrNull { keyProperties?.getProperty(it)?.trim()?.takeIf(String::isNotEmpty) }

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
                val password = keyProperty("key.password")
                storeFile = file("key.jks").also {
                    require(it.exists()) { "Release signing: ${it.path} is missing" }
                }
                storePassword = keyProperty("storePassword") ?: password
                    ?: error("Release signing: key.password is missing in key.properties")
                keyPassword = keyProperty("keyPassword") ?: password
                    ?: error("Release signing: key.password is missing in key.properties")
                // Without an alias, the only key of the store is used.
                keyAlias = keyProperty("keyAlias", "key.alias")
                    ?: KeyStore.getInstance(storeFile, storePassword!!.toCharArray()).aliases().toList()
                        .singleOrNull()
                    ?: "key"
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
    implementation(libs.kotlinx.coroutines.core)
}
