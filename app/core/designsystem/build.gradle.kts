plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    explicitApi()
    jvmToolchain(21)
    jvm()
    wasmJs {
        browser()
    }
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.jeudry.rosafiesta.designsystem"
        compileSdk = 37
        minSdk = 26
        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.resources)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

compose.resources {
    packageOfResClass = "com.jeudry.rosafiesta.designsystem.resources"
    publicResClass = false
}
