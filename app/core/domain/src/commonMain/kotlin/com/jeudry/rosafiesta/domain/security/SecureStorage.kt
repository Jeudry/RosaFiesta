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
        public const val KEY_REFRESH_TOKEN: String = "rf_refresh_token"
        public const val KEY_SESSION_METADATA: String = "rf_session_metadata"
        public const val KEY_PENDING_OAUTH_PKCE: String = "rf_pending_oauth_pkce"
        public const val KEY_ACTIVE_VIEW: String = "rf_active_view"
        public const val KEY_USER_ROLE: String = "rf_user_role"
        public const val KEY_DARK_THEME: String = "rf_dark_theme"
        public const val KEY_AVATAR_KEY: String = "rf_avatar_key"
        public const val KEY_AVATAR_IMAGE_DATA: String = "rf_avatar_image_data"
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
        public const val KEY_SELECTED_EVENT_ID: String = "rf_selected_event_id"
        public const val KEY_DRAFT_QUANTITIES: String = "rf_draft_quantities"
        public const val KEY_DRAFT_DISCOUNT: String = "rf_draft_discount"
        public const val KEY_WIZARD_EVENT_DRAFT: String = "rf_wizard_event_draft"
        public const val KEY_WIZARD_SCHEDULE: String = "rf_wizard_schedule"
        public const val KEY_AUDIO_ENABLED: String = "rf_audio_enabled"
        public const val KEY_AUDIO_VOLUME: String = "rf_audio_volume"
        public const val KEY_AUDIO_INTERACTION: String = "rf_audio_interaction"
        public const val KEY_AUDIO_ROSA: String = "rf_audio_rosa"
        public const val KEY_AUDIO_ALERTS: String = "rf_audio_alerts"
    }
}

/**
 * In-memory [SecureStorage] implementation for deterministic unit tests and headless environments.
 */
public class InMemorySecureStorage : SecureStorage {
    private val entries: MutableMap<String, String> = mutableMapOf()

    override suspend fun get(key: String): String? {
        val cleanKey = key.trim()
        return entries[cleanKey]
    }

    override suspend fun put(key: String, value: String) {
        val cleanKey = key.trim()
        val cleanValue = value.trim()
        if (cleanKey.isNotEmpty()) {
            entries[cleanKey] = cleanValue
        }
    }

    override suspend fun remove(key: String) {
        val cleanKey = key.trim()
        entries.remove(cleanKey)
    }
}
