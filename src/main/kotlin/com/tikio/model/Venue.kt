package com.tikio.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "venues")
data class Venue(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var location: String,

    @Column(nullable = false)
    var capacity: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: VenueType,

    @Column(nullable = true)
    var description: String? = null,

    @Column(nullable = false)
    var availabilityStatus: Boolean = true,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "venue_amenity_mapping",
        joinColumns = [JoinColumn(name = "venue_id")],
        inverseJoinColumns = [JoinColumn(name = "amenity_id")]
    )
    var amenities: MutableSet<VenueAmenity> = mutableSetOf(),

    @OneToMany(mappedBy = "venue", cascade = [CascadeType.ALL], orphanRemoval = true)
    var bookings: MutableList<VenueBooking> = mutableListOf(),

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var imageUrl: String? = null
)
