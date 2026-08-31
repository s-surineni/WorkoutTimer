package com.example.workouttimer.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workouttimer.data.DefaultDataRepository
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout
import com.example.workouttimer.theme.WorkoutTimerTheme
import com.example.workouttimer.ui.components.TabataRoutineCard
import com.example.workouttimer.ui.components.TabataTimerRunner

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
            onDismiss = { viewModel.stopWorkout() }
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
                workouts = uiState.data,
                onAddClick = onAddClick,
                onEditClick = onEditClick,
                onStartWorkout = { viewModel.startWorkout(it) },
                onDeleteWorkout = { viewModel.removeWorkout(it) },
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
    onAddClick: () -> Unit,
    onEditClick: (Workout) -> Unit,
    onStartWorkout: (Workout) -> Unit,
    onDeleteWorkout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Tabata Workout Timer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Tabata Workout")
            }
        }
    ) { innerPadding ->
        if (workouts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
            onAddClick = {},
            onEditClick = {},
            onStartWorkout = {},
            onDeleteWorkout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenEmptyPreview() {
    WorkoutTimerTheme {
        MainScreenContent(
            workouts = emptyList(),
            onAddClick = {},
            onEditClick = {},
            onStartWorkout = {},
            onDeleteWorkout = {}
        )
    }
}
