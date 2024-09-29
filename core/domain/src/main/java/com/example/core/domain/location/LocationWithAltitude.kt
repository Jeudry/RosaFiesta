package com.example.core.domain.location

import com.example.core.domain.location.Location

data class LocationWithAltitude(
    val location: Location,
    val altitude: Double
)