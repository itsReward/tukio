package com.tikio.dto

import com.tikio.model.VenueType

data class VenueCreateRequest(
    val name: String,
    val location: String,
    val capacity: Int,
    val type: VenueType,
    val description: String?,
    val amenities: List<String>
)
