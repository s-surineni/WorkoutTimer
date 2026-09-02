package com.example.workouttimer.data

import androidx.compose.runtime.Immutable
import java.util.UUID
import kotlinx.serialization.Serializable

/**
 * Represents a full Tabata / HIIT workout routine consisting of multiple exercises,
 * configured rounds, and rest intervals.
 */
@Immutable
@Serializable
data class Workout(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val exercises: List<Exercise>,
    val rounds: Int = 1,
    val restBetweenRoundsSeconds: Int = 30
) {
    /**
     * Total duration of the workout in seconds, including all exercise work, rest,
     * and rest periods between rounds. The rest interval after the final exercise
     * in a routine is excluded.
     */
    val totalDurationSeconds: Int
        get() {
            val singleRoundWork = exercises.sumOf { it.workSeconds }
            val singleRoundRest = exercises.dropLast(1).sumOf { it.restSeconds }
            val singleRoundDuration = singleRoundWork + singleRoundRest
            val totalRoundsDuration = singleRoundDuration * rounds
            val interRoundRestDuration = (rounds - 1).coerceAtLeast(0) * restBetweenRoundsSeconds
            return totalRoundsDuration + interRoundRestDuration
        }

    /**
     * Formats total duration into mm:ss or hh:mm:ss string.
     */
    fun formattedTotalDuration(): String {
        val totalSec = totalDurationSeconds
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return if (minutes >= 60) {
            val hours = minutes / 60
            val remMinutes = minutes % 60
            String.format("%d:%02d:%02d", hours, remMinutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}
