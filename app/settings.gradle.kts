rootProject.name = "rosafiesta"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":core:engine")
include(":core:domain")
include(":core:data")
include(":core:designsystem")
include(":composeApp")
include(":androidApp")
include(":adminAndroidApp")
