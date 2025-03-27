package com.tikio.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "venue_bookings")
data class VenueBooking(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    val venue: Venue,

    @Column(nullable = false)
    val eventId: Long,

    @Column(nullable = false)
    val eventName: String,

    @Column(nullable = false)
    val startTime: LocalDateTime,

    @Column(nullable = false)
    val endTime: LocalDateTime,

    @Column(nullable = false)
    val attendeeCount: Int,

    @Column(nullable = true)
    val bookingNotes: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)