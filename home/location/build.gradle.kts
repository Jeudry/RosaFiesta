plugins {
  alias(libs.plugins.rosafiesta.android.library)
}

android {
  namespace = "com.example.home.location"
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.bundles.koin)
  
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.google.android.gms.play.services.location)
  
  implementation(projects.core.domain)
}