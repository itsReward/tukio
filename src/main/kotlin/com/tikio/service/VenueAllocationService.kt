package com.tikio.service

import com.tikio.dto.AllocationResponseDTO
import com.tikio.dto.VenueAllocationRequest
import java.time.LocalDateTime

interface VenueAllocationService {
    fun allocateVenue(request: VenueAllocationRequest): AllocationResponseDTO
    fun checkVenueAvailability(venueId: Long, startTime: LocalDateTime, endTime: LocalDateTime): Boolean
    fun cancelVenueBooking(eventId: Long): Boolean
}