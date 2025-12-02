package com.adventist.adventist.domain.events

import java.time.Instant

interface AdventistEvent {
    val eventId: String
    val eventKey: String
    val occurredAt: Instant
    val exchange: String
}