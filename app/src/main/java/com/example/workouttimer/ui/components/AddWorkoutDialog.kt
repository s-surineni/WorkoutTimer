package com.example.workouttimer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Dialog to add a new workout routine with workout and cooldown durations in seconds.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddWorkoutDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (name: String, workoutSeconds: Int, cooldownSeconds: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var workoutDurationText by remember { mutableStateOf("30") }
    var cooldownDurationText by remember { mutableStateOf("10") }
    var nameError by remember { mutableStateOf(false) }
    var workoutDurationError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Add New Workout", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    label = { Text("Workout Name") },
                    placeholder = { Text("e.g., Jumping Jacks") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Workout name cannot be empty") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    OutlinedTextField(
                        value = workoutDurationText,
                        onValueChange = {
                            workoutDurationText = it.filter { char -> char.isDigit() }
                            if (workoutDurationError && (workoutDurationText.toIntOrNull() ?: 0) > 0) {
                                workoutDurationError = false
                            }
                        },
                        label = { Text("Workout Duration (seconds)") },
                        suffix = { Text("s") },
                        isError = workoutDurationError,
                        supportingText = if (workoutDurationError) {
                            { Text("Duration must be greater than 0") }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(15, 30, 45, 60).forEach { seconds ->
                            SuggestionChip(
                                onClick = {
                                    workoutDurationText = seconds.toString()
                                    workoutDurationError = false
                                },
                                label = { Text("${seconds}s") }
                            )
                        }
                    }
                }

                Column {
                    OutlinedTextField(
                        value = cooldownDurationText,
                        onValueChange = {
                            cooldownDurationText = it.filter { char -> char.isDigit() }
                        },
                        label = { Text("Cooldown Duration (seconds)") },
                        suffix = { Text("s") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(5, 10, 15, 30).forEach { seconds ->
                            SuggestionChip(
                                onClick = { cooldownDurationText = seconds.toString() },
                                label = { Text("${seconds}s") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val workoutSec = workoutDurationText.toIntOrNull() ?: 0
                    val cooldownSec = cooldownDurationText.toIntOrNull() ?: 0

                    val isNameValid = trimmedName.isNotEmpty()
                    val isDurationValid = workoutSec > 0

                    nameError = !isNameValid
                    workoutDurationError = !isDurationValid

                    if (isNameValid && isDurationValid) {
                        onConfirm(trimmedName, workoutSec, cooldownSec)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AddWorkoutDialogPreview() {
    AddWorkoutDialog(
        onDismissRequest = {},
        onConfirm = { _, _, _ -> }
    )
}

