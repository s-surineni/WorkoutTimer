package com.example.workouttimer.data

import java.util.UUID

/**
 * Represents a single workout exercise with work and cooldown intervals.
 */
data class Workout(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val workoutSeconds: Int,
    val cooldownSeconds: Int
)

