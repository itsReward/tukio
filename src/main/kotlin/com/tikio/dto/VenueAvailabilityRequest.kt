package com.tikio.dto

import com.tikio.model.VenueType
import java.time.LocalDateTime

data class VenueAvailabilityRequest(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val minCapacity: Int?,
    val venueType: VenueType?,
    val requiredAmenities: List<String>?,
    val location: String?
)