@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            kotlin.srcDir("src")
            resources.srcDir("resources")
            dependencies {
                implementation(project(":shared"))
                implementation("org.jetbrains.compose.ui:ui:1.11.1")
                implementation(libs.sqldelight.web.worker.driver)
                implementation(libs.kotlinx.coroutines.core)
                implementation(npm("sql.js", "1.6.2"))
                implementation(npm("@cashapp/zwyka-sqljs-worker", file("sqljs")))
                implementation(devNpm("copy-webpack-plugin", "9.1.0"))
            }
        }
    }
}
