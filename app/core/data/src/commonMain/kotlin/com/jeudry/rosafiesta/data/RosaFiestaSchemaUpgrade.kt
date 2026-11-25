package com.jeudry.rosafiesta.data

import app.cash.sqldelight.db.SqlDriver

/**
 * Applies additive schema statements for pre-existing local SQLite caches created before v2 columns and tables.
 */
internal object RosaFiestaSchemaUpgrade {
    private val statements: List<String> = listOf(
        "ALTER TABLE LocalArticleEntity ADD COLUMN photoUrl TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE LocalArticleEntity ADD COLUMN damagedStock INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE LocalArticleEntity ADD COLUMN variantsJson TEXT NOT NULL DEFAULT '[]'",
        "ALTER TABLE LocalArticleEntity ADD COLUMN dimensions TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE LocalArticleEntity ADD COLUMN material TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE LocalArticleEntity ADD COLUMN visibleInCatalog INTEGER NOT NULL DEFAULT 1",
        "ALTER TABLE LocalChecklistStepEntity ADD COLUMN eventId TEXT NOT NULL DEFAULT 'evt-boda-real'",
        "ALTER TABLE LocalOfflineMutationEntity ADD COLUMN operationType TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE LocalOfflineMutationEntity ADD COLUMN payloadJson TEXT NOT NULL DEFAULT '{}'",
        "ALTER TABLE LocalOfflineMutationEntity ADD COLUMN idempotencyKey TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE eventEntity ADD COLUMN eventDateIso TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE eventEntity ADD COLUMN pickupDateIso TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE eventEntity ADD COLUMN returnDateIso TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE eventEntity ADD COLUMN quoteId TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE eventEntity ADD COLUMN closeoutCompleted INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE eventEntity ADD COLUMN closeoutNotes TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE eventEntity ADD COLUMN sketchJson TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE eventEntity ADD COLUMN lastPaymentReminderAt TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE eventEntity ADD COLUMN lastPaymentReminderChannel TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE eventEntity ADD COLUMN itemsJson TEXT NOT NULL DEFAULT '[]'",
        "ALTER TABLE eventEntity ADD COLUMN clientEmail TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE eventEntity ADD COLUMN cancellationReason TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE eventEntity ADD COLUMN setupPhotoUrlsJson TEXT NOT NULL DEFAULT '[]'",
        "ALTER TABLE eventEntity ADD COLUMN contactPhone TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE quoteEntity ADD COLUMN contactPhone TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE quoteEntity ADD COLUMN signedByUserId TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE quoteEntity ADD COLUMN contractHashSha256 TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE staffPermissionEntity ADD COLUMN email TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE staffPermissionEntity ADD COLUMN phone TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE staffPermissionEntity ADD COLUMN active INTEGER NOT NULL DEFAULT 1",
        "ALTER TABLE userAccountEntity ADD COLUMN pushNotificationsEnabled INTEGER NOT NULL DEFAULT 1",
        "ALTER TABLE userAccountEntity ADD COLUMN whatsappAlertsEnabled INTEGER NOT NULL DEFAULT 1",
        "ALTER TABLE userAccountEntity ADD COLUMN emailReceiptsEnabled INTEGER NOT NULL DEFAULT 1",
        "ALTER TABLE userAccountEntity ADD COLUMN biometricLockEnabled INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE userAccountEntity ADD COLUMN autoSyncWifiOnly INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE userAccountEntity ADD COLUMN eventRemindersEnabled INTEGER NOT NULL DEFAULT 1",
        "ALTER TABLE userAccountEntity ADD COLUMN preferredLanguage TEXT NOT NULL DEFAULT 'es'",
        "ALTER TABLE userAccountEntity ADD COLUMN darkThemeEnabled INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE userAccountEntity ADD COLUMN savedPaymentMethod TEXT NOT NULL DEFAULT 'Transferencia'",
        "ALTER TABLE userAccountEntity ADD COLUMN profileZone TEXT NOT NULL DEFAULT 'Distrito Nacional'",
        "ALTER TABLE userAccountEntity ADD COLUMN avatarKey TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE paymentEntity ADD COLUMN voucherUrl TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE paymentEntity ADD COLUMN ncfType TEXT NOT NULL DEFAULT 'B02'",
        "ALTER TABLE paymentEntity ADD COLUMN ncfNumber TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE paymentEntity ADD COLUMN clientRnc TEXT NOT NULL DEFAULT ''",
        "ALTER TABLE paymentEntity ADD COLUMN subtotalDop INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE paymentEntity ADD COLUMN itbisDop INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE paymentEntity ADD COLUMN securityDepositDop INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE paymentEntity ADD COLUMN rejectionReason TEXT NOT NULL DEFAULT ''",
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

    internal fun applySafeColumnUpgrades(driver: SqlDriver) {
        statements.forEach { sql ->
            runCatching {
                driver.execute(identifier = null, sql = sql, parameters = 0)
            }
        }
    }
}
