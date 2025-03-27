package com.tikio.dto

data class AllocationResponseDTO(
    val success: Boolean,
    val venueId: Long?,
    val venueName: String?,
    val message: String
)