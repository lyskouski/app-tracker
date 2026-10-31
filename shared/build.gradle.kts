@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.flatpak.gradle.generator)
}

kotlin {
    android {
        namespace = "com.tercad.zwyka.shared"
        compileSdk = 37
        minSdk = 24
        // Packs the compose resources (strings, ...) into the app: without it they are missing on Android.
        androidResources.enable = true
        withHostTest {}
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()
    wasmJs {
        browser()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.compose.runtime:runtime:1.11.1")
                implementation("org.jetbrains.compose.foundation:foundation:1.11.1")
                implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")
                implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
                implementation("org.jetbrains.compose.material3:material3:1.11.0-alpha07")
                implementation("org.jetbrains.compose.ui:ui:1.11.1")
                implementation("org.jetbrains.compose.components:components-resources:1.11.1")
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.tab.navigator)
                implementation(libs.voyager.transitions)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                api(libs.sqldelight.runtime)
                api(libs.sqldelight.async.extensions)
            }
        }

        commonTest {
            kotlin.srcDir("test")
            dependencies {
                implementation(kotlin("test"))
            }
        }

        androidMain {
            dependencies {
                implementation(libs.sqldelight.android.driver)
            }
        }

        jvmMain {
            kotlin.srcDir("src@jvm")
            dependencies {
                implementation(libs.sqldelight.jdbc.driver)
            }
        }

        nativeMain {
            dependencies {
                implementation(libs.sqldelight.native.driver)
            }
        }

        iosMain {
            kotlin.srcDir("src@ios")
        }

        wasmJsMain {
            kotlin.srcDir("src@wasmJs")
            dependencies {
                implementation(libs.sqldelight.web.worker.driver)
            }
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        jvmTest {
            kotlin.srcDir("test@jvm")
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.screenshot.generator)
            }
        }

        iosTest {
            kotlin.srcDir("test@ios")
        }

        wasmJsTest {
            kotlin.srcDir("test@wasmJs")
        }
    }
}

compose.resources {
    packageOfResClass = "com.tercad.zwyka.resources"
    publicResClass = true
    customDirectory(
        "commonMain",
        providers.provider { layout.projectDirectory.dir("composeResources") },
    )
}

val screenshotTests = "com.tercad.zwyka.screenshots.*"
val stressTests = "com.tercad.zwyka.stress.*"

tasks.named<Test>("jvmTest") {
    filter.excludeTestsMatching(screenshotTests)
    filter.excludeTestsMatching(stressTests)
    filter.isFailOnNoMatchingTests = false
}

// ./gradlew :shared:jvmScreenshots -PSCREENSHOT_DEVICES="iPhone 15 Pro" -PSCREENSHOT_LOCALES=en
tasks.register<Test>("jvmScreenshots") {
    description = "Generates application screenshots across devices and locales into ./screenshots."
    group = "verification"
    val jvmTest = tasks.named<Test>("jvmTest").get()
    testClassesDirs = jvmTest.testClassesDirs
    classpath = jvmTest.classpath
    filter.includeTestsMatching(screenshotTests)
    // Environment variables reach the tests as is; -P properties are forwarded.
    listOf("SCREENSHOT_DEVICES", "SCREENSHOT_LOCALES", "SCREENSHOT_STEPS", "SKIP_EXISTANT")
        .forEach { name -> providers.gradleProperty(name).orNull?.let { systemProperty(name, it) } }
    systemProperty(
        "SCREENSHOT_OUTPUT",
        providers.gradleProperty("SCREENSHOT_OUTPUT")
            .orElse(providers.environmentVariable("SCREENSHOT_OUTPUT"))
            .getOrElse(rootProject.layout.projectDirectory.dir("screenshots").asFile.path),
    )
    outputs.upToDateWhen { false }
    testLogging.showStandardStreams = true
}

// ./gradlew :androidApp:installDebug :shared:androidStressTest -PSTRESS_HABITS=60 -PSTRESS_YEARS=3
tasks.register<Test>("androidStressTest") {
    description = "Stress test of the Android app on the connected device or emulator (see AndroidStressTest)."
    group = "verification"
    val jvmTest = tasks.named<Test>("jvmTest").get()
    testClassesDirs = jvmTest.testClassesDirs
    classpath = jvmTest.classpath
    filter.includeTestsMatching(stressTests)
    // -P properties are forwarded to the test.
    providers.gradlePropertiesPrefixedBy("STRESS_").get().forEach { (name, value) -> systemProperty(name, value) }
    providers.gradleProperty("ADB").orNull?.let { systemProperty("ADB", it) }
    systemProperty("STRESS_OUTPUT", layout.buildDirectory.dir("stress").get().asFile.path)
    outputs.upToDateWhen { false }
    testLogging.showStandardStreams = true
    testLogging.events("passed", "failed", "skipped")
    testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.tercad.zwyka.infrastructures")
            generateAsync.set(true)
        }
    }
}

// The Flatpak package needs only the desktop (JVM) target.
tasks.flatpakGradleGenerator {
    outputFile = rootProject.file("linux-flatpak/gradle-sources-shared.json")
    downloadDirectory = "./offline-repository"
    excludeConfigurations = configurations.names.filter { name ->
        listOf("android", "ios", "wasm", "js", "native", "test", "hotReload").any { name.contains(it, ignoreCase = true) }
    }
}
