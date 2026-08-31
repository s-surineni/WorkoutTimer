package com.example.workouttimer.data.local

import androidx.room.TypeConverter
import com.example.workouttimer.data.Exercise
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Type converters allowing Room to store List<Exercise> as JSON string.
 */
class Converters {
    @TypeConverter
    fun fromExerciseList(value: List<Exercise>?): String {
        return if (value.isNullOrEmpty()) "[]" else Json.encodeToString(value)
    }

    @TypeConverter
    fun toExerciseList(value: String?): List<Exercise> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            Json.decodeFromString<List<Exercise>>(value)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
