import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.jeudry.rosafiesta.composeapp"
        compileSdk = 37
        minSdk = 26
        androidResources.enable = true
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            export(projects.core.domain)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.engine)
            api(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.core.designsystem)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.compose.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.qrose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

tasks.withType<Test>().configureEach {
    if (!project.hasProperty("screenshots")) exclude("**/*Screenshots*", "**/dev/*ScreenshotTest*")
}

compose.resources {
    packageOfResClass = "com.jeudry.rosafiesta.composeapp.resources"
    publicResClass = false
}

compose.desktop {
    application {
        mainClass = "com.jeudry.rosafiesta.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "RosaFiesta"
            packageVersion = "1.0.0"
        }
    }
}
