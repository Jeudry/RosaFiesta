import java.util.*

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.liquibase.core)
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.flywaydb)
    alias(libs.plugins.liquibase)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    implementation(libs.ktor.server.task.scheduling.core)
    implementation(libs.ktor.server.task.scheduling.redis)
    implementation(libs.ktor.server.task.scheduling.mongodb)
    implementation(libs.ktor.server.task.scheduling.jdbc)
    implementation(libs.ktor.server.rate.limiting)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.websockets)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.postgresql)
    implementation(libs.h2)

    // DataBase
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.zaxxer)
    implementation(libs.exposed.java.time)
    implementation(libs.commons.lang)
    implementation(libs.liquibase.core)
    implementation(libs.logback.core)
    implementation(libs.javax.xml.bind)
    liquibaseRuntime(libs.liquibase.core)

    // Monitoring
    implementation(libs.ktor.server.metrics)
    implementation(libs.ktor.server.call.logging)

    // Http
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.http.redirect)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.auto.head.response)

    // Security
    implementation(libs.ktor.server.auth)
    implementation(libs.firebase.auth.provider)
    implementation(libs.ktor.server.auth.jwt)

    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation(libs.apache.commons.codec)
}

// Database migrations
val dbEnv: String by project.ext

val propertiesFile = file("local.properties")
val properties = Properties()
if (propertiesFile.exists()) {
    properties.load(propertiesFile.inputStream())
}

liquibase {
    activities.register("dev") {
        this.arguments = mapOf(
            "logLevel" to "info",
            "changeLogFile" to "src/main/resources/db/migration/migrations.xml",
            "url" to "jdbc:mysql://localhost:3308/chucknorris",
            "username" to "root",
            "password" to "password"
        )
    }

    activities.register("prod") {
        val url = properties.getProperty("liquibase.url") ?: System.getenv("LIQUIBASE_URL")
        val user = properties.getProperty("liquibase.user") ?: System.getenv("LIQUIBASE_USER")
        val pwd = properties.getProperty("liquibase.pwd") ?: System.getenv("LIQUIBASE_PWD")

        this.arguments = mapOf(
            "logLevel" to "info",
            "changeLogFile" to "/resources/db/migration/migrations.xml",
            "url" to url,
            "username" to user,
            "password" to pwd
        )
    }
    runList = dbEnv
}