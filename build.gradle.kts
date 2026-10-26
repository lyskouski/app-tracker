plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.flatpak.gradle.generator)
}

// Offline Flatpak build: ./gradlew flatpakGradleGenerator --no-configuration-cache
tasks.flatpakGradleGenerator {
    outputFile = file("linux-flatpak/gradle-sources-root.json")
    downloadDirectory = "./offline-repository"
}
