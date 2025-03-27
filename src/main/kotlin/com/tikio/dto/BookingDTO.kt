package com.tikio.dto

import java.time.LocalDateTime

data class BookingDTO(
    val id: Long,
    val eventId: Long,
    val eventName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val attendeeCount: Int,
    val bookingNotes: String?
)

