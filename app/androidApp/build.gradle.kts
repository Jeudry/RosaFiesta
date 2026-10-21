plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.jeudry.rosafiesta.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.jeudry.rosafiesta"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(projects.core.engine)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(libs.androidx.activity.compose)
}
