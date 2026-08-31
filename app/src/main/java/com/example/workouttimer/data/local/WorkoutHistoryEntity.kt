package com.example.workouttimer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.workouttimer.data.WorkoutHistoryRecord
import java.util.UUID

/**
 * Room entity representing a row in the "workout_history" table.
 */
@Entity(tableName = "workout_history")
data class WorkoutHistoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val workoutId: String,
    val workoutTitle: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val totalDurationSeconds: Int,
    val roundsCompleted: Int,
    val totalExercises: Int
) {
    fun toDomain(): WorkoutHistoryRecord = WorkoutHistoryRecord(
        id = id,
        workoutId = workoutId,
        workoutTitle = workoutTitle,
        timestampMillis = timestampMillis,
        totalDurationSeconds = totalDurationSeconds,
        roundsCompleted = roundsCompleted,
        totalExercises = totalExercises
    )
}

fun WorkoutHistoryRecord.toEntity(): WorkoutHistoryEntity = WorkoutHistoryEntity(
    id = id,
    workoutId = workoutId,
    workoutTitle = workoutTitle,
    timestampMillis = timestampMillis,
    totalDurationSeconds = totalDurationSeconds,
    roundsCompleted = roundsCompleted,
    totalExercises = totalExercises
)
