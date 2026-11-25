package com.jeudry.rosafiesta.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jeudry.rosafiesta.data.db.RosaFiestaDatabase
import java.io.File

public actual class DriverFactory(
    private val url: String = JdbcSqliteDriver.IN_MEMORY,
) {
    public actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver(url)
        runCatching { RosaFiestaDatabase.Schema.create(driver) }
        RosaFiestaSchemaUpgrade.applySafeColumnUpgrades(driver)
        return driver
    }
}

internal fun isRunningInAutomatedTest(): Boolean {
    val gradleWorkerProp = System.getProperty("org.gradle.test.worker").orEmpty()
    if (gradleWorkerProp.isNotBlank()) {
        return true
    }
    val javaCommand = System.getProperty("sun.java.command").orEmpty()
    if (javaCommand.contains("GradleWorkerMain")) {
        return true
    }
    if (javaCommand.contains("junit", ignoreCase = true)) {
        return true
    }
    val stackFrames = Thread.currentThread().stackTrace
    return stackFrames.any { frame ->
        val className = frame.className
        val isTestFrameworkFrame = listOf(
            "org.junit.",
            "org.gradle.api.internal.tasks.testing.",
            "androidx.compose.ui.test.",
            "kotlinx.coroutines.test.",
        ).any { prefix -> className.startsWith(prefix) }
        isTestFrameworkFrame
    }
}

internal fun resolveDesktopAppDataDirectory(): File {
    val homeDir = System.getProperty("user.home").orEmpty().ifBlank { "." }
    val rootDir = File(homeDir, ".rosafiesta")
    val profile = System.getProperty("rosafiesta.profile").orEmpty().trim().lowercase()
    val resolvedDir = if (profile.isEmpty()) rootDir else File(rootDir, profile)
    if (!resolvedDir.exists()) {
        resolvedDir.mkdirs()
    }
    return resolvedDir
}

public actual fun createDefaultSqlDriverOrNull(): SqlDriver? = runCatching {
    if (isRunningInAutomatedTest()) {
        return@runCatching DriverFactory(url = JdbcSqliteDriver.IN_MEMORY).createDriver()
    }
    val appDir = resolveDesktopAppDataDirectory()
    val dbFile = File(appDir, "rosafiesta.db")
    DriverFactory(url = "jdbc:sqlite:${dbFile.absolutePath}").createDriver()
}.getOrNull()

public actual fun createDefaultLocalSnapshotCacheOrNull(): LocalSnapshotCache? = null
