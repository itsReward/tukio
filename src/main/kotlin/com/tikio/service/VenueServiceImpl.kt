package com.tikio.service

import com.tikio.dto.*
import com.tikio.exception.ResourceNotFoundException
import com.tikio.model.Venue
import com.tikio.model.VenueAmenity
import com.tikio.model.VenueBooking
import com.tikio.repository.VenueAmenityRepository
import com.tikio.repository.VenueBookingRepository
import com.tikio.repository.VenueRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class VenueServiceImpl(
    private val venueRepository: VenueRepository,
    private val venueAmenityRepository: VenueAmenityRepository,
    private val venueBookingRepository: VenueBookingRepository
) : VenueService {

    override fun getAllVenues(): List<VenueDTO> {
        return venueRepository.findAll().map { it.toDTO() }
    }

    override fun getVenueById(id: Long): VenueDTO {
        val venue = venueRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Venue not found with id: $id") }
        return venue.toDTO()
    }

    @Transactional
    override fun createVenue(venueRequest: VenueCreateRequest): VenueDTO {
        // Process amenities
        val amenities = processAmenities(venueRequest.amenities)

        // Create new venue
        val venue = Venue(
            name = venueRequest.name,
            location = venueRequest.location,
            capacity = venueRequest.capacity,
            type = venueRequest.type,
            description = venueRequest.description,
            amenities = amenities.toMutableSet()
        )

        return venueRepository.save(venue).toDTO()
    }

    @Transactional
    override fun updateVenue(id: Long, venueRequest: VenueUpdateRequest): VenueDTO {
        val venue = venueRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Venue not found with id: $id") }

        // Update fields if provided
        venueRequest.name?.let { venue.name = it }
        venueRequest.location?.let { venue.location = it }
        venueRequest.capacity?.let { venue.capacity = it }
        venueRequest.type?.let { venue.type = it }
        venueRequest.description?.let { venue.description = it }
        venueRequest.availabilityStatus?.let { venue.availabilityStatus = it }

        // Update amenities if provided
        venueRequest.amenities?.let {
            val amenities = processAmenities(it)
            venue.amenities.clear()
            venue.amenities.addAll(amenities)
        }

        venue.updatedAt = LocalDateTime.now()
        return venueRepository.save(venue).toDTO()
    }

    @Transactional
    override fun deleteVenue(id: Long) {
        if (!venueRepository.existsById(id)) {
            throw ResourceNotFoundException("Venue not found with id: $id")
        }
        venueRepository.deleteById(id)
    }

    override fun findAvailableVenues(request: VenueAvailabilityRequest): List<VenueDTO> {
        // Get venues available during requested time period with minimum capacity
        val minCapacity = request.minCapacity ?: 0
        var availableVenues = venueRepository.findAvailableVenuesByTimeAndCapacity(
            request.startTime,
            request.endTime,
            minCapacity
        )

        // Further filter by venue type if specified
        request.venueType?.let { type ->
            availableVenues = availableVenues.filter { it.type == type }
        }

        // Filter by location if specified
        request.location?.let { location ->
            availableVenues = availableVenues.filter {
                it.location.contains(location, ignoreCase = true)
            }
        }

        // Filter by required amenities if specified
        request.requiredAmenities?.let { amenities ->
            if (amenities.isNotEmpty()) {
                availableVenues = availableVenues.filter { venue ->
                    val venueAmenityNames = venue.amenities.map { it.name.lowercase() }
                    amenities.all { amenity ->
                        venueAmenityNames.contains(amenity.lowercase())
                    }
                }
            }
        }

        return availableVenues.map { it.toDTO() }
    }

    override fun getVenueSchedule(venueId: Long): VenueScheduleDTO {
        return getVenueSchedule(venueId, LocalDateTime.now(), LocalDateTime.now().plusMonths(1))
    }

    override fun getVenueSchedule(venueId: Long, startDate: LocalDateTime, endDate: LocalDateTime): VenueScheduleDTO {
        val venue = venueRepository.findById(venueId)
            .orElseThrow { ResourceNotFoundException("Venue not found with id: $venueId") }

        val bookings = venueBookingRepository.findByVenueIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
            venueId,
            startDate,
            endDate
        )

        return VenueScheduleDTO(
            venueId = venue.id,
            venueName = venue.name,
            bookings = bookings.map { it.toDTO() }
        )
    }

    private fun processAmenities(amenityNames: List<String>): List<VenueAmenity> {
        return amenityNames.map { amenityName ->
            // Find existing amenity or create a new one
            venueAmenityRepository.findByNameIgnoreCase(amenityName)
                ?: venueAmenityRepository.save(VenueAmenity(name = amenityName))
        }
    }

    // Extension functions for mapping to DTOs
    private fun Venue.toDTO(): VenueDTO {
        return VenueDTO(
            id = this.id,
            name = this.name,
            location = this.location,
            capacity = this.capacity,
            type = this.type,
            description = this.description,
            availabilityStatus = this.availabilityStatus,
            amenities = this.amenities.map { it.name },
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    private fun VenueBooking.toDTO(): BookingDTO {
        return BookingDTO(
            id = this.id,
            eventId = this.eventId,
            eventName = this.eventName,
            startTime = this.startTime,
            endTime = this.endTime,
            attendeeCount = this.attendeeCount,
            bookingNotes = this.bookingNotes
        )
    }
}