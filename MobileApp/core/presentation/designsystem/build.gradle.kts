plugins {
  alias(libs.plugins.rosafiesta.android.library.compose)
}

android {
  namespace = "com.example.core.presentation.designsystem"
}

dependencies {
  
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  debugImplementation(libs.androidx.ui.tooling)
  api(libs.androidx.material3)
}