package com.tikio.repository

import com.tikio.model.Venue
import com.tikio.model.VenueType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime


@Repository
interface VenueRepository : JpaRepository<Venue, Long> {
    fun findByCapacityGreaterThanEqualAndAvailabilityStatusTrue(capacity: Int): List<Venue>

    fun findByTypeAndAvailabilityStatusTrue(type: VenueType): List<Venue>

    fun findByLocationContainingIgnoreCaseAndAvailabilityStatusTrue(location: String): List<Venue>

    @Query("""
        SELECT v FROM Venue v 
        WHERE v.capacity >= :capacity 
        AND v.availabilityStatus = true 
        AND v.type = :type
    """)
    fun findAvailableVenuesByCapacityAndType(
        @Param("capacity") capacity: Int,
        @Param("type") type: VenueType
    ): List<Venue>

    @Query("""
        SELECT DISTINCT v FROM Venue v 
        JOIN v.amenities a 
        WHERE v.availabilityStatus = true 
        AND a.name IN :amenities 
        GROUP BY v.id 
        HAVING COUNT(DISTINCT a.name) = :amenityCount
    """)
    fun findByAmenitiesIn(
        @Param("amenities") amenities: List<String>,
        @Param("amenityCount") amenityCount: Long
    ): List<Venue>

    @Query("""
        SELECT v FROM Venue v
        WHERE v.capacity >= :minCapacity
        AND v.availabilityStatus = true
        AND NOT EXISTS (
            SELECT b FROM VenueBooking b
            WHERE b.venue = v
            AND (
                (b.startTime <= :endTime AND b.endTime >= :startTime)
            )
        )
    """)
    fun findAvailableVenuesByTimeAndCapacity(
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
        @Param("minCapacity") minCapacity: Int
    ): List<Venue>
}