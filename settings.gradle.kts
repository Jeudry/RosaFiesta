import org.gradle.kotlin.dsl.internal.relocated.kotlin.metadata.internal.metadata.deserialization.VersionRequirementTable.Companion.create
import org.gradle.kotlin.dsl.maven

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.version.toml"))
        }
    }
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "adventist"

include("app")

include("user")
include("chat")
include("notification")
include("common")