package com.tikio.model

import jakarta.persistence.*

@Entity
@Table(name = "venue_amenities")
data class VenueAmenity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = true)
    val description: String? = null
)
