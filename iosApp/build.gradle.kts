plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "iosApp"
            isStatic = true
            export(project(":shared"))
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val iosMain by getting {
            kotlin.srcDir("src")
            dependencies {
                api(project(":shared"))
                implementation("org.jetbrains.compose.ui:ui:1.11.1")
                implementation(libs.sqldelight.native.driver)
            }
        }
    }
}
