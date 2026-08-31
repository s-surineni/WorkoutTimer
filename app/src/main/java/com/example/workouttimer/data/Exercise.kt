package com.example.workouttimer.data

import androidx.compose.runtime.Immutable
import java.util.UUID
import kotlinx.serialization.Serializable

/**
 * Represents an individual exercise within a Tabata / HIIT workout routine.
 */
@Immutable
@Serializable
data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val workSeconds: Int = 20,
    val restSeconds: Int = 10
)
