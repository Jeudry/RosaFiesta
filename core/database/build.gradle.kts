plugins {
  alias(libs.plugins.rosafiesta.android.library)
  alias(libs.plugins.rosafiesta.android.room)
}

android {
  namespace = "com.example.core.database"
}

dependencies {
  // DB
  implementation(libs.org.mongodb.bson)
  implementation(libs.bundles.koin)
  
  implementation(projects.core.domain)
}