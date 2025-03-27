package com.tikio.controller

import com.tikio.dto.VenueCreateRequest
import com.tikio.dto.VenueDTO
import com.tikio.dto.*
import com.tikio.service.VenueAllocationService
import com.tikio.service.VenueService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/venues")
class VenueController(
    private val venueService: VenueService,
    private val venueAllocationService: VenueAllocationService
) {

    @GetMapping
    fun getAllVenues(): ResponseEntity<List<VenueDTO>> {
        return ResponseEntity.ok(venueService.getAllVenues())
    }

    @GetMapping("/{id}")
    fun getVenueById(@PathVariable id: Long): ResponseEntity<VenueDTO> {
        return ResponseEntity.ok(venueService.getVenueById(id))
    }

    @PostMapping
    fun createVenue(@RequestBody request: VenueCreateRequest): ResponseEntity<VenueDTO> {
        val venue = venueService.createVenue(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(venue)
    }

    @PutMapping("/{id}")
    fun updateVenue(
        @PathVariable id: Long,
        @RequestBody request: VenueUpdateRequest
    ): ResponseEntity<VenueDTO> {
        return ResponseEntity.ok(venueService.updateVenue(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteVenue(@PathVariable id: Long): ResponseEntity<Unit> {
        venueService.deleteVenue(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/available")
    fun findAvailableVenues(@RequestBody request: VenueAvailabilityRequest): ResponseEntity<List<VenueDTO>> {
        return ResponseEntity.ok(venueService.findAvailableVenues(request))
    }

    @GetMapping("/{id}/schedule")
    fun getVenueSchedule(
        @PathVariable id: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime?
    ): ResponseEntity<VenueScheduleDTO> {
        return if (startDate != null && endDate != null) {
            ResponseEntity.ok(venueService.getVenueSchedule(id, startDate, endDate))
        } else {
            ResponseEntity.ok(venueService.getVenueSchedule(id))
        }
    }

    @PostMapping("/allocate")
    fun allocateVenue(@RequestBody request: VenueAllocationRequest): ResponseEntity<AllocationResponseDTO> {
        return ResponseEntity.ok(venueAllocationService.allocateVenue(request))
    }

    @GetMapping("/{id}/availability")
    fun checkVenueAvailability(
        @PathVariable id: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startTime: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endTime: LocalDateTime
    ): ResponseEntity<Map<String, Boolean>> {
        val isAvailable = venueAllocationService.checkVenueAvailability(id, startTime, endTime)
        return ResponseEntity.ok(mapOf("available" to isAvailable))
    }

    @DeleteMapping("/bookings/event/{eventId}")
    fun cancelVenueBooking(@PathVariable eventId: Long): ResponseEntity<Map<String, Boolean>> {
        val success = venueAllocationService.cancelVenueBooking(eventId)
        return if (success) {
            ResponseEntity.ok(mapOf("cancelled" to true))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("cancelled" to false))
        }
    }
}