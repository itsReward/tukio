package com.tikio.repository

import com.tikio.model.VenueBooking
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface VenueBookingRepository : JpaRepository<VenueBooking, Long> {
    fun findByVenueId(venueId: Long): List<VenueBooking>

    fun findByVenueIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
        venueId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<VenueBooking>

    fun findByEventId(eventId: Long): List<VenueBooking>

    @Query("""
        SELECT b FROM VenueBooking b
        WHERE b.venue.id = :venueId
        AND (
            (b.startTime <= :endTime AND b.endTime >= :startTime)
        )
    """)
    fun findConflictingBookings(
        @Param("venueId") venueId: Long,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime
    ): List<VenueBooking>
}