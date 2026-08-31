package com.example.workouttimer.data

import androidx.compose.runtime.Immutable
import java.util.UUID

/**
 * Domain model representing a completed workout session log.
 */
@Immutable
data class WorkoutHistoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val workoutId: String,
    val workoutTitle: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val totalDurationSeconds: Int,
    val roundsCompleted: Int,
    val totalExercises: Int
)
