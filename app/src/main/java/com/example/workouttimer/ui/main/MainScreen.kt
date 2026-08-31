package com.example.workouttimer.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workouttimer.data.DefaultDataRepository
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout
import com.example.workouttimer.data.WorkoutHistoryRecord
import com.example.workouttimer.theme.WorkoutTimerTheme
import com.example.workouttimer.ui.components.TabataRoutineCard
import com.example.workouttimer.ui.components.TabataTimerRunner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    onAddClick: () -> Unit,
    onEditClick: (Workout) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(DefaultDataRepository()) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activeWorkout by viewModel.activeWorkout.collectAsStateWithLifecycle()

    activeWorkout?.let { workout ->
        TabataTimerRunner(
            workout = workout,
            onDismiss = { viewModel.stopWorkout() },
            onWorkoutComplete = { completedWorkout, duration ->
                viewModel.logWorkoutCompletion(completedWorkout, duration)
            }
        )
    }

    when (val uiState = state) {
        MainScreenUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is MainScreenUiState.Success -> {
            MainScreenContent(
                workouts = uiState.workouts,
                history = uiState.history,
                onAddClick = onAddClick,
                onEditClick = onEditClick,
                onStartWorkout = { viewModel.startWorkout(it) },
                onDeleteWorkout = { viewModel.removeWorkout(it) },
                onClearHistory = { viewModel.clearHistory() },
                modifier = modifier
            )
        }
        is MainScreenUiState.Error -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error loading workouts: ${uiState.throwable.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreenContent(
    workouts: List<Workout>,
    history: List<WorkoutHistoryRecord> = emptyList(),
    onAddClick: () -> Unit,
    onEditClick: (Workout) -> Unit,
    onStartWorkout: (Workout) -> Unit,
    onDeleteWorkout: (String) -> Unit,
    onClearHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Tabata Workout Timer") },
                actions = {
                    if (selectedTabIndex == 1 && history.isNotEmpty()) {
                        IconButton(onClick = onClearHistory) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Tabata Workout")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Routines (${workouts.size})") },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("History & Stats (${history.size})") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
            }

            if (selectedTabIndex == 0) {
                RoutinesTabContent(
                    workouts = workouts,
                    onEditClick = onEditClick,
                    onStartWorkout = onStartWorkout,
                    onDeleteWorkout = onDeleteWorkout
                )
            } else {
                HistoryTabContent(history = history)
            }
        }
    }
}

@Composable
private fun RoutinesTabContent(
    workouts: List<Workout>,
    onEditClick: (Workout) -> Unit,
    onStartWorkout: (Workout) -> Unit,
    onDeleteWorkout: (String) -> Unit
) {
    if (workouts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Tabata routines yet",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the + button below to create your first multi-exercise Tabata workout.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(workouts, key = { it.id }) { workout ->
                TabataRoutineCard(
                    workout = workout,
                    onStart = { onStartWorkout(workout) },
                    onEdit = { onEditClick(workout) },
                    onDelete = { onDeleteWorkout(workout.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HistoryTabContent(history: List<WorkoutHistoryRecord>) {
    val totalWorkouts = history.size
    val totalSeconds = history.sumOf { it.totalDurationSeconds }
    val totalMinutes = totalSeconds / 60
    val totalRounds = history.sumOf { it.roundsCompleted }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Statistics Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Training Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            icon = Icons.Default.EmojiEvents,
                            value = "$totalWorkouts",
                            label = "Completed"
                        )
                        StatItem(
                            icon = Icons.Default.Timer,
                            value = "${totalMinutes}m",
                            label = "Time Trained"
                        )
                        StatItem(
                            icon = Icons.Default.FitnessCenter,
                            value = "$totalRounds",
                            label = "Rounds Done"
                        )
                    }
                }
            }
        }

        if (history.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No workouts completed yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Complete a workout from the Routines tab to see your logged training sessions here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(history, key = { it.id }) { record ->
                HistoryItemCard(record = record)
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun HistoryItemCard(record: WorkoutHistoryRecord) {
    val dateString = remember(record.timestampMillis) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        sdf.format(Date(record.timestampMillis))
    }

    val durationString = remember(record.totalDurationSeconds) {
        val mins = record.totalDurationSeconds / 60
        val secs = record.totalDurationSeconds % 60
        if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.workoutTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${record.roundsCompleted} Rounds • ${record.totalExercises} Exercises",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = durationString,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WorkoutTimerTheme {
        MainScreenContent(
            workouts = listOf(
                Workout(
                    title = "Classic Tabata",
                    rounds = 2,
                    restBetweenRoundsSeconds = 30,
                    exercises = listOf(
                        Exercise(name = "Jumping Jacks", workSeconds = 20, restSeconds = 10),
                        Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10),
                    )
                )
            ),
            history = listOf(
                WorkoutHistoryRecord(
                    workoutId = "1",
                    workoutTitle = "Classic Tabata",
                    totalDurationSeconds = 240,
                    roundsCompleted = 2,
                    totalExercises = 2
                )
            ),
            onAddClick = {},
            onEditClick = {},
            onStartWorkout = {},
            onDeleteWorkout = {},
            onClearHistory = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenEmptyPreview() {
    WorkoutTimerTheme {
        MainScreenContent(
            workouts = emptyList(),
            history = emptyList(),
            onAddClick = {},
            onEditClick = {},
            onStartWorkout = {},
            onDeleteWorkout = {},
            onClearHistory = {}
        )
    }
}
