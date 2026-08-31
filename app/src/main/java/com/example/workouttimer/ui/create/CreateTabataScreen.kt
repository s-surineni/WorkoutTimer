package com.example.workouttimer.ui.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout
import com.example.workouttimer.theme.WorkoutTimerTheme

/**
 * Full-page screen to create a new Tabata routine or edit an existing one.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTabataScreen(
    onNavigateBack: () -> Unit,
    onSaveWorkout: (Workout) -> Unit,
    modifier: Modifier = Modifier,
    initialWorkout: Workout? = null
) {
    val isEditMode = initialWorkout != null
    var title by remember { mutableStateOf(initialWorkout?.title ?: "") }
    var titleError by remember { mutableStateOf(false) }
    var rounds by remember { mutableIntStateOf(initialWorkout?.rounds ?: 2) }
    var restBetweenRoundsText by remember { mutableStateOf(initialWorkout?.restBetweenRoundsSeconds?.toString() ?: "30") }

    val exercises = remember {
        mutableStateListOf<Exercise>().apply {
            if (initialWorkout != null) {
                addAll(initialWorkout.exercises)
            } else {
                addAll(
                    listOf(
                        Exercise(name = "Jumping Jacks", workSeconds = 20, restSeconds = 10),
                        Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10),
                    )
                )
            }
        }
    }

    var newExerciseName by remember { mutableStateOf("") }
    var newWorkSecondsText by remember { mutableStateOf("20") }
    var newRestSecondsText by remember { mutableStateOf("10") }
    var exerciseListError by remember { mutableStateOf(false) }
    var addExerciseInputError by remember { mutableStateOf(false) }

    val quickExerciseSuggestions = listOf(
        "Squats", "Burpees", "Plank", "Mountain Climbers", "High Knees", "Lunges", "Crunches", "Jumping Jacks", "Push Ups"
    )

    val currentTotalDurationSeconds by remember {
        derivedStateOf {
            val restBetweenRounds = restBetweenRoundsText.toIntOrNull() ?: 0
            val singleRound = exercises.sumOf { it.workSeconds + it.restSeconds }
            (singleRound * rounds) + ((rounds - 1).coerceAtLeast(0) * restBetweenRounds)
        }
    }

    fun formattedLiveDuration(): String {
        val totalSec = currentTotalDurationSeconds
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return if (minutes >= 60) {
            String.format("%d:%02d:%02d", minutes / 60, minutes % 60, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    fun validateAndSave() {
        val trimmedTitle = title.trim()
        val isTitleValid = trimmedTitle.isNotEmpty()
        val hasExercises = exercises.isNotEmpty()

        titleError = !isTitleValid
        exerciseListError = !hasExercises

        if (isTitleValid && hasExercises) {
            val restBetweenRounds = restBetweenRoundsText.toIntOrNull() ?: 30
            val workout = if (initialWorkout != null) {
                initialWorkout.copy(
                    title = trimmedTitle,
                    rounds = rounds,
                    restBetweenRoundsSeconds = restBetweenRounds,
                    exercises = exercises.toList()
                )
            } else {
                Workout(
                    title = trimmedTitle,
                    rounds = rounds,
                    restBetweenRoundsSeconds = restBetweenRounds,
                    exercises = exercises.toList()
                )
            }
            onSaveWorkout(workout)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Tabata Workout" else "Create Tabata Workout") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { validateAndSave() }) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { validateAndSave() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditMode) "Update Tabata Workout" else "Save Tabata Workout",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Summary Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Estimated Duration",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = formattedLiveDuration(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$rounds Rounds",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${exercises.size} Exercises",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Section 1: Routine Configuration
            Text(
                text = "1. Workout Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (titleError && it.isNotBlank()) titleError = false
                },
                label = { Text("Workout Title") },
                placeholder = { Text("e.g., Morning HIIT Blast") },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("Workout title cannot be empty") }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            // Rounds configuration card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Number of Rounds: $rounds",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(
                                onClick = { if (rounds > 1) rounds -= 1 },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease rounds")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalIconButton(
                                onClick = { if (rounds < 12) rounds += 1 },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Increase rounds")
                            }
                        }
                    }

                    // Round preset chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(1, 2, 3, 4, 6, 8).forEach { r ->
                            SuggestionChip(
                                onClick = { rounds = r },
                                label = { Text("$r Round${if (r > 1) "s" else ""}") }
                            )
                        }
                    }

                    // Rest between rounds
                    OutlinedTextField(
                        value = restBetweenRoundsText,
                        onValueChange = { restBetweenRoundsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Rest Between Rounds (seconds)") },
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(15, 30, 45, 60).forEach { s ->
                            SuggestionChip(
                                onClick = { restBetweenRoundsText = s.toString() },
                                label = { Text("${s}s Rest") }
                            )
                        }
                    }
                }
            }

            // Section 2: Exercise Sequence List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2. Exercise Sequence (${exercises.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (exerciseListError) {
                    Text(
                        text = "Add at least 1 exercise",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (exercises.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No exercises in sequence yet. Use the form below to add exercises.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercises.forEachIndexed { index, exercise ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = exercise.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Work: ${exercise.workSeconds}s  •  Rest: ${exercise.restSeconds}s",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(onClick = { exercises.removeAt(index) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove exercise",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Add Exercise Form
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "3. Add Exercise to Sequence",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = newExerciseName,
                        onValueChange = {
                            newExerciseName = it
                            if (addExerciseInputError && it.isNotBlank()) addExerciseInputError = false
                        },
                        label = { Text("Exercise Name") },
                        placeholder = { Text("e.g., Mountain Climbers") },
                        isError = addExerciseInputError,
                        supportingText = if (addExerciseInputError) {
                            { Text("Exercise name cannot be empty") }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Suggestion chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickExerciseSuggestions.forEach { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    newExerciseName = suggestion
                                    addExerciseInputError = false
                                },
                                label = { Text(suggestion, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newWorkSecondsText,
                            onValueChange = { newWorkSecondsText = it.filter { c -> c.isDigit() } },
                            label = { Text("Work Duration") },
                            suffix = { Text("s") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = newRestSecondsText,
                            onValueChange = { newRestSecondsText = it.filter { c -> c.isDigit() } },
                            label = { Text("Rest Duration") },
                            suffix = { Text("s") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Duration interval preset chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            Pair(20, 10),
                            Pair(30, 15),
                            Pair(40, 20),
                            Pair(45, 15),
                            Pair(60, 20)
                        ).forEach { (work, rest) ->
                            SuggestionChip(
                                onClick = {
                                    newWorkSecondsText = work.toString()
                                    newRestSecondsText = rest.toString()
                                },
                                label = { Text("${work}s Work / ${rest}s Rest", style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            val trimmed = newExerciseName.trim()
                            val workSec = newWorkSecondsText.toIntOrNull() ?: 20
                            val restSec = newRestSecondsText.toIntOrNull() ?: 10
                            if (trimmed.isEmpty()) {
                                addExerciseInputError = true
                            } else if (workSec > 0) {
                                exercises.add(Exercise(name = trimmed, workSeconds = workSec, restSeconds = restSec))
                                newExerciseName = ""
                                addExerciseInputError = false
                                exerciseListError = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Exercise to Sequence")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateTabataScreenPreview() {
    WorkoutTimerTheme {
        CreateTabataScreen(
            onNavigateBack = {},
            onSaveWorkout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditTabataScreenPreview() {
    WorkoutTimerTheme {
        CreateTabataScreen(
            initialWorkout = Workout(
                title = "Core HIIT",
                rounds = 3,
                restBetweenRoundsSeconds = 30,
                exercises = listOf(Exercise(name = "Plank", workSeconds = 30, restSeconds = 15))
            ),
            onNavigateBack = {},
            onSaveWorkout = {}
        )
    }
}
