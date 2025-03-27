package com.tikio.repository

import com.tikio.model.VenueAmenity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VenueAmenityRepository : JpaRepository<VenueAmenity, Long> {
    fun findByNameIgnoreCase(name: String): VenueAmenity?

    fun findByNameIn(names: List<String>): List<VenueAmenity>
}