plugins {
  alias(libs.plugins.rosafiesta.android.library)
  alias(libs.plugins.rosafiesta.jvm.ktor)
}

android {
  namespace = "com.example.products.data"
}

dependencies {
  // Timber
  implementation(libs.timber)
  implementation(libs.bundles.koin)
  implementation(projects.core.domain)
  implementation(projects.core.database)
  implementation(projects.products.domain)
}