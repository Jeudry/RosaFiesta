pluginManagement {
  includeBuild("build-logic")
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

gradle.startParameter.excludedTaskNames.addAll(listOf(":build-logic:convention:testClasses"))

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "RosaFiesta"
include(":app")
include(":core:data")
include(":core:domain")
include(":core:presentation:designsystem")
include(":core:presentation:ui")
include(":auth:data")
include(":auth:presentation")
include(":auth:domain")
include(":core:database")
include(":home:presentation")
include(":home:data")
include(":home:domain")
include(":home:location")
include(":home:network")
include(":products:presentation")
include(":products:data")
include(":products:domain")