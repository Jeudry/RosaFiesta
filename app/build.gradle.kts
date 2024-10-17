plugins {
  alias(libs.plugins.rosafiesta.android.application.compose)
  alias(libs.plugins.rosafiesta.jvm.ktor)
  alias(libs.plugins.mapsplatform.secrets.plugin)
}


android {
  namespace = "com.example.rosafiesta"

  defaultConfig {
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  // Compose
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)

  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)

  // Core

  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.core.ktx)

  // Coil
  implementation(libs.coil.compose)

  // Icons
  implementation(libs.androidx.material.icons.extended)

  // Crypto
  implementation(libs.androidx.security.crypto.ktx)

  implementation(libs.bundles.koin)

  api(libs.core)
  implementation(project(":home:presentation"))

  // Test
  androidTestImplementation(platform(libs.androidx.compose.bom))
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)

  // Location
  implementation(libs.google.android.gms.play.services.location)

  // Splash screen
  implementation(libs.androidx.core.splashscreen)

  // Timber
  implementation(libs.timber)

  implementation(projects.core.domain)
  implementation(projects.core.data)
  implementation(projects.core.database)
  implementation(projects.core.presentation.ui)
  implementation(projects.core.presentation.designsystem)

  implementation(projects.auth.data)
  implementation(projects.auth.domain)
  implementation(projects.auth.presentation)

  implementation(projects.products.data)
  implementation(projects.products.domain)
  implementation(projects.products.presentation)
}