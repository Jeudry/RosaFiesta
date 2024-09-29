@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  `kotlin-dsl`
}

group = "com.example.rosafiesta.buildlogic"

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.android.tools.common)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.ksp.gradlePlugin)
  compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("androidApplication") {
      id = "rosafiesta.android.application"
      implementationClass = "AndroidApplicationConventionPlugin"
    }
    register("androidApplicationCompose") {
      id = "rosafiesta.android.application.compose"
      implementationClass = "AndroidApplicationComposeConventionPlugin"
    }
    register("androidLibrary") {
      id = "rosafiesta.android.library"
      implementationClass = "AndroidLibraryConventionPlugin"
    }
    register("androidLibraryCompose") {
      id = "rosafiesta.android.library.compose"
      implementationClass = "AndroidLibraryComposeConventionPlugin"
    }
    register("androidFeatureUi") {
      id = "rosafiesta.android.feature.ui"
      implementationClass = "AndroidFeatureUiConventionPlugin"
    }
    register("androidRoom") {
      id = "rosafiesta.android.room"
      implementationClass = "AndroidRoomConventionPlugin"
    }
    register("androidDynamicFeature") {
      id = "rosafiesta.android.dynamic.feature"
      implementationClass = "AndroidDynamicFeatureConventionPlugin"
    }
    register("jvmLibrary") {
      id = "rosafiesta.jvm.library"
      implementationClass = "JvmLibraryConventionPlugin"
    }
    register("jvmKtor") {
      id = "rosafiesta.jvm.ktor"
      implementationClass = "JvmKtorConventionPlugin"
    }
  }
}