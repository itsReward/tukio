package com.tikio.util

/**
 * Utility class for venue allocation algorithms
 */
object VenueAllocationAlgorithm {

    /**
     * Hill climbing algorithm implementation for venue allocation optimization
     * Will be expanded with more complex optimization logic in the future
     */
    fun hillClimbingOptimize(
        initialSolution: List<Pair<Long, Long>>,  // List of (venueId, eventId) pairs
        scoreFunction: (List<Pair<Long, Long>>) -> Double,  // Function to score a solution
        maxIterations: Int = 1000,
        stagnationLimit: Int = 100
    ): List<Pair<Long, Long>> {
        var currentSolution = initialSolution
        var currentScore = scoreFunction(currentSolution)
        var bestSolution = currentSolution
        var bestScore = currentScore

        var iterations = 0
        var stagnationCounter = 0

        while (iterations < maxIterations && stagnationCounter < stagnationLimit) {
            // Generate neighboring solution by swapping two assignments
            val neighbor = generateNeighbor(currentSolution)
            val neighborScore = scoreFunction(neighbor)

            // If neighbor is better, move to it
            if (neighborScore > currentScore) {
                currentSolution = neighbor
                currentScore = neighborScore
                stagnationCounter = 0

                // Update best solution if current is better
                if (currentScore > bestScore) {
                    bestSolution = currentSolution
                    bestScore = currentScore
                }
            } else {
                stagnationCounter++
            }

            iterations++
        }

        return bestSolution
    }

    /**
     * Generates a neighboring solution by swapping two random assignments
     */
    private fun generateNeighbor(solution: List<Pair<Long, Long>>): List<Pair<Long, Long>> {
        if (solution.size < 2) return solution

        val result = solution.toMutableList()
        val idx1 = (0 until solution.size).random()
        var idx2 = (0 until solution.size).random()

        // Ensure we pick two different indices
        while (idx1 == idx2) {
            idx2 = (0 until solution.size).random()
        }

        // Swap the venue assignments
        val temp = result[idx1]
        result[idx1] = Pair(result[idx2].first, result[idx1].second)
        result[idx2] = Pair(temp.first, result[idx2].second)

        return result
    }

    /**
     * Simple scoring function for venue allocations that considers:
     * - Capacity fit (penalty for over/under capacity)
     * - Venue preferences
     * - Time conflicts
     *
     * Note: This is a placeholder. Implement the actual scoring logic based on your requirements.
     */
    fun scoreAllocation(
        allocation: List<Pair<Long, Long>>,
        venueCapacities: Map<Long, Int>,
        eventAttendees: Map<Long, Int>,
        venuePreferences: Map<Long, List<Long>>,  // eventId -> preferred venueIds
        conflictMatrix: Map<Pair<Long, Long>, Boolean>  // (eventId1, eventId2) -> conflicts?
    ): Double {
        var score = 0.0

        // Process each venue-event assignment
        for ((venueId, eventId) in allocation) {
            // Check capacity fit
            val capacity = venueCapacities[venueId] ?: 0
            val attendees = eventAttendees[eventId] ?: 0

            if (capacity < attendees) {
                // Major penalty for under capacity
                score -= 1000.0
            } else {
                // Minor penalty for wasted capacity (efficiency)
                val wastedCapacity = capacity - attendees
                score -= wastedCapacity * 0.1
            }

            // Check venue preferences
            val preferences = venuePreferences[eventId] ?: emptyList()
            if (venueId in preferences) {
                score += 50.0
            }
        }

        // Check for scheduling conflicts
        for (i in allocation.indices) {
            for (j in i + 1 until allocation.size) {
                val event1 = allocation[i].second
                val event2 = allocation[j].second

                // If events are scheduled at the same venue and time periods conflict
                if (allocation[i].first == allocation[j].first &&
                    conflictMatrix[Pair(event1, event2)] == true) {
                    score -= 2000.0  // Major penalty for conflicts
                }
            }
        }

        return score
    }
}