package com.example.workouttimer.data

import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Utility for robust encoding and decoding of [Workout] routines to/from JSON.
 */
object WorkoutJsonCodec {
    val jsonFormat = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Serializes a single [Workout] to a formatted JSON string.
     */
    fun encodeWorkout(workout: Workout): String {
        return jsonFormat.encodeToString(workout)
    }

    /**
     * Serializes a list of [Workout] routines to a formatted JSON string.
     */
    fun encodeWorkouts(workouts: List<Workout>): String {
        return jsonFormat.encodeToString(workouts)
    }

    /**
     * Deserializes a JSON string into a [Workout] domain object.
     * Validates contents and assigns a new UUID if [generateNewId] is true.
     */
    fun decodeWorkout(jsonString: String, generateNewId: Boolean = true): Result<Workout> {
        return runCatching {
            val raw = jsonString.trim()
            val parsed = jsonFormat.decodeFromString<Workout>(raw)
            validateAndSanitizeWorkout(parsed, generateNewId)
        }
    }

    /**
     * Deserializes a JSON string into a list of [Workout]s, supporting both single objects
     * and array payloads.
     */
    fun decodeWorkouts(jsonString: String, generateNewIds: Boolean = true): Result<List<Workout>> {
        return runCatching {
            val raw = jsonString.trim()
            if (raw.startsWith("[")) {
                val list = jsonFormat.decodeFromString<List<Workout>>(raw)
                require(list.isNotEmpty()) { "Workout list is empty" }
                list.map { validateAndSanitizeWorkout(it, generateNewIds) }
            } else {
                val single = jsonFormat.decodeFromString<Workout>(raw)
                listOf(validateAndSanitizeWorkout(single, generateNewIds))
            }
        }
    }

    private fun validateAndSanitizeWorkout(workout: Workout, generateNewId: Boolean): Workout {
        require(workout.title.isNotBlank()) { "Workout title cannot be blank" }
        require(workout.rounds > 0) { "Rounds must be at least 1" }
        require(workout.exercises.isNotEmpty()) { "Workout must contain at least one exercise" }

        val sanitizedExercises = workout.exercises.map { exercise ->
            require(exercise.name.isNotBlank()) { "Exercise name cannot be blank" }
            require(exercise.workSeconds > 0) { "Exercise work duration must be positive" }
            require(exercise.restSeconds >= 0) { "Exercise rest duration cannot be negative" }
            if (generateNewId) exercise.copy(id = UUID.randomUUID().toString()) else exercise
        }

        return workout.copy(
            id = if (generateNewId) UUID.randomUUID().toString() else workout.id,
            title = workout.title.trim(),
            exercises = sanitizedExercises
        )
    }
}

