package com.jeudry.rosafiesta.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver

/**
 * Applies additive schema statements for pre-existing local SQLite caches created before v2 columns and tables.
 */
internal object RosaFiestaSchemaUpgrade {
    private const val SCHEMA_UPGRADE_VERSION: Long = 2L

    private val tableStatements: List<String> = listOf(
        """
        CREATE TABLE IF NOT EXISTS quoteEntity (
            id TEXT NOT NULL PRIMARY KEY,
            code TEXT NOT NULL,
            eventId TEXT NOT NULL,
            eventTitle TEXT NOT NULL,
            clientName TEXT NOT NULL,
            clientEmail TEXT NOT NULL,
            venue TEXT NOT NULL,
            dateLabel TEXT NOT NULL,
            guests INTEGER NOT NULL,
            status TEXT NOT NULL,
            linesJson TEXT NOT NULL DEFAULT '[]',
            deliveryFeeDop INTEGER NOT NULL,
            discountDop INTEGER NOT NULL,
            totalDop INTEGER NOT NULL,
            signatureSvg TEXT NOT NULL,
            depositPercent INTEGER NOT NULL,
            depositDop INTEGER NOT NULL,
            signedByName TEXT NOT NULL,
            rejectionReason TEXT NOT NULL,
            eventDateIso TEXT NOT NULL,
            packageId TEXT NOT NULL,
            contractPdfUrl TEXT NOT NULL,
            signedAtIso TEXT NOT NULL,
            sortOrder INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent(),
        """
        CREATE TABLE IF NOT EXISTS deliveryStopEntity (
            stopOrder INTEGER NOT NULL,
            eventId TEXT NOT NULL,
            eventTitle TEXT NOT NULL,
            venue TEXT NOT NULL,
            windowLabel TEXT NOT NULL,
            vehicleAndCrew TEXT NOT NULL,
            statusLabel TEXT NOT NULL,
            PRIMARY KEY(eventId, windowLabel)
        )
        """.trimIndent(),
        """
        CREATE TABLE IF NOT EXISTS damageIncidentEntity (
            id TEXT NOT NULL PRIMARY KEY,
            eventId TEXT NOT NULL,
            eventTitle TEXT NOT NULL,
            articleCode TEXT NOT NULL,
            articleName TEXT NOT NULL,
            damagedUnits INTEGER NOT NULL,
            missingUnits INTEGER NOT NULL,
            replacementDop INTEGER NOT NULL,
            notes TEXT NOT NULL,
            photoUrl TEXT NOT NULL,
            resolutionNotes TEXT NOT NULL,
            chargePaymentId TEXT NOT NULL
        )
        """.trimIndent(),
        """
        CREATE TABLE IF NOT EXISTS notificationEntity (
            id TEXT NOT NULL PRIMARY KEY,
            title TEXT NOT NULL,
            body TEXT NOT NULL,
            category TEXT NOT NULL,
            createdAtLabel TEXT NOT NULL,
            isRead INTEGER NOT NULL DEFAULT 0,
            recipientRole TEXT NOT NULL,
            actionPath TEXT NOT NULL
        )
        """.trimIndent(),
        """
        CREATE TABLE IF NOT EXISTS submoduleSnapshotEntity (
            snapshotKey TEXT NOT NULL PRIMARY KEY,
            payloadJson TEXT NOT NULL
        )
        """.trimIndent(),
        """
        CREATE TABLE IF NOT EXISTS activeSessionEntity (
            slot INTEGER NOT NULL PRIMARY KEY,
            userId TEXT NOT NULL,
            accessToken TEXT NOT NULL
        )
        """.trimIndent(),
    )

    private val columnUpgradesByTable: Map<String, List<Pair<String, String>>> = mapOf(
        "LocalArticleEntity" to listOf(
            "photoUrl" to "TEXT NOT NULL DEFAULT ''",
            "damagedStock" to "INTEGER NOT NULL DEFAULT 0",
            "variantsJson" to "TEXT NOT NULL DEFAULT '[]'",
            "dimensions" to "TEXT NOT NULL DEFAULT ''",
            "material" to "TEXT NOT NULL DEFAULT ''",
            "visibleInCatalog" to "INTEGER NOT NULL DEFAULT 1",
        ),
        "LocalChecklistStepEntity" to listOf(
            "eventId" to "TEXT NOT NULL DEFAULT 'evt-boda-real'",
        ),
        "LocalOfflineMutationEntity" to listOf(
            "operationType" to "TEXT NOT NULL DEFAULT ''",
            "payloadJson" to "TEXT NOT NULL DEFAULT '{}'",
            "idempotencyKey" to "TEXT NOT NULL DEFAULT ''",
        ),
        "eventEntity" to listOf(
            "eventDateIso" to "TEXT NOT NULL DEFAULT ''",
            "pickupDateIso" to "TEXT NOT NULL DEFAULT ''",
            "returnDateIso" to "TEXT NOT NULL DEFAULT ''",
            "quoteId" to "TEXT NOT NULL DEFAULT ''",
            "closeoutCompleted" to "INTEGER NOT NULL DEFAULT 0",
            "closeoutNotes" to "TEXT NOT NULL DEFAULT ''",
            "sketchJson" to "TEXT NOT NULL DEFAULT ''",
            "lastPaymentReminderAt" to "TEXT NOT NULL DEFAULT ''",
            "lastPaymentReminderChannel" to "TEXT NOT NULL DEFAULT ''",
            "itemsJson" to "TEXT NOT NULL DEFAULT '[]'",
            "clientEmail" to "TEXT NOT NULL DEFAULT ''",
            "cancellationReason" to "TEXT NOT NULL DEFAULT ''",
            "setupPhotoUrlsJson" to "TEXT NOT NULL DEFAULT '[]'",
            "contactPhone" to "TEXT NOT NULL DEFAULT ''",
        ),
        "quoteEntity" to listOf(
            "contactPhone" to "TEXT NOT NULL DEFAULT ''",
            "signedByUserId" to "TEXT NOT NULL DEFAULT ''",
            "contractHashSha256" to "TEXT NOT NULL DEFAULT ''",
        ),
        "staffPermissionEntity" to listOf(
            "email" to "TEXT NOT NULL DEFAULT ''",
            "phone" to "TEXT NOT NULL DEFAULT ''",
            "active" to "INTEGER NOT NULL DEFAULT 1",
        ),
        "userAccountEntity" to listOf(
            "pushNotificationsEnabled" to "INTEGER NOT NULL DEFAULT 1",
            "whatsappAlertsEnabled" to "INTEGER NOT NULL DEFAULT 1",
            "emailReceiptsEnabled" to "INTEGER NOT NULL DEFAULT 1",
            "biometricLockEnabled" to "INTEGER NOT NULL DEFAULT 0",
            "autoSyncWifiOnly" to "INTEGER NOT NULL DEFAULT 0",
            "eventRemindersEnabled" to "INTEGER NOT NULL DEFAULT 1",
            "preferredLanguage" to "TEXT NOT NULL DEFAULT 'es'",
            "darkThemeEnabled" to "INTEGER NOT NULL DEFAULT 0",
            "savedPaymentMethod" to "TEXT NOT NULL DEFAULT 'Transferencia'",
            "profileZone" to "TEXT NOT NULL DEFAULT 'Distrito Nacional'",
            "avatarKey" to "TEXT NOT NULL DEFAULT ''",
        ),
        "paymentEntity" to listOf(
            "voucherUrl" to "TEXT NOT NULL DEFAULT ''",
            "ncfType" to "TEXT NOT NULL DEFAULT 'B02'",
            "ncfNumber" to "TEXT NOT NULL DEFAULT ''",
            "clientRnc" to "TEXT NOT NULL DEFAULT ''",
            "subtotalDop" to "INTEGER NOT NULL DEFAULT 0",
            "itbisDop" to "INTEGER NOT NULL DEFAULT 0",
            "securityDepositDop" to "INTEGER NOT NULL DEFAULT 0",
            "rejectionReason" to "TEXT NOT NULL DEFAULT ''",
        ),
    )

    private const val UPGRADE_META_TABLE_SQL: String =
        "CREATE TABLE IF NOT EXISTS _rf_schema_upgrade (id INTEGER NOT NULL PRIMARY KEY, version INTEGER NOT NULL)"

    internal fun applySafeColumnUpgrades(driver: SqlDriver) {
        val currentVersion = readUpgradeVersion(driver)
        if (currentVersion >= SCHEMA_UPGRADE_VERSION) {
            return
        }
        tableStatements.forEach { sql ->
            runCatching {
                driver.execute(identifier = null, sql = sql, parameters = 0)
            }
        }
        columnUpgradesByTable.forEach { (tableName, columnSpecs) ->
            val existingColumns = readExistingColumnNames(driver, tableName)
            columnSpecs.forEach { (columnName, columnDef) ->
                val isColumnMissing = columnName !in existingColumns
                if (isColumnMissing) {
                    runCatching {
                        driver.execute(
                            identifier = null,
                            sql = "ALTER TABLE $tableName ADD COLUMN $columnName $columnDef",
                            parameters = 0,
                        )
                    }
                }
            }
        }
        runCatching {
            driver.execute(identifier = null, sql = UPGRADE_META_TABLE_SQL, parameters = 0)
            driver.execute(
                identifier = null,
                sql = "INSERT OR REPLACE INTO _rf_schema_upgrade(id, version) VALUES (1, $SCHEMA_UPGRADE_VERSION)",
                parameters = 0,
            )
        }
    }

    private fun readUpgradeVersion(driver: SqlDriver): Long = runCatching {
        driver.execute(identifier = null, sql = UPGRADE_META_TABLE_SQL, parameters = 0)
        driver.executeQuery(
            identifier = null,
            sql = "SELECT version FROM _rf_schema_upgrade WHERE id = 1",
            mapper = { cursor ->
                val version = if (cursor.next().value) {
                    cursor.getLong(0) ?: 0L
                } else {
                    0L
                }
                QueryResult.Value(version)
            },
            parameters = 0,
        ).value
    }.getOrDefault(0L)

    private fun readExistingColumnNames(driver: SqlDriver, tableName: String): Set<String> = runCatching {
        driver.executeQuery(
            identifier = null,
            sql = "PRAGMA table_info('$tableName')",
            mapper = { cursor ->
                val names = mutableSetOf<String>()
                for (rowIndex in 0 until 256) {
                    if (!cursor.next().value) break
                    val colName = cursor.getString(1)
                    if (!colName.isNullOrBlank()) {
                        names += colName
                    }
                }
                QueryResult.Value(names.toSet())
            },
            parameters = 0,
        ).value
    }.getOrDefault(emptySet())
}
