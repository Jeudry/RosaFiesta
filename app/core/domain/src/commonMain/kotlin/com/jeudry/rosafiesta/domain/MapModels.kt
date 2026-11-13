package com.jeudry.rosafiesta.domain

/**
 * Represents a real geocoded place or address candidate resolved for the in-app map picker.
 */
public data class MapPlaceSuggestion(
    val displayName: String,
    val shortTitle: String,
    val latitude: Double,
    val longitude: Double,
)
