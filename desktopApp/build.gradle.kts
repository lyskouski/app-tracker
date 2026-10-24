plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
    sourceSets.main {
        kotlin.srcDir("src")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.sqldelight.jdbc.driver)
    implementation(libs.logback.classic)
}

compose.desktop {
    application {
        mainClass = "com.tercad.zwyka.MainKt"

        nativeDistributions {
            packageName = "zwyka"
            // Set by the release workflow: -PversionName=<tag>.
            packageVersion = findProperty("versionName") as String? ?: "1.0.0"
            description = "Open-source cross-platform habits tracking application"
            vendor = "terCAD"
            // From `./gradlew :desktopApp:suggestRuntimeModules`.
            modules("java.instrument", "java.sql", "jdk.unsupported")
        }
    }
}
