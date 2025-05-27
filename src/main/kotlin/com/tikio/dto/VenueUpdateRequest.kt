package com.tikio.dto

import com.tikio.model.VenueType


data class VenueUpdateRequest(
    val name: String?,
    val location: String?,
    val capacity: Int?,
    val type: VenueType?,
    val description: String?,
    val availabilityStatus: Boolean?,
    val amenities: List<String>?,
    val imgUrl: String
)