package com.tikio.util

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Utility class for handling date and time operations
 */
object DateTimeUtil {

    /**
     * Checks if two time periods overlap
     */
    fun isOverlapping(
        startTime1: LocalDateTime,
        endTime1: LocalDateTime,
        startTime2: LocalDateTime,
        endTime2: LocalDateTime
    ): Boolean {
        return startTime1.isBefore(endTime2) && endTime1.isAfter(startTime2)
    }

    /**
     * Calculates the duration between two timestamps in minutes
     */
    fun durationInMinutes(startTime: LocalDateTime, endTime: LocalDateTime): Long {
        return ChronoUnit.MINUTES.between(startTime, endTime)
    }

    /**
     * Calculates the duration between two timestamps in hours
     */
    fun durationInHours(startTime: LocalDateTime, endTime: LocalDateTime): Double {
        val minutes = durationInMinutes(startTime, endTime)
        return minutes / 60.0
    }

    /**
     * Gets the epoch time in seconds for a LocalDateTime
     */
    fun getEpochSeconds(dateTime: LocalDateTime): Long {
        return dateTime.toEpochSecond(ZoneOffset.UTC)
    }
}
