package com.example.workouttimer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.workouttimer.data.Exercise
import java.util.UUID

/**
 * Dialog for adding a new exercise or editing an existing exercise with duration presets and quick suggestions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditExerciseDialog(
    initialExercise: Exercise? = null,
    onDismiss: () -> Unit,
    onSaveExercise: (Exercise) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEdit = initialExercise != null
    var exerciseName by remember { mutableStateOf(initialExercise?.name ?: "") }
    var workSecondsText by remember { mutableStateOf(initialExercise?.workSeconds?.toString() ?: "20") }
    var restSecondsText by remember { mutableStateOf(initialExercise?.restSeconds?.toString() ?: "10") }
    var nameError by remember { mutableStateOf(false) }

    val quickSuggestions = remember {
        listOf("Push Ups", "Squats", "Burpees", "Jumping Jacks", "Plank", "Mountain Climbers", "Lunges", "High Knees")
    }
    val workPresets = remember { listOf(15, 20, 30, 40, 45, 60) }
    val restPresets = remember { listOf(0, 5, 10, 15, 20, 30) }

    fun validateAndSubmit() {
        val trimmed = exerciseName.trim()
        if (trimmed.isBlank()) {
            nameError = true
            return
        }
        val workSec = workSecondsText.toIntOrNull()?.coerceAtLeast(1) ?: 20
        val restSec = restSecondsText.toIntOrNull()?.coerceAtLeast(0) ?: 10

        val exercise = initialExercise?.copy(
            name = trimmed,
            workSeconds = workSec,
            restSeconds = restSec
        ) ?: Exercise(
            id = UUID.randomUUID().toString(),
            name = trimmed,
            workSeconds = workSec,
            restSeconds = restSec
        )

        onSaveExercise(exercise)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (isEdit) "Edit Exercise" else "Add Exercise",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = {
                        exerciseName = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    label = { Text("Exercise Name") },
                    placeholder = { Text("e.g., Mountain Climbers") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Exercise name cannot be empty") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick exercise name chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickSuggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = {
                                exerciseName = suggestion
                                nameError = false
                            },
                            label = { Text(suggestion, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Work Duration
                Text(
                    text = "Work Duration",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = workSecondsText,
                    onValueChange = { workSecondsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Work Time (seconds)") },
                    suffix = { Text("s") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    workPresets.forEach { s ->
                        SuggestionChip(
                            onClick = { workSecondsText = s.toString() },
                            label = { Text("${s}s") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Rest Duration
                Text(
                    text = "Rest Duration",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = restSecondsText,
                    onValueChange = { restSecondsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Rest Time (seconds)") },
                    suffix = { Text("s") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    restPresets.forEach { s ->
                        SuggestionChip(
                            onClick = { restSecondsText = s.toString() },
                            label = { Text("${s}s") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { validateAndSubmit() }) {
                Text(if (isEdit) "Save Changes" else "Add to Routine")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

