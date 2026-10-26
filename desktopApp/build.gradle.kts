plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.flatpak.gradle.generator)
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

// Offline Flatpak build on x86_64 and aarch64: both Skiko runtimes and the dependencies
// resolved by the Compose plugin during the build go to the offline repository.
val flatpak by configurations.creating
dependencies {
    val compose = libs.versions.compose.get()
    flatpak("org.jetbrains.compose.desktop:desktop-jvm-linux-x64:$compose")
    flatpak("org.jetbrains.compose.desktop:desktop-jvm-linux-arm64:$compose")
    flatpak("org.jetbrains.compose:gradle-plugin-internal-jdk-version-probe:$compose")
}

tasks.flatpakGradleGenerator {
    outputFile = rootProject.file("linux-flatpak/gradle-sources-desktopApp.json")
    downloadDirectory = "./offline-repository"
    excludeConfigurations = configurations.names.filter { name ->
        listOf("test", "hotReload").any { name.contains(it, ignoreCase = true) }
    }
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

            windows {
                iconFile.set(project.file("icons/zwyka.ico"))
            }
        }
    }
}
