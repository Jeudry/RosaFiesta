plugins {
  alias(libs.plugins.rosafiesta.android.library)
  alias(libs.plugins.rosafiesta.jvm.ktor)
}

android {
  namespace = "com.example.home.network"
}

dependencies {
  implementation(libs.bundles.koin)
  
  implementation(projects.core.domain)
  implementation(projects.core.data)
}