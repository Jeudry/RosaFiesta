package com.example.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import com.android.build.api.dsl.BuildType as BuildTypeDsl

internal fun Project.configureBuildTypes(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    extensionType: ExtensionType
){
    commonExtension.run {

        buildFeatures {
            buildConfig = true
        }

        val localProperties = gradleLocalProperties(rootDir, providers)
        val apiKey = localProperties.getProperty("API_KEY")
        val baseUrl = localProperties.getProperty("BASE_URL")

        when(extensionType){
            ExtensionType.APPLICATION -> extensions.configure<ApplicationExtension> {
                buildTypes {
                    debug {
                        configureDebugBuildType(apiKey, baseUrl)
                    }
                    release {
                        configureReleaseBuildType(commonExtension, apiKey, baseUrl)
                    }
                }
            }
            ExtensionType.LIBRARY -> extensions.configure<LibraryExtension> {
                buildTypes {
                    debug {
                        configureDebugBuildType(apiKey, baseUrl)
                    }
                    release {
                        configureReleaseBuildType(commonExtension, apiKey, baseUrl)
                    }
                }
            }
        }
    }
}

private fun BuildTypeDsl.configureDebugBuildType(apiKey: String, baseUrl: String) {
    buildConfigField("String", "API_KEY", "\"$apiKey\"")
    buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
}

private fun BuildTypeDsl.configureReleaseBuildType(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    apiKey: String,
    baseUrl: String
) {
    buildConfigField("String", "API_KEY", "\"$apiKey\"")
    buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

    isMinifyEnabled = false
    proguardFiles(
        commonExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}