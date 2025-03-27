package com.tikio.dto

data class VenueScheduleDTO(
    val venueId: Long,
    val venueName: String,
    val bookings: List<BookingDTO>
)