plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
    // Toda API pública debe declarar visibilidad y tipo de retorno de forma explícita.
    explicitApi()
    // Cero advertencias: el motor de reglas de negocio de RosaFiesta debe compilar limpio.
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
        namespace = "com.jeudry.rosafiesta.core.engine"
        compileSdk = 37
        minSdk = 26
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
