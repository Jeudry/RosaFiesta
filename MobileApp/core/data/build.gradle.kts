plugins {
  alias(libs.plugins.rosafiesta.android.library)
  alias(libs.plugins.rosafiesta.jvm.ktor)
}

android {
  namespace = "com.example.core.data"
}

dependencies {
  // Timber
  implementation(libs.timber)
  implementation(libs.bundles.koin)
  implementation(libs.jwt.decode)
  implementation(projects.core.domain)
  implementation(projects.core.database)
}