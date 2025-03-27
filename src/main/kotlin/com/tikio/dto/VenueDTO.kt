package com.tikio.dto

import com.tikio.model.VenueType
import java.time.LocalDateTime

data class VenueDTO(
    val id: Long?,
    val name: String,
    val location: String,
    val capacity: Int,
    val type: VenueType,
    val description: String?,
    val availabilityStatus: Boolean,
    val amenities: List<String>,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)