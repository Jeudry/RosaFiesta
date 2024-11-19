plugins {
  alias(libs.plugins.rosafiesta.android.feature.ui)
}

android {
  namespace = "com.example.home.presentation"
}

dependencies {
  implementation(projects.auth.domain)
  implementation(projects.core.domain)
  implementation(project(":products:presentation"))
  implementation(project(":products:domain"))
}