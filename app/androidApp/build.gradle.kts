import java.io.File

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

if (file("google-services.json").exists()) {
    apply(
        plugin =
            libs.plugins.googleServices
                .get()
                .pluginId,
    )
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
        val configuredApiUrl =
            (project.findProperty("ROSAFIESTA_API_URL") as? String)
                ?: System.getenv("ROSAFIESTA_API_URL")
                ?: ""
        manifestPlaceholders["ROSAFIESTA_API_URL"] = configuredApiUrl.trim()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    val keystorePath =
        (project.findProperty("ROSAFIESTA_KEYSTORE_PATH") as? String)
            ?: System.getenv("ROSAFIESTA_KEYSTORE_PATH")
            ?: ""
    val hasReleaseKeystore = keystorePath.isNotBlank() && File(keystorePath).exists()

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = File(keystorePath)
                storePassword = (project.findProperty("ROSAFIESTA_KEYSTORE_PASSWORD") as? String)
                    ?: System.getenv("ROSAFIESTA_KEYSTORE_PASSWORD")
                    ?: ""
                keyAlias = (project.findProperty("ROSAFIESTA_KEY_ALIAS") as? String)
                    ?: System.getenv("ROSAFIESTA_KEY_ALIAS")
                    ?: ""
                keyPassword = (project.findProperty("ROSAFIESTA_KEY_PASSWORD") as? String)
                    ?: System.getenv("ROSAFIESTA_KEY_PASSWORD")
                    ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(projects.core.engine)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(libs.androidx.activity.compose)
    if (file("google-services.json").exists()) {
        implementation(libs.firebase.messaging)
    }
}
