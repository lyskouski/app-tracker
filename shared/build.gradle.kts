@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sqldelight)
}

kotlin {
    android {
        namespace = "com.tercad.zwyka.shared"
        compileSdk = 37
        minSdk = 24
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

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.tercad.zwyka.infrastructures")
            generateAsync.set(true)
        }
    }
}
