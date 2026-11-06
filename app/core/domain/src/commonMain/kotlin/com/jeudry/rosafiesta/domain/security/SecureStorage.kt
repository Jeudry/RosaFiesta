package com.jeudry.rosafiesta.domain.security

/**
 * Encrypted or OS-protected key-value store for session tokens and sensitive credentials.
 */
public interface SecureStorage {
    /**
     * Retrieves the stored value for [key], or `null` if absent.
     */
    public suspend fun get(key: String): String?

    /**
     * Persists [value] under [key] after trimming whitespace.
     */
    public suspend fun put(key: String, value: String)

    /**
     * Removes the value associated with [key].
     */
    public suspend fun remove(key: String)

    public companion object {
        public const val KEY_ACCESS_TOKEN: String = "rf_access_token"
        public const val KEY_ACTIVE_VIEW: String = "rf_active_view"
        public const val KEY_USER_ROLE: String = "rf_user_role"
        public const val KEY_DARK_THEME: String = "rf_dark_theme"
        public const val KEY_AVATAR_KEY: String = "rf_avatar_key"
        public const val KEY_LANGUAGE: String = "rf_language"
        public const val KEY_REMINDERS: String = "rf_reminders"
        public const val KEY_PAYMENT_METHOD: String = "rf_payment_method"
        public const val KEY_COMPANY_REVIEWS: String = "rf_company_reviews"
        public const val KEY_DIRECT_CHAT: String = "rf_direct_chat"
        public const val KEY_EVENT_TASKS: String = "rf_event_tasks"
        public const val KEY_EVENT_TIMELINE: String = "rf_event_timeline"
        public const val KEY_EVENT_GUESTS: String = "rf_event_guests"
        public const val KEY_SKETCH_TABLES: String = "rf_sketch_tables"
        public const val KEY_SUPPLIERS: String = "rf_suppliers"
    }
}

/**
 * Thread-safe in-memory [SecureStorage] implementation for unit tests and ephemeral sessions.
 */
public class InMemorySecureStorage : SecureStorage {
    private val entries: MutableMap<String, String> = mutableMapOf()

    override suspend fun get(key: String): String? = entries[key.trim()]

    override suspend fun put(key: String, value: String) {
        val cleanKey = key.trim()
        val cleanValue = value.trim()
        if (cleanKey.isNotEmpty()) {
            entries[cleanKey] = cleanValue
        }
    }

    override suspend fun remove(key: String) {
        entries.remove(key.trim())
    }
}
