plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
    explicitApi()
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
    jvmToolchain(21)
    jvm()
    wasmJs {
        browser()
    }
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.jeudry.rosafiesta.domain"
        compileSdk = 37
        minSdk = 26
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.engine)
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
