plugins {
  alias(libs.plugins.rosafiesta.android.feature.ui)
}

android {
  namespace = "com.example.products.presentation"
}

dependencies {
  implementation(libs.coil.compose)
  implementation(libs.androidx.activity.compose)
  implementation(libs.timber)

  implementation(projects.auth.domain)
  implementation(projects.core.domain)
  implementation(projects.products.domain)
}