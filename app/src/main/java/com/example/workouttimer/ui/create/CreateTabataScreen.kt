package com.example.workouttimer.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.workouttimer.ui.components.AddEditExerciseDialog

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

    var exerciseListError by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var editingExerciseIndex by remember { mutableStateOf<Int?>(null) }

    // Dialog for adding a new exercise
    if (showAddExerciseDialog) {
        AddEditExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onSaveExercise = { newExercise ->
                exercises.add(newExercise)
                exerciseListError = false
            }
        )
    }

    // Dialog for editing an existing exercise
    editingExerciseIndex?.let { index ->
        if (index in exercises.indices) {
            AddEditExerciseDialog(
                initialExercise = exercises[index],
                onDismiss = { editingExerciseIndex = null },
                onSaveExercise = { updatedExercise ->
                    exercises[index] = updatedExercise
                    editingExerciseIndex = null
                }
            )
        }
    }

    val currentTotalDurationSeconds by remember {
        derivedStateOf {
            val restBetweenRounds = restBetweenRoundsText.toIntOrNull() ?: 0
            val singleRoundWork = exercises.sumOf { it.workSeconds }
            val singleRoundRest = exercises.dropLast(1).sumOf { it.restSeconds }
            val singleRound = singleRoundWork + singleRoundRest
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
                            imeAction = ImeAction.Done
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
                Column {
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

                Button(
                    onClick = { showAddExerciseDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Exercise")
                }
            }

            if (exercises.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No exercises in sequence yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Add Exercise' above to configure your workout routine.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercises.forEachIndexed { index, exercise ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingExerciseIndex = index },
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
                                        val isLastInList = index == exercises.lastIndex
                                        Text(
                                            text = if (isLastInList) {
                                                "Work: ${exercise.workSeconds}s  •  No Rest (End of Routine)"
                                            } else {
                                                "Work: ${exercise.workSeconds}s  •  Rest: ${exercise.restSeconds}s"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { editingExerciseIndex = index }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit exercise",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val item = exercises.removeAt(index)
                                                exercises.add(index - 1, item)
                                            }
                                        },
                                        enabled = index > 0
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Move exercise up"
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (index < exercises.lastIndex) {
                                                val item = exercises.removeAt(index)
                                                exercises.add(index + 1, item)
                                            }
                                        },
                                        enabled = index < exercises.lastIndex
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Move exercise down"
                                        )
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
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateTabataScreenPreview() {
    WorkoutTimerTheme {
        CreateTabataScreen(
            onNavigateBack = {},
            onSaveWorkout = {}
        )
    }
}
