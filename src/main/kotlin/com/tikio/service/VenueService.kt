package com.tikio.service

import com.tikio.dto.*
import java.time.LocalDateTime

interface VenueService {
    fun getAllVenues(): List<VenueDTO>
    fun getVenueById(id: Long): VenueDTO
    fun createVenue(venueRequest: VenueCreateRequest): VenueDTO
    fun updateVenue(id: Long, venueRequest: VenueUpdateRequest): VenueDTO
    fun deleteVenue(id: Long)
    fun findAvailableVenues(request: VenueAvailabilityRequest): List<VenueDTO>
    fun getVenueSchedule(venueId: Long): VenueScheduleDTO
    fun getVenueSchedule(venueId: Long, startDate: LocalDateTime, endDate: LocalDateTime): VenueScheduleDTO
}