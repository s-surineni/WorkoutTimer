package com.example.workouttimer.data

import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Utility for robust encoding and decoding of [Workout] routines to/from JSON.
 * Includes smart JSON payload extraction to effortlessly handle copied chat messages,
 * markdown code blocks, and formatted share text.
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
     * Extracts a raw JSON substring from mixed text if the input contains surrounding
     * chat prose, headers, or markdown code fences (e.g. ```json ... ```).
     */
    fun extractJsonPayload(input: String): String {
        val trimmed = input.trim()
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
            (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return trimmed
        }

        // Check for markdown code fences ```json ... ``` or ``` ... ```
        val codeBlockRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```", RegexOption.IGNORE_CASE)
        val match = codeBlockRegex.find(trimmed)
        if (match != null) {
            val content = match.groupValues[1].trim()
            if (content.isNotEmpty()) return content
        }

        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        val firstBracket = trimmed.indexOf('[')
        val lastBracket = trimmed.lastIndexOf(']')

        // Determine whether an array or an object starts first in the text
        if (firstBracket != -1 && (firstBrace == -1 || firstBracket < firstBrace)) {
            if (lastBracket != -1 && lastBracket > firstBracket) {
                return trimmed.substring(firstBracket, lastBracket + 1).trim()
            }
        } else if (firstBrace != -1) {
            if (lastBrace != -1 && lastBrace > firstBrace) {
                return trimmed.substring(firstBrace, lastBrace + 1).trim()
            }
        }

        return trimmed
    }

    /**
     * Deserializes a JSON string (or mixed text containing JSON) into a [Workout] domain object.
     * Validates contents and assigns a new UUID if [generateNewId] is true.
     */
    fun decodeWorkout(jsonString: String, generateNewId: Boolean = true): Result<Workout> {
        return runCatching {
            val payload = extractJsonPayload(jsonString)
            val parsed = jsonFormat.decodeFromString<Workout>(payload)
            validateAndSanitizeWorkout(parsed, generateNewId)
        }
    }

    /**
     * Deserializes a JSON string (or mixed text containing JSON) into a list of [Workout]s,
     * supporting both single objects and array payloads.
     */
    fun decodeWorkouts(jsonString: String, generateNewIds: Boolean = true): Result<List<Workout>> {
        return runCatching {
            val payload = extractJsonPayload(jsonString)
            if (payload.startsWith("[")) {
                val list = jsonFormat.decodeFromString<List<Workout>>(payload)
                require(list.isNotEmpty()) { "Workout list is empty" }
                list.map { validateAndSanitizeWorkout(it, generateNewIds) }
            } else {
                val single = jsonFormat.decodeFromString<Workout>(payload)
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
