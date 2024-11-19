plugins {
  alias(libs.plugins.rosafiesta.android.library)
  alias(libs.plugins.rosafiesta.jvm.ktor)
}

android {
  namespace = "com.example.auth.data"
}

dependencies {
  implementation(libs.bundles.koin)
  implementation(projects.auth.domain)
  implementation(projects.core.domain)
  implementation(projects.core.data)
}