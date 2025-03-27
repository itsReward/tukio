package com.tikio.dto

import com.tikio.model.VenueType
import java.time.LocalDateTime

data class VenueAllocationRequest(
    val eventId: Long,
    val eventName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val attendeeCount: Int,
    val requiredAmenities: List<String>?,
    val preferredVenueType: VenueType?,
    val preferredLocation: String?,
    val notes: String?
)