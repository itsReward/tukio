package com.tikio.service

import com.tikio.dto.AllocationResponseDTO
import com.tikio.dto.VenueAllocationRequest
import com.tikio.exception.VenueAllocationException
import com.tikio.model.Venue
import com.tikio.model.VenueBooking
import com.tikio.repository.VenueAmenityRepository
import com.tikio.repository.VenueBookingRepository
import com.tikio.repository.VenueRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class VenueAllocationServiceImpl(
    private val venueRepository: VenueRepository,
    private val venueAmenityRepository: VenueAmenityRepository,
    private val venueBookingRepository: VenueBookingRepository
) : VenueAllocationService {

    private val logger = LoggerFactory.getLogger(VenueAllocationServiceImpl::class.java)

    @Transactional
    override fun allocateVenue(request: VenueAllocationRequest): AllocationResponseDTO {
        logger.info("Processing venue allocation request for event ${request.eventId}: ${request.eventName}")

        // Check if there's already a booking for this event
        val existingBookings = venueBookingRepository.findByEventId(request.eventId)
        if (existingBookings.isNotEmpty()) {
            logger.info("Event ${request.eventId} already has venue bookings. Cancelling previous bookings.")
            // Cancel previous bookings for this event
            venueBookingRepository.deleteAll(existingBookings)
        }

        // Step 1: Find all venues that meet the capacity requirement
        var candidates = venueRepository.findByCapacityGreaterThanEqualAndAvailabilityStatusTrue(request.attendeeCount)
        if (candidates.isEmpty()) {
            return AllocationResponseDTO(
                success = false,
                venueId = null,
                venueName = null,
                message = "No venues available with sufficient capacity for ${request.attendeeCount} attendees"
            )
        }

        // Step 2: Filter out venues that are already booked during the requested time
        candidates = candidates.filter { venue ->
            checkVenueAvailability(venue.id, request.startTime, request.endTime)
        }

        if (candidates.isEmpty()) {
            return AllocationResponseDTO(
                success = false,
                venueId = null,
                venueName = null,
                message = "All suitable venues are already booked during the requested time"
            )
        }

        // Step 3: Further filter by venue type if specified
        request.preferredVenueType?.let { venueType ->
            val typedCandidates = candidates.filter { it.type == venueType }
            if (typedCandidates.isNotEmpty()) {
                candidates = typedCandidates
            }
        }

        // Step 4: Filter by location if specified
        request.preferredLocation?.let { location ->
            val locationCandidates = candidates.filter {
                it.location.contains(location, ignoreCase = true)
            }
            if (locationCandidates.isNotEmpty()) {
                candidates = locationCandidates
            }
        }

        // Step 5: Filter by required amenities if specified
        request.requiredAmenities?.let { amenities ->
            if (amenities.isNotEmpty()) {
                val amenityCandidates = candidates.filter { venue ->
                    val venueAmenityNames = venue.amenities.map { it.name.lowercase() }
                    amenities.all { amenity -> venueAmenityNames.contains(amenity.lowercase()) }
                }
                if (amenityCandidates.isNotEmpty()) {
                    candidates = amenityCandidates
                }
            }
        }

        // Step 6: Rank venues based on a scoring algorithm
        val rankedVenues = rankVenues(candidates, request)

        if (rankedVenues.isEmpty()) {
            return AllocationResponseDTO(
                success = false,
                venueId = null,
                venueName = null,
                message = "No suitable venues found after applying all filters"
            )
        }

        // Step 7: Select the highest-ranked venue
        val selectedVenue = rankedVenues.first()

        // Step 8: Create the booking
        try {
            val booking = VenueBooking(
                venue = selectedVenue,
                eventId = request.eventId,
                eventName = request.eventName,
                startTime = request.startTime,
                endTime = request.endTime,
                attendeeCount = request.attendeeCount,
                bookingNotes = request.notes
            )

            venueBookingRepository.save(booking)

            logger.info("Successfully allocated venue ${selectedVenue.id}: ${selectedVenue.name} for event ${request.eventId}")

            return AllocationResponseDTO(
                success = true,
                venueId = selectedVenue.id,
                venueName = selectedVenue.name,
                message = "Venue allocated successfully"
            )
        } catch (e: Exception) {
            logger.error("Error allocating venue: ${e.message}", e)
            throw VenueAllocationException("Failed to allocate venue: ${e.message}")
        }
    }

    override fun checkVenueAvailability(venueId: Long, startTime: LocalDateTime, endTime: LocalDateTime): Boolean {
        val conflictingBookings = venueBookingRepository.findConflictingBookings(venueId, startTime, endTime)
        return conflictingBookings.isEmpty()
    }

    @Transactional
    override fun cancelVenueBooking(eventId: Long): Boolean {
        val bookings = venueBookingRepository.findByEventId(eventId)
        if (bookings.isEmpty()) {
            return false
        }

        venueBookingRepository.deleteAll(bookings)
        return true
    }

    /**
     * Ranks venues using a scoring algorithm that considers:
     * 1. Capacity efficiency (not too much wasted space)
     * 2. Amenity match
     * 3. Location preference
     * 4. Venue type preference
     */
    private fun rankVenues(venues: List<Venue>, request: VenueAllocationRequest): List<Venue> {
        // Scoring function
        val venueScores = venues.associateWith { venue ->
            var score = 0.0

            // Capacity efficiency: Prefer venues that aren't too much larger than needed
            // Lower score for venues with much larger capacity than needed (to avoid waste)
            val capacityRatio = venue.capacity.toDouble() / request.attendeeCount.toDouble()
            score += when {
                capacityRatio <= 1.1 -> 100.0  // Perfect match (0-10% extra capacity)
                capacityRatio <= 1.25 -> 80.0  // Good match (10-25% extra capacity)
                capacityRatio <= 1.5 -> 60.0   // Acceptable (25-50% extra capacity)
                capacityRatio <= 2.0 -> 40.0   // Not ideal (50-100% extra capacity)
                else -> 20.0                   // Poor match (more than double the needed capacity)
            }

            // Amenity match: Higher score for venues with all required amenities
            request.requiredAmenities?.let { requiredAmenities ->
                if (requiredAmenities.isNotEmpty()) {
                    val venueAmenityNames = venue.amenities.map { it.name.lowercase() }
                    val matchCount = requiredAmenities.count { requiredAmenity ->
                        venueAmenityNames.contains(requiredAmenity.lowercase())
                    }
                    // Add score based on percentage of matching amenities
                    score += (matchCount.toDouble() / requiredAmenities.size.toDouble()) * 50.0
                }
            }

            // Location preference: Bonus for matching preferred location
            request.preferredLocation?.let { preferredLocation ->
                if (venue.location.contains(preferredLocation, ignoreCase = true)) {
                    score += 30.0
                }
            }

            // Venue type preference: Bonus for matching preferred venue type
            request.preferredVenueType?.let { preferredType ->
                if (venue.type == preferredType) {
                    score += 40.0
                }
            }

            score
        }

        // Sort venues by score in descending order
        return venues.sortedByDescending { venueScores[it] ?: 0.0 }
    }
}