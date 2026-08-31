package com.example.workouttimer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A simple Compose card that accepts an exercise name, workout time (seconds),
 * and cooldown time (seconds). Includes start/pause/reset and a progress bar.
 */
@Composable
fun ExerciseTimerCard(
    exerciseName: String,
    workoutSeconds: Int,
    cooldownSeconds: Int,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
) {
    var isRunning by remember { mutableStateOf(false) }
    var isWorkoutPhase by remember { mutableStateOf(true) }
    val initial = if (isWorkoutPhase) workoutSeconds.coerceAtLeast(0) else cooldownSeconds.coerceAtLeast(0)
    var timeLeft by remember { mutableStateOf(initial) }

    // Keep timeLeft in sync when phase toggles or inputs change
    LaunchedEffect(isWorkoutPhase, workoutSeconds, cooldownSeconds) {
        timeLeft = if (isWorkoutPhase) workoutSeconds.coerceAtLeast(0) else cooldownSeconds.coerceAtLeast(0)
    }

    LaunchedEffect(isRunning, isWorkoutPhase, timeLeft) {
        while (isRunning && timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
            if (timeLeft <= 0) {
                if (isWorkoutPhase) {
                    isWorkoutPhase = false
                    timeLeft = cooldownSeconds.coerceAtLeast(0)
                } else {
                    // Finished cooldown -> stop
                    isRunning = false
                }
            }
        }
    }

    val totalForPhase = if (isWorkoutPhase) workoutSeconds.coerceAtLeast(1) else cooldownSeconds.coerceAtLeast(1)
    val progress by remember { derivedStateOf { 1f - (timeLeft.toFloat() / totalForPhase.toFloat()) } }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = exerciseName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (isWorkoutPhase) "Workout (${workoutSeconds}s)" else "Cooldown (${cooldownSeconds}s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isRunning = true }) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                    }
                    IconButton(onClick = { isRunning = false }) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Workout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Time left: ${timeLeft}s", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {
                    // reset to workout phase
                    isRunning = false
                    isWorkoutPhase = true
                    timeLeft = workoutSeconds.coerceAtLeast(0)
                }) {
                    Text("Reset")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseTimerCardPreview() {
    ExerciseTimerCard(
        exerciseName = "Push Ups",
        workoutSeconds = 30,
        cooldownSeconds = 10,
        modifier = Modifier.padding(16.dp),
        onDelete = {}
    )
}
