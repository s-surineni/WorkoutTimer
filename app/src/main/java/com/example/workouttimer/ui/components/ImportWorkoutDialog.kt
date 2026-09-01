package com.example.workouttimer.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workouttimer.data.Workout
import com.example.workouttimer.data.WorkoutJsonCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Dialog allowing users to import Tabata routines by pasting JSON or picking a JSON file.
 */
@Composable
fun ImportWorkoutDialog(
    onDismiss: () -> Unit,
    onWorkoutsImported: (List<Workout>) -> Unit,
    modifier: Modifier = Modifier
) {
    var jsonText by remember { mutableStateOf("") }
    var parsedWorkouts by remember { mutableStateOf<List<Workout>?>(null) }
    var parseError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun updateAndValidateJson(input: String) {
        jsonText = input
        if (input.isBlank()) {
            parsedWorkouts = null
            parseError = null
            return
        }
        val result = WorkoutJsonCodec.decodeWorkouts(input, generateNewIds = true)
        result.onSuccess {
            parsedWorkouts = it
            parseError = null
        }.onFailure {
            parsedWorkouts = null
            parseError = it.localizedMessage ?: "Invalid workout JSON format"
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val content = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            stream.bufferedReader().readText()
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                content?.let { updateAndValidateJson(it) }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.UploadFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Import Workout Routines",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Paste a shared Tabata workout JSON payload below or pick a .json file from your device:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.FileOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select JSON File")
                }

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { updateAndValidateJson(it) },
                    label = { Text("Workout JSON Content") },
                    placeholder = { Text("{\"title\": \"HIIT Sprint\", ...}") },
                    isError = parseError != null,
                    supportingText = {
                        if (parseError != null) {
                            Text(
                                text = parseError ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (parsedWorkouts != null) {
                            Text(
                                text = "Valid workout configuration (${parsedWorkouts?.size} routine(s) detected)",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 200.dp)
                )

                // Preview Card if valid
                parsedWorkouts?.let { workouts ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ready to Import (${workouts.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            workouts.forEach { w ->
                                Text(
                                    text = "• ${w.title} (${w.rounds} rds • ${w.exercises.size} ex • ${w.formattedTotalDuration()})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    parsedWorkouts?.let {
                        onWorkoutsImported(it)
                        onDismiss()
                    }
                },
                enabled = !parsedWorkouts.isNullOrEmpty()
            ) {
                Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Import Routine(s)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

