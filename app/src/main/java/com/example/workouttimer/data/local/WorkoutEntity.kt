package com.example.workouttimer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout

/**
 * Room database entity representing a stored Workout routine.
 */
@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val rounds: Int,
    val restBetweenRoundsSeconds: Int,
    val exercises: List<Exercise>
) {
    fun toDomain(): Workout = Workout(
        id = id,
        title = title,
        rounds = rounds,
        restBetweenRoundsSeconds = restBetweenRoundsSeconds,
        exercises = exercises
    )
}

fun Workout.toEntity(): WorkoutEntity = WorkoutEntity(
    id = id,
    title = title,
    rounds = rounds,
    restBetweenRoundsSeconds = restBetweenRoundsSeconds,
    exercises = exercises
)
